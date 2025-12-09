package com.example.check.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.example.check.WeChatManager; // 确认导入路径正确

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "微信回调Activity被创建");

        // 使用WeChatManager.getApi() - 现在这个方法存在了
        IWXAPI api = WeChatManager.getApi();

        if (api != null) {
            try {
                api.handleIntent(getIntent(), this);
                Log.d(TAG, "成功处理微信回调Intent");
            } catch (Exception e) {
                Log.e(TAG, "处理微信回调时发生异常", e);
                finish();
            }
        } else {
            Log.e(TAG, "无法获取微信API实例，Activity将关闭");
            finish();
        }
    }

    @Override
    public void onReq(BaseReq req) {
        Log.d(TAG, "收到微信请求, 类型: " + req.getType());
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "收到微信响应, 错误码: " + resp.errCode);

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.d(TAG, "跳转小程序成功");
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Log.w(TAG, "用户取消跳转");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Log.e(TAG, "授权被拒绝");
                break;
            default:
                Log.e(TAG, "跳转失败，错误码: " + resp.errCode);
                break;
        }
        finish();
    }
}