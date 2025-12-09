package com.example.check;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout layoutButtons;
    private Button btnIdentifyCourses;
    private TextView tvGuide;

    private boolean hasShownLoginSuccess = false;
    private boolean hasShownCoursePageHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 添加登录检查
        if (!TokenManager.isLoggedIn(this)) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        webView = findViewById(R.id.webView);
        layoutButtons = findViewById(R.id.layoutButtons);
        btnIdentifyCourses = findViewById(R.id.btnIdentifyCourses);
        tvGuide = findViewById(R.id.tvGuide);

        setupWebView();
        setupButtonListeners();

        // 加载统一身份认证登录页面
        webView.loadUrl("https://authserver.swun.edu.cn/authserver/login?service=http%3A%2F%2Fehall.swun.edu.cn%2Flogin");
    }


    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        webView.getSettings().setUserAgentString(desktopUserAgent);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d("WebView", "页面加载完成: " + url);

                runOnUiThread(() -> {
                    layoutButtons.setVisibility(View.VISIBLE);

                    if (url.contains("authserver")) {
                        tvGuide.setText("请在页面中完成登录");
                        btnIdentifyCourses.setEnabled(false);
                        hasShownLoginSuccess = false;
                        hasShownCoursePageHint = false;

                    } else if (url.contains("ehall") && !hasShownLoginSuccess) {
                        tvGuide.setText("登录成功！请手动导航到课表页面");
                        btnIdentifyCourses.setEnabled(false);
                        hasShownLoginSuccess = true;

                        Toast.makeText(LoginActivity.this,
                                "登录成功！请在页面中手动导航到课表页面",
                                Toast.LENGTH_LONG).show();

                    } else if ((url.contains("kbcx") || url.contains("kbxx") || url.contains("xskbcx")) && !hasShownCoursePageHint) {
                        tvGuide.setText("已进入课表页面，请点击下方'识别课表'按钮");
                        btnIdentifyCourses.setEnabled(true);
                        hasShownCoursePageHint = true;

                        Toast.makeText(LoginActivity.this,
                                "已进入课表页面，可以点击'识别课表'按钮",
                                Toast.LENGTH_LONG).show();
                    } else if (url.contains("ehall")) {
                        tvGuide.setText("登录成功！请在页面中手动导航到课表页面");
                        btnIdentifyCourses.setEnabled(false);
                    }
                });
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void setupButtonListeners() {
        btnIdentifyCourses.setOnClickListener(v -> {
            String currentUrl = webView.getUrl();
            if (currentUrl != null && (currentUrl.contains("kbcx") || currentUrl.contains("kbxx") || currentUrl.contains("xskbcx"))) {
                getPageContentForParsing();
            } else {
                Toast.makeText(LoginActivity.this,
                        "请先完成登录并手动导航到课表页面\n当前URL: " + currentUrl,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPageContentForParsing() {
        webView.evaluateJavascript(
                "(function() { " +
                        "try { " +
                        "  return document.documentElement.outerHTML; " +
                        "} catch(e) { " +
                        "  return 'error:' + e.message; " +
                        "}" +
                        "})();",
                html -> {
                    if (html != null && !html.startsWith("error:")) {
                        String cleanHtml = html.replace("\\\"", "\"")
                                .replace("\\n", "\n")
                                .replace("\\/", "/")
                                .replace("\\u003C", "<")
                                .replace("\\u003E", ">");

                        Log.d("HTML", "获取到HTML长度: " + cleanHtml.length());

                        if (cleanHtml.length() > 1000) {
                            parseCourseTable(cleanHtml);
                        } else {
                            Toast.makeText(LoginActivity.this, "获取的HTML内容过短，请确认页面加载完成", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "无法获取页面HTML", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void parseCourseTable(String html) {
        new Thread(() -> {
            try {
                CourseParser.ParseResult parseResult = CourseParser.parseCourseTable(html, "用户课表");

                if (parseResult.isSuccess() && !parseResult.getCourses().isEmpty()) {
                    // 保存到本地数据库
                    CourseManager.getInstance(LoginActivity.this).clearCourses();
                    CourseManager.getInstance(LoginActivity.this).addAllCourses(parseResult.getCourses());

                    // 验证保存结果
                    Thread.sleep(1000);
                    List<Course> savedCourses = CourseManager.getInstance(LoginActivity.this).getAllCourses();

                    runOnUiThread(() -> {
                        showParseResult(parseResult.getCourses().size(), savedCourses.size(), parseResult.getMessage());

                        // 新增：同步到服务器
                        if (TokenManager.isLoggedIn(LoginActivity.this)) {
                            syncCoursesToServer(savedCourses);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this,
                                parseResult.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                Log.e("LoginActivity", "解析失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "解析失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // 新增：同步课程到服务器
    private void syncCoursesToServer(List<Course> courses) {
        CourseManager.getInstance(this).syncCoursesToServer(this, new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(LoginActivity.this, "课程已同步到服务器", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "课程同步失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCoursesLoaded(List<Course> courses) {
                // 不需要实现
            }
        });
    }

    private void showParseResult(int parsedCount, int savedCount, String message) {
        StringBuilder result = new StringBuilder();
        result.append("🎉 识别成功！\n\n");
        result.append("📊 ").append(message).append("\n");
        result.append("💾 已保存: ").append(savedCount).append(" 门课程\n\n");

        if (savedCount > 0) {
            CourseManager.getInstance(this).getAllCourses(new CourseManager.DatabaseOperationCallback() {
                @Override
                public void onOperationCompleted(boolean success) {}

                @Override
                public void onCoursesLoaded(List<Course> courses) {
                    runOnUiThread(() -> {
                        result.append("📚 课程列表:\n");
                        int displayCount = Math.min(courses.size(), 5);
                        for (int i = 0; i < displayCount; i++) {
                            Course course = courses.get(i);
                            result.append("• ").append(course.getDisplayInfo()).append("\n");
                        }
                        if (courses.size() > 5) {
                            result.append("... 等").append(courses.size()).append("门课程");
                        }


                        // 显示详细结果
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("识别结果")
                                .setMessage(result.toString())
                                .setPositiveButton("确定", (dialog, which) -> finish())
                                .show();
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}