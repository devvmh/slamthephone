package com.callysto.devin.slamthephone;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(28)
public class SlamthePhoneActivity extends Activity {
	private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
	private CheckBox checkbox;
	private SharedPreferences savedData;
	public Button start, stop;
	private TextView help_msg;
	private boolean portrait = true;
	
	//fields for animating help text fade in/out
    private Handler h = new Handler();
    private final int NUM_FRAMES = 30;
    private float change = 1f / NUM_FRAMES;
    private float alpha = 0;
    private boolean fadingIn = false; //first click will start it fading in
    private Runnable r = new Runnable() {
		public void run() {
			//fade until you hit 0 or 1. The fade direction could
			//get reversed at any time by a button press!
			if (0 <= alpha && alpha <= 1) {
				//how many frames?
				alpha += change;
				help_msg.setAlpha(alpha);
				h.post(r);
			} else {
				//round it so fading doesn't get disabled
				alpha = (int) alpha;
			}
    	}
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewBasedOnOrientation();
        //grab the alpha that was saved
        if (savedInstanceState != null) {
        	alpha = savedInstanceState.getFloat("alpha", 0f);
        	fadingIn = savedInstanceState.getBoolean("fadingIn", false);
        }

		findViews();
		requestNeededPermissions();
    	checkPreferences();
    	//update the checkbox's value to the one saved in preferences
    	checkCheckbox(null);

		requestNeededPermissions();
	}

	private void requestNeededPermissions() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
				String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE};
				requestPermissions(permissions, PERMISSION_REQUEST_READ_PHONE_STATE);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putFloat("alpha", alpha);
		outState.putBoolean("fadingIn", fadingIn);
	}
	
	@TargetApi(28)
	private void setContentViewBasedOnOrientation() {
        WindowManager winMan = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        if (winMan != null) {
        	Point size = new Point();
        	winMan.getDefaultDisplay().getSize(size);
        	if (size.x <= size.y) {
                // Portrait
                setContentView(R.layout.main_portrait);
                portrait = true;
            } else {
                // Landscape
                setContentView(R.layout.main_landscape);
                portrait = false;
            }      
        }
	}
        
    private void findViews() {
    	checkbox = findViewById((portrait) ? R.id.enabled : R.id.enabled_landscape);
        help_msg = findViewById((portrait) ? R.id.help_message : R.id.help_message_landscape);
       	start = findViewById((portrait) ? R.id.startService : R.id.startService_landscape);
       	stop = findViewById((portrait) ? R.id.stopService : R.id.stopService_landscape);
       	
       	if (help_msg == null) {
       		help_msg = findViewById((portrait) ? R.id.help_message : R.id.help_message_landscape);
       	} else {
       		help_msg.setAlpha(alpha);
       	}
    }
        
    public void checkPreferences() {
		savedData = getSharedPreferences("savedData", MODE_PRIVATE);
    	checkbox.setChecked(savedData.getBoolean("enabled", true));
    }
  
    
    //so it can be used by onClick, use View v argument but ignore it
    public void checkCheckbox(View v) {
    	boolean val = checkbox.isChecked();
    	AppContext.setIsEnabled(val);
    	savedData.edit().putBoolean("enabled", checkbox.isChecked()).apply();
    	if (stop.isEnabled()) {
    		stopService(new Intent(this, WaitForSlamService.class));
    	}
    	start.setEnabled(val);
    	stop.setEnabled(false);
    }
    
    public void helpButtonOnClick(View v) {
    	fadingIn = !fadingIn; //toggle value
    	help_msg.setVisibility(View.VISIBLE);
    	if (fadingIn) {
			change = 1f / NUM_FRAMES; //positive
    	} else {
			change = - 1f / NUM_FRAMES; //negative
    	}
		h.removeCallbacks(r);
		h.post(r);
    }
    
    public void startService(View v) {
    	Intent startIntent = new Intent(this, WaitForSlamService.class);
    	startIntent.putExtra("practising", true);
    	startService(startIntent);
    	start.setEnabled(false);
    	stop.setEnabled(true);
    }
    
    public void stopService(View v) {
    	Intent stopIntent = new Intent(this, WaitForSlamService.class);
    	stopService(stopIntent);
    	stop.setEnabled(false);
    	start.setEnabled(true);
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_READ_PHONE_STATE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission granted: " + PERMISSION_REQUEST_READ_PHONE_STATE, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission NOT granted: " + PERMISSION_REQUEST_READ_PHONE_STATE, Toast.LENGTH_SHORT).show();
			}
		}
	}
}