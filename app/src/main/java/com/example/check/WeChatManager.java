package com.example.check;

import android.content.Context;
import android.util.Log;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WeChatManager {
    private static final String TAG = "WeChatManager";
    private static final String APP_ID = "wx0d7594c55eae201f";

    private static IWXAPI api;

    public static void registerApp(Context context) {
        if (api == null) {
            api = WXAPIFactory.createWXAPI(context, APP_ID, true);
            boolean registered = api.registerApp(APP_ID);
            Log.d(TAG, "微信API注册结果: " + registered);
        }
    }

    /**
     * 跳转到指定小程序
     * @param context 上下文
     * @param miniProgramId 小程序ID
     * @param path 页面路径（可选）
     * @return 是否成功发起跳转
     */
    public static boolean launchMiniProgram(Context context, String miniProgramId, String path) {
        Log.d(TAG, "跳转到小程序: " + miniProgramId);

        if (api == null) {
            registerApp(context);
        }

        try {
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = miniProgramId;
            req.path = path;

            // 根据小程序ID选择类型
            if (miniProgramId.equals("gh_d43f693ca31f")) {
                req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_TEST; // 测试版
            } else {
                req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE; // 正式版
            }

            boolean result = api.sendReq(req);
            Log.d(TAG, "跳转结果: " + result);
            return result;

        } catch (Exception e) {
            Log.e(TAG, "跳转异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 简化版：跳转到小程序首页
     */
    public static boolean launchMiniProgram(Context context, String miniProgramId) {
        return launchMiniProgram(context, miniProgramId, "");
    }

    /**
     * 跳转到微信考勤小程序
     */
    public static boolean launchToAttendanceProgram(Context context) {
        return launchMiniProgram(context, "gh_d2d41b77389b", "");
    }

    /**
     * 跳转到微信测试小程序
     */
    public static boolean launchToTestProgram(Context context) {
        return launchMiniProgram(context, "gh_d43f693ca31f", "");
    }

    public static IWXAPI getApi() {
        return api;
    }
}