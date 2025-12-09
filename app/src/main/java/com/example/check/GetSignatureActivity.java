/*package com.example.check;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.security.MessageDigest;

public class GetSignatureActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        setContentView(textView);
        textView.setTextSize(18);
        textView.setPadding(50, 50, 50, 50);

        try {
            // 1. 获取应用信息
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES
            );

            // 2. 获取签名
            Signature[] signatures = packageInfo.signatures;
            Signature signature = signatures[0];
            byte[] cert = signature.toByteArray();

            // 3. 计算MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Digest = md.digest(cert);
            String md5 = bytesToHex(md5Digest).toUpperCase();

            // 4. 显示结果
            String result = "✅ 签名获取成功\n\n" +
                    "应用包名: " + getPackageName() + "\n\n" +
                    "MD5签名 (32位):\n" + md5 + "\n\n" +
                    "步骤:\n" +
                    "1. 复制上方MD5签名\n" +
                    "2. 登录微信开放平台\n" +
                    "3. 更新应用签名\n" +
                    "4. 等待5分钟生效";

            textView.setText(result);

            // 5. 在Logcat中输出（方便复制）
            Log.d("WECHAT_SIGNATURE", "==================================");
            Log.d("WECHAT_SIGNATURE", "包名: " + getPackageName());
            Log.d("WECHAT_SIGNATURE", "MD5签名: " + md5);
            Log.d("WECHAT_SIGNATURE", "==================================");

        } catch (Exception e) {
            textView.setText("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String h = Integer.toHexString(0xFF & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }
}

 */