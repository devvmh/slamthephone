package com.callysto.devin.slamthephone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WaitForSlamService extends Service {
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private AccListener accListener;
    private MediaPlayer mp;
    
    private ITelephony telephonyService;
    
    private boolean practising = false;
    
    private Context context;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//if we're just practising it won't try to hang up
		if (intent != null) {
			practising = intent.getBooleanExtra("practising", false);
		}
		return START_STICKY;
	}

	public void onCreate() {
		context = getApplicationContext();
		
		setUpMediaPlayer();
		setUpITelephony();
		setUpAccListener();
		
		WaitForSlamService.this.notify("Service started");
	}
	
	@Override
	public void onDestroy() {
		mSensorManager.unregisterListener(accListener);
		WaitForSlamService.this.notify("Service stopped");
	}
	
	private void setUpMediaPlayer() {
		mp = MediaPlayer.create(this, R.raw.slam);
		mp.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				WaitForSlamService.this.notify("Ready to slam the phone.");
			}
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setUpITelephony() {
		TelephonyManager telephonyManager = (TelephonyManager) context
		        .getSystemService(Context.TELEPHONY_SERVICE);
		Class c = null;
		try {
			c = Class.forName(telephonyManager.getClass().getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Method m = null;
		try {
			m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
		} catch (SecurityException | NoSuchMethodException | NullPointerException e) {
			e.printStackTrace();
		}
		try {
			telephonyService = (ITelephony) m.invoke(telephonyManager);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private void setUpAccListener() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accListener = new AccListener();
        mSensorManager.registerListener(accListener, mAccelerometer, 1000000);
	}

	private static int notification_id = 0;
	@SuppressLint("NewApi")
	private void notify(String msg) {
		Log.v("SlamThePhone", msg);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    PendingIntent emptyIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);

	    Notification noti = new Notification.Builder(this, "com.callysto.devin.slamthephone.NotificationChannel")
	       .setContentTitle("Slam the Phone")
	       .setContentText(msg)
	       .setSmallIcon(R.drawable.phone)
	       .setContentIntent(emptyIntent)
	       .build();
	    noti.flags |= Notification.FLAG_AUTO_CANCEL;

	    mNotificationManager.notify(notification_id, noti);
	    notification_id ++;
	}

	private class AccListener implements SensorEventListener {
		boolean slammed = false;
		float x = 0, y = 0, z = 0;
		@SuppressWarnings("unused")
		float prevX = 0, prevY = 0, prevZ = 0;
		
		public void onAccuracyChanged(Sensor arg0, int arg1) {}

		public void onSensorChanged(SensorEvent e) {
			prevX = x;
			prevY = y;
			prevZ = z;
			x = e.values[0];
			y = e.values[1];
			z = e.values[2];
			if (!slammed && y < 0) {
				if (y < -10 && prevX < 5 && x > 5 && prevZ > 10 ) {
					WaitForSlamService.this.notify("Slamming!");
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
					mp.start(); //play slam sound

					if (!practising) {
						//wait 1.5s for the realization to sink in, then hang up
						slammed = true;
						new Handler().postDelayed(new Runnable() {
							public void run() {
								WaitForSlamService.this.notify("Trying to hang up");
								telephonyService.endCall();
								slammed = true;
								WaitForSlamService.this.notify("Slammed phone!");
							}
						}, (long) (1.5*1000)); //1.5s delay
					}
				}
			}//if
		}//onSensorChanged
	}//AccListener
}//WaitForSlamService