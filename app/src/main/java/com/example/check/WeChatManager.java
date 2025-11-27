package com.example.check.utils;

import android.content.Context;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WeChatManager {
    private static final String APP_ID = "wx13439bc546007458"; // 你的微信AppID

    private static IWXAPI api;

    public static boolean registerApp(Context context) {
        api = WXAPIFactory.createWXAPI(context, APP_ID, true);
        return api.registerApp(APP_ID);
    }

    public static boolean launchMiniProgram(Context context, String miniProgramId, String path) {
        if (api == null) {
            registerApp(context);
        }

        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        req.userName = miniProgramId; // 小程序原始id
        req.path = path; // 小程序页面路径，可空
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE; // 正式版

        return api.sendReq(req);
    }

    public static boolean launchMiniProgram(Context context, String miniProgramId) {
        return launchMiniProgram(context, miniProgramId, "");
    }

    public static IWXAPI getApi() {
        return api;
    }
}