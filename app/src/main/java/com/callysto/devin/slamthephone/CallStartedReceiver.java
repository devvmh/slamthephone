package com.callysto.devin.slamthephone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallStartedReceiver extends BroadcastReceiver {
	static boolean alreadyStarted = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!alreadyStarted && AppContext.isEnabled()) {
			Log.v("SlamThePhone", "Starting to listen");
			MyPhoneStateListener phoneListener = new MyPhoneStateListener(context);
			TelephonyManager telephony = (TelephonyManager) 
					context.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
			alreadyStarted = true;
		}
	}
}

class MyPhoneStateListener extends PhoneStateListener {
	private Context context;
	
	public MyPhoneStateListener(Context c) {
		this.context = c;
	}
	
	public void onCallStateChanged(int state,String incomingNumber){
		switch(state){
	    case TelephonyManager.CALL_STATE_IDLE:
	    	context.stopService(new Intent(context, WaitForSlamService.class));
	    break;
	    case TelephonyManager.CALL_STATE_OFFHOOK:
	    	context.stopService(new Intent(context, WaitForSlamService.class));
	    	context.startService(new Intent(context, WaitForSlamService.class));
	    break;
	    case TelephonyManager.CALL_STATE_RINGING:
	    break;
	    }
	} 
}
