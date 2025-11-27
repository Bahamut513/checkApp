package com.example.check;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnCourseManagement = findViewById(R.id.btn_course_management);
        Button btnCourseTable = findViewById(R.id.btn_course_table); // 新增课程表按钮
        Button btnAutoSign = findViewById(R.id.btn_auto_sign);
        tvStatus = findViewById(R.id.tv_status);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
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