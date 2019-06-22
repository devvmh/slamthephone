package com.callysto.devin.slamthephone;

import android.app.Application;

public class AppContext extends Application {

    //sensible place to declare a log tag for the application
    public static final String LOG_TAG = "com.callysto.devin.slamthephone";

    //instance 
    private static AppContext instance = null;

    //keep references to our global resources
    private static Boolean enabled = null;

    /**
     * Convenient accessor, saves having to call and cast getApplicationContext() 
     */
    public static AppContext getInstance() {
        checkInstance();
        return instance;
    }

    /**
     * Accessor for some resource that depends on a context
     */
    public static Boolean isEnabled() {
        if (enabled == null) {
            checkInstance();
            enabled = false;
        }
        return enabled;
    }
    
    public static void setIsEnabled(boolean value) {
    	if (enabled == null) {
    		checkInstance();
    	}
    	enabled = value;
    }


    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //provide an instance for our static accessors
        instance = this;
    }

}