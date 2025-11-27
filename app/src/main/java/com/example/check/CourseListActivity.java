package com.example.check;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<Course> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        listView = findViewById(R.id.listView);
        loadCourses();
    }

    private void loadCourses() {
        CourseManager.getInstance(this).getAllCourses(new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {}

            @Override
            public void onCoursesLoaded(List<Course> courses) {
                runOnUiThread(() -> {
                    if (courses.isEmpty()) {
                        Toast.makeText(CourseListActivity.this, "没有找到课程数据", Toast.LENGTH_SHORT).show();
                        // 设置空适配器
                        adapter = new ArrayAdapter<>(CourseListActivity.this, android.R.layout.simple_list_item_1);
                        listView.setAdapter(adapter);
                    } else {
                        // 创建自定义适配器来显示详细信息
                        adapter = new ArrayAdapter<Course>(
                                CourseListActivity.this,
                                android.R.layout.simple_list_item_2,
                                android.R.id.text1,
                                courses
                        ) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text1 = view.findViewById(android.R.id.text1);
                                TextView text2 = view.findViewById(android.R.id.text2);

                                Course course = courses.get(position);

                                // 主标题：课程名称
                                text1.setText(course.courseName);

                                // 副标题：详细信息
                                text2.setText(course.getDetailedInfo());

                                return view;
                            }
                        };

                        listView.setAdapter(adapter);
                        Toast.makeText(CourseListActivity.this,
                                "加载了 " + courses.size() + " 门课程",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面时重新加载数据
        loadCourses();
    }
}