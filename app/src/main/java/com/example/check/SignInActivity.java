package com.example.check;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private TextView tvCurrentCourse;
    private Button btnStartSign;

    // 小程序原始ID
    private static final String MINI_PROGRAM_ORIGINAL_ID = "gh_d2d41b77389b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        tvCurrentCourse = findViewById(R.id.tv_current_course);
        btnStartSign = findViewById(R.id.btn_start_sign);

        findCurrentCourse();
        btnStartSign.setOnClickListener(v -> startWechatMiniProgram());
    }

    private void findCurrentCourse() {
        CourseManager.getInstance(this).getCurrentCourse(new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {}

            @Override
            public void onCoursesLoaded(List<Course> courses) {
                runOnUiThread(() -> {
                    if (!courses.isEmpty()) {
                        Course currentCourse = courses.get(0);
                        tvCurrentCourse.setText("当前课程: " + currentCourse.courseName +
                                "\n地点: " + currentCourse.location +
                                "\n时间: " + currentCourse.time);
                        btnStartSign.setEnabled(true);
                    } else {
                        tvCurrentCourse.setText("当前时间段没有课程");
                        btnStartSign.setEnabled(false);
                    }
                });
            }
        });
    }

    private void startWechatMiniProgram() {
        Log.d("WechatMiniProgram", "开始启动微信小程序...");

        // 检查微信是否安装
        if (!isWechatInstalled()) {
            Toast.makeText(this, "请先安装微信", Toast.LENGTH_SHORT).show();
            return;
        }

        // 方法1: 使用标准URL Scheme跳转
        if (tryStandardJump()) {
            return;
        }

        // 方法2: 直接启动微信
        launchWechatDirectly();
    }

    /**
     * 检查微信是否安装
     */
    private boolean isWechatInstalled() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            return intent != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 方法1: 使用标准URL Scheme跳转
     */
    private boolean tryStandardJump() {
        try {
            Log.d("WechatMiniProgram", "尝试标准URL Scheme跳转...");

            // 扩展的跳转格式列表
            String[] urlFormats = {
                    // 格式1: 使用username参数（微信官方推荐）
                    "weixin://dl/business/?username=gh_d2d41b77389b",
                    "weixin://dl/business/?t=" + System.currentTimeMillis() + "&username=gh_d2d41b77389b",

                    // 格式2: 使用appid参数
                    "weixin://dl/business/?appid=gh_d2d41b77389b",
                    "weixin://dl/business/?t=" + System.currentTimeMillis() + "&appid=gh_d2d41b77389b",

                    // 格式3: 最简格式
                    "weixin://dl/business/gh_d2d41b77389b",

                    // 格式4: 使用startapp
                    "weixin://dl/startapp?userName=gh_d2d41b77389b",
                    "weixin://dl/startapp?userName=gh_d2d41b77389b&path=pages/index/index",

                    // 格式5: 使用jumpWxa
                    "weixin://jumpWxa/?userName=gh_d2d41b77389b",

                    // 格式6: 带路径参数
                    "weixin://dl/business/?username=gh_d2d41b77389b&path=pages/index/index",
                    "weixin://dl/business/?appid=gh_d2d41b77389b&path=pages/index/index",
                    "weixin://dl/business/?username=gh_d2d41b77389b&path=pages/home/home",

                    // 格式7: 其他可能的参数名
                    "weixin://dl/business/?miniProgramId=gh_d2d41b77389b",
                    "weixin://dl/business/?target=gh_d2d41b77389b",
                    "weixin://dl/business/?id=gh_d2d41b77389b"
            };

            for (int i = 0; i < urlFormats.length; i++) {
                String url = urlFormats[i];
                Log.d("WechatMiniProgram", "测试URL [" + (i+1) + "/" + urlFormats.length + "]: " + url);

                if (trySingleUrl(url)) {
                    // 记录成功的URL格式
                    Log.d("WechatMiniProgram", "🎉 找到可用的URL格式: " + url);
                    return true;
                }

                // 短暂延迟
                try { Thread.sleep(300); } catch (InterruptedException e) { break; }
            }

            Log.d("WechatMiniProgram", "所有URL格式都失败了");
            showFormatTestDialog();

        } catch (Exception e) {
            Log.e("WechatMiniProgram", "标准跳转失败", e);
        }
        return false;
    }

    private boolean trySingleUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.tencent.mm"); // 指定微信包名
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Log.d("WechatMiniProgram", "✅ URL跳转成功: " + url);
                Toast.makeText(this, "正在跳转到小程序...", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Log.d("WechatMiniProgram", "❌ 没有应用可以处理: " + url);
            }
        } catch (Exception e) {
            Log.e("WechatMiniProgram", "❌ URL跳转失败: " + url, e);
        }
        return false;
    }

    /**
     * 显示格式测试对话框
     */
    private void showFormatTestDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("跳转测试")
                .setMessage("自动跳转失败，可能的原因：\n\n" +
                        "1. 小程序ID不正确\n" +
                        "2. 小程序未发布或不可用\n" +
                        "3. 需要特定的跳转参数\n\n" +
                        "是否尝试手动获取正确的小程序信息？")
                .setPositiveButton("手动操作", (dialog, which) -> {
                    launchWechatDirectly();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 方法2: 直接启动微信
     */
    private void launchWechatDirectly() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            if (intent != null) {
                startActivity(intent);
                showWechatGuide();
            } else {
                Toast.makeText(this, "请先安装微信", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "启动微信失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWechatGuide() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("小程序签到指引")
                .setMessage("微信已启动，请按以下步骤操作：\n\n" +
                        "1. 点击右上角搜索图标\n" +
                        "2. 搜索：西南民大课堂考勤系统\n" +
                        "3. 点击第一个搜索结果\n" +
                        "4. 在小程序中完成签到\n\n" +
                        "完成后返回此应用")
                .setPositiveButton("明白了", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("SignIn", "用户返回应用");
    }
}