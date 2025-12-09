package com.example.check;

import android.app.Application;
import com.example.check.WeChatManager; // 确认导入路径正确

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化微信API
        WeChatManager.registerApp(this);
    }
}