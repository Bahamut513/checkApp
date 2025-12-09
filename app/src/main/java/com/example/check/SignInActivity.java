package com.example.check;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    // 视图组件
    private TextView tvStatus;
    private TextView tvCurrentCourse;
    private Button btnWechatMiniProgram;
    private Button btnTestMiniProgram;
    private Button btnOpenWechat;
    private Button btnOpenChaoxing;

    // 常量定义
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String CHAOXING_PACKAGE = "com.chaoxing.mobile";
    private static final String TARGET_MINI_PROGRAM = "gh_d2d41b77389b"; // 微信考勤小程序
    private static final String TEST_MINI_PROGRAM = "gh_d43f693ca31f";   // 微信测试小程序

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // 初始化视图
        initViews();

        // 初始化微信API
        WeChatManager.registerApp(this);

        // 设置点击监听
        setClickListeners();

        // 模拟课程查询
        findCurrentCourse();

        // 更新状态
        updateStatus("应用已就绪");
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvCurrentCourse = findViewById(R.id.tv_current_course);
        btnWechatMiniProgram = findViewById(R.id.btn_wechat_mini_program);
        btnTestMiniProgram = findViewById(R.id.btn_test_mini_program);
        btnOpenWechat = findViewById(R.id.btn_open_wechat);
        btnOpenChaoxing = findViewById(R.id.btn_open_chaoxing);
    }

    private void setClickListeners() {
        // 1. 跳转到微信考勤小程序
        btnWechatMiniProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchToWeChatMiniProgram(TARGET_MINI_PROGRAM, "考勤小程序");
            }
        });

        // 2. 测试小程序演示
        btnTestMiniProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchToWeChatMiniProgram(TEST_MINI_PROGRAM, "测试小程序");
            }
        });

        // 3. 打开微信APP
        btnOpenWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeChatApp();
            }
        });

        // 4. 打开学习通APP
        btnOpenChaoxing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChaoxingApp();
            }
        });
    }

    private void findCurrentCourse() {
        // 模拟延迟后找到课程
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String courseName = "移动应用开发 (周一 10:00-11:40)";
                        String location = "综合楼B座305教室";
                        tvCurrentCourse.setText("📚 " + courseName + "\n📍 " + location);
                        updateStatus("找到课程，可以开始签到");
                    }
                });
            }
        }, 1000);
    }

    /**
     * 跳转到微信小程序
     * @param programId 小程序ID
     * @param programName 小程序名称
     */
    private void launchToWeChatMiniProgram(String programId, String programName) {
        updateStatus("正在跳转到" + programName + "...");

        // 检查微信是否安装
        if (!isAppInstalled(WECHAT_PACKAGE)) {
            Toast.makeText(this, "请先安装微信客户端", Toast.LENGTH_LONG).show();
            updateStatus("微信未安装");
            return;
        }

        // 执行跳转
        boolean success = WeChatManager.launchMiniProgram(this, programId);

        if (success) {
            Toast.makeText(this, "正在打开" + programName, Toast.LENGTH_SHORT).show();
            updateStatus("已发送跳转请求");
        } else {
            Toast.makeText(this, "跳转失败，请重试", Toast.LENGTH_SHORT).show();
            updateStatus("跳转失败");

            // 备选方案：直接打开微信
            openWeChatApp();
        }
    }

    /**
     * 打开学习通APP
     */
    private void openChaoxingApp() {
        updateStatus("正在打开学习通...");

        if (isAppInstalled(CHAOXING_PACKAGE)) {
            try {
                Intent intent = getPackageManager().getLaunchIntentForPackage(CHAOXING_PACKAGE);
                if (intent != null) {
                    startActivity(intent);
                    updateStatus("已打开学习通");
                } else {
                    openChaoxingMarket();
                }
            } catch (Exception e) {
                Toast.makeText(this, "打开失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                updateStatus("打开失败");
                openChaoxingMarket();
            }
        } else {
            Toast.makeText(this, "未安装学习通，正在跳转到下载页面", Toast.LENGTH_LONG).show();
            openChaoxingMarket();
        }
    }

    /**
     * 打开微信APP
     */
    private void openWeChatApp() {
        updateStatus("正在打开微信...");

        if (!isAppInstalled(WECHAT_PACKAGE)) {
            Toast.makeText(this, "请先安装微信客户端", Toast.LENGTH_LONG).show();
            updateStatus("微信未安装");
            return;
        }

        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(WECHAT_PACKAGE);
            if (intent != null) {
                startActivity(intent);
                updateStatus("已打开微信");
            } else {
                Toast.makeText(this, "无法打开微信", Toast.LENGTH_SHORT).show();
                updateStatus("打开失败");
            }
        } catch (Exception e) {
            Toast.makeText(this, "打开微信失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateStatus("打开失败");
        }
    }

    /**
     * 打开学习通应用市场页面
     */
    private void openChaoxingMarket() {
        try {
            // 尝试打开应用市场
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse("market://details?id=" + CHAOXING_PACKAGE));
            startActivity(marketIntent);
        } catch (Exception e) {
            // 备用：打开网页版
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            webIntent.setData(Uri.parse("https://app.mi.com/details?id=" + CHAOXING_PACKAGE));
            startActivity(webIntent);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 检查应用是否安装
     */
    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 更新状态显示
     */
    private void updateStatus(String message) {
        tvStatus.setText("状态: " + message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("应用已恢复");
    }
}