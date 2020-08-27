package com.myfav.rider;

import android.app.Application;

public class RiderApplication extends Application {


    private static RiderApplication mInstance;

    public void onCreate()
    {
        super.onCreate();
        mInstance=this;
    }

    public static synchronized RiderApplication getInstance()
    {
        return mInstance;
    }


}