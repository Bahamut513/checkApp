package com.example.check;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.widget.Toast;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnClassMemory;  // 课堂记忆
    private Button btnUploadFile;   // 上传文件

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 首先检查登录状态
        if (!TokenManager.isLoggedIn(this)) {
            // 未登录，跳转到认证页面
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish(); // 结束当前Activity，避免用户按返回键回到这里
            return;
        }

        // 已登录，正常设置布局
        setContentView(R.layout.activity_main); // 使用您原有的主界面布局

        // 初始化界面组件
        initViews();

        // 设置点击事件
        setClickListeners();

        // 显示欢迎信息
        String username = TokenManager.getUsername(this);
        Toast.makeText(this, "欢迎回来，" + username, Toast.LENGTH_SHORT).show();

        checkAndSyncCourses();  // 替换原来的 updateStatus() 调用
    }

    private void initViews() {
        // 这里只是找到视图，不需要保存为变量
        tvStatus = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        btnClassMemory = findViewById(R.id.btn_class_memory);
        btnUploadFile = findViewById(R.id.btn_upload_file);
    }

    private void setClickListeners() {
        // 登录学校系统
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // 课程列表
        findViewById(R.id.btn_course_management).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CourseListActivity.class);
            startActivity(intent);
        });

        // 课程表 - 新增功能
        findViewById(R.id.btn_course_table).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CourseTableActivity.class);
            startActivity(intent);
        });

        // 课堂签到
        findViewById(R.id.btn_auto_sign).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // 新增按钮功能
        btnClassMemory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClassMemoryActivity.class);
            startActivity(intent);
        });

        btnUploadFile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadFileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 再次检查登录状态
        if (!TokenManager.isLoggedIn(this)) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
        updateStatus();
    }


    // 新增方法：智能检查课程同步（放在 updateStatus() 方法前面）
    private void checkAndSyncCourses() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvStatus != null) {
            tvStatus.setText("正在检查课程数据...");
        }

        // 1. 先检查本地是否有课程
        CourseManager.getInstance(this).getAllCourses(new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {}

            @Override
            public void onCoursesLoaded(List<Course> localCourses) {
                runOnUiThread(() -> {
                    if (localCourses != null && !localCourses.isEmpty()) {
                        // 本地有课程，不需要从服务器加载
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (tvStatus != null) {
                            tvStatus.setText("已加载 " + localCourses.size() + " 门课程");
                        }
                    } else {
                        // 本地没有课程，尝试从服务器加载
                        loadCoursesFromServer();
                    }
                });
            }
        });
    }

    // 新增方法：从服务器加载课程（只有本地没有时才调用）
    private void loadCoursesFromServer() {
        if (tvStatus != null) {
            tvStatus.setText("正在从云端同步课程...");
        }

        CourseManager.getInstance(this).loadCoursesFromServer(this, new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {}

            @Override
            public void onCoursesLoaded(List<Course> serverCourses) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (serverCourses != null && !serverCourses.isEmpty()) {
                        if (tvStatus != null) {
                            tvStatus.setText("已同步 " + serverCourses.size() + " 门课程");
                        }
                        Toast.makeText(MainActivity.this,
                                "已从云端加载课程", Toast.LENGTH_SHORT).show();
                    } else {
                        // 服务器也没有课程，是新用户
                        if (tvStatus != null) {
                            tvStatus.setText("请先导入课表");
                        }
                        // 可选：给新用户一个友好提示
                        // Toast.makeText(MainActivity.this,
                        //     "欢迎使用，请点击'登录学校系统'导入课表", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateStatus() {
        // 使用异步方式检查是否有课程
        CourseManager.getInstance(this).hasCourses(new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {
                // 这个方法在这里用不到
            }

            @Override
            public void onCoursesLoaded(List<Course> courses) {
                runOnUiThread(() -> {
                    boolean hasCourses = !courses.isEmpty();
                    if (hasCourses) {
                        tvStatus.setText("已导入课表，可以开始签到");
                    } else {
                        tvStatus.setText("请先登录学校系统并导入课表");
                    }
                });
            }
        });
    }
}