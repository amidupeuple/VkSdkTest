package com.example.dpivovar.vksdktest;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by dpivovar on 15.08.2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(MyApplication.this);
    }

}
