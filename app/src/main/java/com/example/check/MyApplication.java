package com.example.check;

import android.app.Application;
import com.example.check.utils.WeChatManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化微信API
        WeChatManager.registerApp(this);
    }
}