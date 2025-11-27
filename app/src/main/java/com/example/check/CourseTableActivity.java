package com.example.check;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseTableActivity extends AppCompatActivity {

    private TableLayout tableCourse;
    private TextView tvCurrentWeek;
    private Spinner spinnerWeek;
    private Button btnReselectWeek;
    private Button btnPrevWeek;
    private Button btnNextWeek;
    private GestureDetector gestureDetector;

    private int currentWeek = 1; // 当前周数，默认第1周
    private List<Course> allCourses;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CourseTablePrefs";
    private static final String KEY_SELECTED_WEEK = "selected_week";
    private static final String KEY_LAST_ASK_DATE = "last_ask_date";

    // 节次时间对应表
    private final String[] sectionTimes = {
            "08:30-09:15", "09:20-10:25", "10:25-11:10", "11:15-12:00",
            "14:00-14:45", "14:50-15:35", "15:55-16:40", "16:45-17:30",
            "19:00-19:45", "19:50-20:35", "20:45-21:30", "21:40-22:25"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupGestureDetector();
        setupWeekSpinner();
        checkAndAskWeek();
        loadCourses();
    }

    private void initViews() {
        tableCourse = findViewById(R.id.table_course);
        tvCurrentWeek = findViewById(R.id.tv_current_week);
        spinnerWeek = findViewById(R.id.spinner_week);
        btnReselectWeek = findViewById(R.id.btn_reselect_week);
        btnPrevWeek = findViewById(R.id.btn_prev_week);
        btnNextWeek = findViewById(R.id.btn_next_week);

        // 重新选择周数按钮点击事件
        btnReselectWeek.setOnClickListener(v -> {
            // 使用SimpleDateFormat直接获取中文星期几
            SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.CHINA);
            String today = dayFormat.format(new Date());
            askCurrentWeek(today);
        });

        // 上一周按钮点击事件
        btnPrevWeek.setOnClickListener(v -> {
            switchToPreviousWeek();
        });

        // 下一周按钮点击事件
        btnNextWeek.setOnClickListener(v -> {
            switchToNextWeek();
        });

        // 从SharedPreferences加载上次选择的周数
        currentWeek = sharedPreferences.getInt(KEY_SELECTED_WEEK, 1);
        updateWeekDisplay();
        updateButtonStates();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();

                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                // 向右滑动 - 上一周
                                switchToPreviousWeek();
                            } else {
                                // 向左滑动 - 下一周
                                switchToNextWeek();
                            }
                            result = true;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        });

        // 为表格设置触摸监听器
        tableCourse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        // 为ScrollView设置触摸监听器，确保滑动时也能检测手势
        findViewById(R.id.scroll_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false; // 返回false让ScrollView继续处理滚动
            }
        });
    }

    private void switchToPreviousWeek() {
        if (currentWeek > 1) {
            setCurrentWeek(currentWeek - 1);
            showSwipeToast("← 切换到第" + currentWeek + "周");
        } else {
            Toast.makeText(this, "已经是第一周了", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToNextWeek() {
        if (currentWeek < 20) {
            setCurrentWeek(currentWeek + 1);
            showSwipeToast("切换到第" + currentWeek + "周 →");
        } else {
            Toast.makeText(this, "已经是最后一周了", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSwipeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        // 更新上一周按钮状态
        btnPrevWeek.setEnabled(currentWeek > 1);

        // 更新下一周按钮状态
        btnNextWeek.setEnabled(currentWeek < 20);
    }

    // 重写onTouchEvent以处理全屏的滑动手势
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void checkAndAskWeek() {
        // 使用SimpleDateFormat直接获取中文星期几
        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.CHINA);
        String today = dayFormat.format(new Date());

        // 检查是否需要询问周数（每天只问一次）
        String lastAskDate = sharedPreferences.getString(KEY_LAST_ASK_DATE, "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

        if (!currentDate.equals(lastAskDate)) {
            // 今天还没有询问过，显示对话框
            askCurrentWeek(today);
            // 保存询问日期
            sharedPreferences.edit().putString(KEY_LAST_ASK_DATE, currentDate).apply();
        } else {
            // 今天已经询问过，使用上次选择的周数
            spinnerWeek.setSelection(currentWeek - 1);
        }
    }

    private void askCurrentWeek(String today) {
        // 创建输入框
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("请输入周数 (1-20)");
        input.setText(String.valueOf(currentWeek));

        // 显示输入对话框
        new AlertDialog.Builder(this)
                .setTitle("周数确认")
                .setMessage("今天是 " + today +
                        "\n请问这周是第几周的课程？")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            try {
                                int week = Integer.parseInt(inputText);
                                if (week >= 1 && week <= 20) {
                                    setCurrentWeek(week);
                                } else {
                                    Toast.makeText(CourseTableActivity.this,
                                            "请输入1-20之间的数字", Toast.LENGTH_SHORT).show();
                                    askCurrentWeek(today); // 重新询问
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(CourseTableActivity.this,
                                        "请输入有效的数字", Toast.LENGTH_SHORT).show();
                                askCurrentWeek(today); // 重新询问
                            }
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户取消，使用当前周数
                        spinnerWeek.setSelection(currentWeek - 1);
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void setCurrentWeek(int week) {
        currentWeek = week;
        // 保存到SharedPreferences
        sharedPreferences.edit().putInt(KEY_SELECTED_WEEK, week).apply();
        updateWeekDisplay();
        spinnerWeek.setSelection(week - 1); // Spinner的position从0开始
        updateButtonStates(); // 更新按钮状态

        // 如果课程数据已经加载，刷新表格（包括日期）
        if (allCourses != null) {
            refreshCourseTable();
        }
    }

    private void setupWeekSpinner() {
        // 创建1-20周的列表
        List<String> weekList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            weekList.add("第" + i + "周");
        }

        // 创建适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                weekList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器
        spinnerWeek.setAdapter(adapter);

        // 设置默认选中当前周数
        spinnerWeek.setSelection(currentWeek - 1);

        // 设置选择监听器
        spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedWeek = position + 1; // position从0开始，周数从1开始
                if (selectedWeek != currentWeek) {
                    setCurrentWeek(selectedWeek);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 什么都不做
            }
        });
    }

    private void updateWeekDisplay() {
        // 计算当前周有多少门课程
        int courseCount = 0;
        if (allCourses != null) {
            for (Course course : allCourses) {
                if (course.isInCurrentWeek(currentWeek)) {
                    courseCount++;
                }
            }
        }

        if (courseCount > 0) {
            tvCurrentWeek.setText("第" + currentWeek + "周 (" + courseCount + "门课)");
        } else {
            tvCurrentWeek.setText("第" + currentWeek + "周 (无课程)");
        }
    }

    private void loadCourses() {
        CourseManager.getInstance(this).getAllCourses(new CourseManager.DatabaseOperationCallback() {
            @Override
            public void onOperationCompleted(boolean success) {}

            @Override
            public void onCoursesLoaded(List<Course> courses) {
                runOnUiThread(() -> {
                    allCourses = courses;
                    if (courses.isEmpty()) {
                        Toast.makeText(CourseTableActivity.this,
                                "没有课程数据，请先导入课表", Toast.LENGTH_LONG).show();
                    } else {
                        createCourseTable();
                        refreshCourseTable();
                        // 更新周数显示（包含课程数量）
                        updateWeekDisplay();
                        // 更新按钮状态
                        updateButtonStates();
                    }
                });
            }
        });
    }

    // 其余方法保持不变（createCourseTable, calculateWeekDates, refreshCourseTable, updateDateRow, placeCourseInTable, getCourseColor, createTableCell, onCourseCellClick, showCourseDetailDialog）

    private void createCourseTable() {
        // 清空表格
        tableCourse.removeAllViews();

        // 第一行：日期显示（每个星期几上方显示对应的日期）
        TableRow dateRow = new TableRow(this);
        dateRow.setBackgroundColor(0xFFE3F2FD);

        // 左上角空白单元格
        TextView emptyDateCell = createTableCell("", 10, true);
        emptyDateCell.setBackgroundColor(0xFFE3F2FD);
        dateRow.addView(emptyDateCell);

        // 为每个星期几计算并显示对应的日期
        String[] dates = calculateWeekDates(currentWeek);
        for (int i = 0; i < 7; i++) {
            TextView dateCell = createTableCell(dates[i], 10, true);
            dateCell.setBackgroundColor(0xFFBBDEFB);
            dateRow.addView(dateCell);
        }

        tableCourse.addView(dateRow);

        // 第二行：星期标题
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(0xFFE3F2FD);

        // 添加左上角空白单元格
        TextView emptyCell = createTableCell("", 12, true);
        emptyCell.setBackgroundColor(0xFFE3F2FD);
        headerRow.addView(emptyCell);

        // 添加星期标题
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (String day : weekDays) {
            TextView cell = createTableCell(day, 12, true);
            cell.setBackgroundColor(0xFFBBDEFB);
            headerRow.addView(cell);
        }

        tableCourse.addView(headerRow);

        // 创建节次行（12节课）
        for (int section = 1; section <= 12; section++) {
            TableRow sectionRow = new TableRow(this);

            // 节次标题
            TextView sectionCell = createTableCell("第" + section + "节\n" + sectionTimes[section-1], 10, true);
            sectionCell.setBackgroundColor(0xFFE3F2FD);
            sectionRow.addView(sectionCell);

            // 每天的课程单元格
            for (int day = 1; day <= 7; day++) {
                TextView cell = createTableCell("", 10, false);
                cell.setTag(new int[]{day, section}); // 存储位置信息
                cell.setOnClickListener(this::onCourseCellClick);
                sectionRow.addView(cell);
            }

            tableCourse.addView(sectionRow);
        }
    }

    /**
     * 计算指定周数的日期
     * @param week 周数（1-20）
     * @return 包含7个日期的数组，对应周一到周日
     */
    private String[] calculateWeekDates(int week) {
        String[] dates = new String[7];
        Calendar calendar = Calendar.getInstance(Locale.CHINA);

        // 设置到当前学期的第一周周一（这里假设第1周周一是9月1日，您可以根据实际情况调整）
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // 找到第1周的周一
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 跳转到指定周数的周一
        calendar.add(Calendar.WEEK_OF_YEAR, week - 1);

        // 计算一周的日期（周一到周日）
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.CHINA);
        for (int i = 0; i < 7; i++) {
            dates[i] = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    private void refreshCourseTable() {
        if (allCourses == null) return;

        // 更新日期行
        updateDateRow();

        // 清空所有课程单元格内容（从第3行开始，跳过日期行和星期行）
        for (int i = 2; i < tableCourse.getChildCount(); i++) {
            TableRow row = (TableRow) tableCourse.getChildAt(i);
            for (int j = 1; j < row.getChildCount(); j++) { // 从1开始跳过节次列
                TextView cell = (TextView) row.getChildAt(j);
                cell.setText("");
                cell.setBackgroundColor(0xFFFFFFFF);
                cell.setTag(new int[]{j, i-1}); // 重置位置信息，注意行索引调整
            }
        }

        // 填充当前周的课程
        for (Course course : allCourses) {
            if (course.isInCurrentWeek(currentWeek)) {
                placeCourseInTable(course);
            }
        }
    }

    /**
     * 更新日期行
     */
    private void updateDateRow() {
        if (tableCourse.getChildCount() > 0) {
            TableRow dateRow = (TableRow) tableCourse.getChildAt(0);
            String[] dates = calculateWeekDates(currentWeek);

            // 更新每个日期单元格
            for (int i = 1; i <= 7; i++) { // 从1开始，跳过第一个空白单元格
                TextView dateCell = (TextView) dateRow.getChildAt(i);
                if (dateCell != null) {
                    dateCell.setText(dates[i-1]);
                }
            }
        }
    }

    private void placeCourseInTable(Course course) {
        int day = course.dayOfWeek;
        int startSection = course.startSection;
        int endSection = course.endSection;

        // 确保节次在有效范围内
        if (startSection < 1 || startSection > 12 || endSection < startSection || endSection > 12) {
            return;
        }

        // 在对应的节次行中放置课程（注意：表格前2行是日期和星期，所以课程从第3行开始）
        for (int section = startSection; section <= endSection; section++) {
            TableRow row = (TableRow) tableCourse.getChildAt(section + 1); // +1 因为前2行是日期和星期
            if (row != null && day >= 1 && day <= 7) {
                TextView cell = (TextView) row.getChildAt(day); // 第0列是节次标题
                if (cell != null) {
                    if (section == startSection) {
                        // 只在开始节次显示完整课程信息
                        cell.setText(course.courseName + "\n" + course.location);
                        cell.setBackgroundColor(getCourseColor(course.courseName));
                        cell.setTag(course); // 存储课程对象
                    } else {
                        // 后续节次显示合并标记
                        cell.setText("↑");
                        cell.setBackgroundColor(getCourseColor(course.courseName));
                        cell.setTag(course);
                    }
                }
            }
        }
    }

    private int getCourseColor(String courseName) {
        // 使用预定义的柔和颜色数组，避免红色等深色背景
        int[] softColors = {
                0xFFE8F5E8, // 浅绿色
                0xFFE3F2FD, // 浅蓝色
                0xFFF3E5F5, // 浅紫色
                0xFFE0F2F1, // 浅青色
                0xFFFFF8E1, // 浅黄色
                0xFFFBE9E7, // 浅橙色
                0xFFE8EAF6, // 浅靛蓝色
                0xFFFCE4EC, // 浅粉色
                0xFFE0F7FA, // 更浅的青色
                0xFFF1F8E9, // 更浅的绿色
                0xFFEDE7F6, // 更浅的紫色
                0xFFFFF3E0  // 更浅的橙色
        };

        // 根据课程名称哈希值选择颜色
        int hash = Math.abs(courseName.hashCode());
        int colorIndex = hash % softColors.length;
        return softColors[colorIndex];
    }

    private TextView createTableCell(String text, int textSize, boolean isHeader) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(1, 1, 1, 1);
        textView.setLayoutParams(params);

        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(4, 8, 4, 8);

        if (isHeader) {
            textView.setBackgroundColor(0xFFBBDEFB);
            textView.setTextColor(0xFF000000);
        } else {
            textView.setBackgroundColor(0xFFFFFFFF);
            textView.setTextColor(0xFF333333);
            textView.setMinHeight(80);
        }

        textView.setSingleLine(false);
        textView.setMaxLines(3);

        return textView;
    }

    private void onCourseCellClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof Course) {
            Course course = (Course) tag;
            showCourseDetailDialog(course);
        } else if (tag instanceof int[]) {
            // 空单元格点击，显示当前单元格信息
            int[] position = (int[]) tag;
            int day = position[0];
            int section = position[1];

            String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            String message = String.format("%s 第%d节\n时间: %s",
                    weekDays[day-1], section, sectionTimes[section-1]);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showCourseDetailDialog(Course course) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_course_detail, null);

        TextView tvCourseName = dialogView.findViewById(R.id.tv_course_name);
        TextView tvTeacher = dialogView.findViewById(R.id.tv_teacher);
        TextView tvLocation = dialogView.findViewById(R.id.tv_location);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvWeeks = dialogView.findViewById(R.id.tv_weeks);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        tvCourseName.setText(course.courseName);
        tvTeacher.setText(course.teacher);
        tvLocation.setText(course.location);
        tvTime.setText(String.format("星期%d %d-%d节", course.dayOfWeek, course.startSection, course.endSection));

        // 构建周数信息
        String weekInfo = course.startWeek + "-" + course.endWeek + "周";
        if ("单周".equals(course.weekType)) {
            weekInfo += " (单周)";
        } else if ("双周".equals(course.weekType)) {
            weekInfo += " (双周)";
        }
        tvWeeks.setText(weekInfo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载课程数据
        loadCourses();
    }
}