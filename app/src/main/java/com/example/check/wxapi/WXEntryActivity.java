package com.example.check.wxapi;

import android.app.Activity;
import android.os.Bundle;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.example.check.utils.WeChatManager;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IWXAPI api = WeChatManager.getApi();
        if (api != null) {
            api.handleIntent(getIntent(), this);
        } else {
            finish();
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        // 微信发送请求到第三方应用时回调
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        // 第三方应用发送请求到微信后的回调
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                // 成功
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                // 用户取消
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                // 认证被否决
                break;
            default:
                // 其他错误
                break;
        }
        finish();
    }
}