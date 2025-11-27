package com.example.check;

import android.util.Log;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Entity(tableName = "courses")
public class Course {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String courseName;
    public String teacher;
    public String location;
    public String time;

    // 新增字段用于表格显示
    public int dayOfWeek;     // 星期几 (1-7)
    public int startSection;  // 开始节次
    public int endSection;    // 结束节次
    public int startWeek;     // 开始周
    public int endWeek;       // 结束周
    public String weekType;   // 周类型 (单周/双周/全部)

    public Course(String courseName, String teacher, String location, String time) {
        this.courseName = courseName;
        this.teacher = teacher;
        this.location = location;
        this.time = time;

        // 解析时间信息到新字段
        parseTimeInfo(time);

        // 记录课程创建日志
        Log.d("Course", String.format("创建课程: %s - %s - %s - %s",
                courseName, teacher, location, time));
    }

    // 解析时间信息
    private void parseTimeInfo(String time) {
        try {
            // 默认值
            this.dayOfWeek = 1;
            this.startSection = 1;
            this.endSection = 2;
            this.startWeek = 1;
            this.endWeek = 16;
            this.weekType = "全部";

            if (time == null || time.isEmpty() || "未知时间".equals(time)) {
                Log.d("CourseTimeParse", "时间信息为空或未知，使用默认值");
                return;
            }

            Log.d("CourseTimeParse", "开始解析时间: " + time);

            // 改进的节次解析 - 更严格的匹配和验证
            Pattern sectionPattern = Pattern.compile("(\\d+)[-~至](\\d+)节");
            Matcher sectionMatcher = sectionPattern.matcher(time);
            if (sectionMatcher.find()) {
                this.startSection = Integer.parseInt(sectionMatcher.group(1));
                this.endSection = Integer.parseInt(sectionMatcher.group(2));

                // 验证节次范围合理性
                if (this.startSection < 1) this.startSection = 1;
                if (this.endSection > 12) this.endSection = 12;
                if (this.endSection < this.startSection) {
                    // 如果结束节次小于开始节次，调整为相同
                    this.endSection = this.startSection;
                }

                Log.d("CourseTimeParse", "解析节次: " + startSection + "-" + endSection);
            } else {
                // 尝试其他格式
                Pattern altSectionPattern = Pattern.compile("第(\\d+)[-~至](\\d+)节");
                Matcher altSectionMatcher = altSectionPattern.matcher(time);
                if (altSectionMatcher.find()) {
                    this.startSection = Integer.parseInt(altSectionMatcher.group(1));
                    this.endSection = Integer.parseInt(altSectionMatcher.group(2));

                    // 验证节次范围
                    if (this.startSection < 1) this.startSection = 1;
                    if (this.endSection > 12) this.endSection = 12;
                    if (this.endSection < this.startSection) {
                        this.endSection = this.startSection;
                    }

                    Log.d("CourseTimeParse", "解析节次(替代格式): " + startSection + "-" + endSection);
                } else {
                    // 如果都找不到，尝试只找一个节次数字
                    Pattern singleSectionPattern = Pattern.compile("(\\d+)节");
                    Matcher singleSectionMatcher = singleSectionPattern.matcher(time);
                    if (singleSectionMatcher.find()) {
                        this.startSection = Integer.parseInt(singleSectionMatcher.group(1));
                        this.endSection = this.startSection;
                        Log.d("CourseTimeParse", "解析单节次: " + startSection);
                    }
                }
            }

            // 改进的周数解析
            Pattern weekPattern = Pattern.compile("(\\d+)[-~至](\\d+)周");
            Matcher weekMatcher = weekPattern.matcher(time);
            if (weekMatcher.find()) {
                this.startWeek = Integer.parseInt(weekMatcher.group(1));
                this.endWeek = Integer.parseInt(weekMatcher.group(2));

                // 验证周数范围
                if (this.startWeek < 1) this.startWeek = 1;
                if (this.endWeek > 20) this.endWeek = 20;
                if (this.endWeek < this.startWeek) {
                    this.endWeek = this.startWeek;
                }

                Log.d("CourseTimeParse", "解析周数: " + startWeek + "-" + endWeek);
            } else {
                // 尝试单周数格式
                Pattern singleWeekPattern = Pattern.compile("(\\d+)周");
                Matcher singleWeekMatcher = singleWeekPattern.matcher(time);
                if (singleWeekMatcher.find()) {
                    this.startWeek = Integer.parseInt(singleWeekMatcher.group(1));
                    this.endWeek = this.startWeek;
                    Log.d("CourseTimeParse", "解析单周数: " + startWeek);
                }
            }

            // 严格的单双周识别逻辑 - 只有在明确标识时才识别
            this.weekType = detectWeekType(time);

            // 解析星期几 - 从原始数据中提取
            parseDayOfWeekFromOriginalData(time);

            Log.d("CourseTimeParse", "最终时间信息 - 星期:" + dayOfWeek +
                    ", 节次:" + startSection + "-" + endSection +
                    ", 周数:" + startWeek + "-" + endWeek +
                    ", 类型:" + weekType);

        } catch (Exception e) {
            Log.e("Course", "解析时间信息失败: " + time, e);
        }
    }

    // 修改方法：严格检测周类型，只有在明确标识时才识别
    // 修改方法：支持网页课表格式的单双周识别
    private String detectWeekType(String time) {
        if (time == null) return "全部";

        // 匹配括号中的单双周标识 - 如 (单) 或 (双)
        Pattern weekTypePattern = Pattern.compile("\\((单|双)\\)");
        Matcher weekTypeMatcher = weekTypePattern.matcher(time);
        if (weekTypeMatcher.find()) {
            String weekType = weekTypeMatcher.group(1);
            if ("单".equals(weekType)) {
                return "单周";
            } else if ("双".equals(weekType)) {
                return "双周";
            }
        }

        // 严格的单双周标识 - 只有在明确包含"单周"或"双周"时才识别
        if (time.contains("单周") || time.contains("(单周)")) {
            return "单周";
        } else if (time.contains("双周") || time.contains("(双周)")) {
            return "双周";
        }

        // 不进行任何推断，其他情况都返回"全部"
        return "全部";
    }

    // 从原始数据中解析星期几
    private void parseDayOfWeekFromOriginalData(String time) {
        try {
            // 尝试从时间字符串中解析星期信息
            if (time.contains("周一") || time.contains("星期一") || time.contains("周1")) {
                this.dayOfWeek = 1;
                Log.d("CourseTimeParse", "识别为星期一");
            } else if (time.contains("周二") || time.contains("星期二") || time.contains("周2")) {
                this.dayOfWeek = 2;
                Log.d("CourseTimeParse", "识别为星期二");
            } else if (time.contains("周三") || time.contains("星期三") || time.contains("周3")) {
                this.dayOfWeek = 3;
                Log.d("CourseTimeParse", "识别为星期三");
            } else if (time.contains("周四") || time.contains("星期四") || time.contains("周4")) {
                this.dayOfWeek = 4;
                Log.d("CourseTimeParse", "识别为星期四");
            } else if (time.contains("周五") || time.contains("星期五") || time.contains("周5")) {
                this.dayOfWeek = 5;
                Log.d("CourseTimeParse", "识别为星期五");
            } else if (time.contains("周六") || time.contains("星期六") || time.contains("周6")) {
                this.dayOfWeek = 6;
                Log.d("CourseTimeParse", "识别为星期六");
            } else if (time.contains("周日") || time.contains("星期日") || time.contains("星期天") || time.contains("周7")) {
                this.dayOfWeek = 7;
                Log.d("CourseTimeParse", "识别为星期日");
            } else {
                // 如果无法从文本解析，使用基于节次的推断（作为最后手段）
                inferDayOfWeekFromSection();
            }
        } catch (Exception e) {
            Log.e("Course", "解析星期几失败", e);
            inferDayOfWeekFromSection();
        }
    }

    // 基于节次推断星期几（不准确，仅作为备用）
    private void inferDayOfWeekFromSection() {
        try {
            // 这个逻辑需要根据你学校的实际课表结构调整
            // 这里只是一个示例，实际情况可能需要修改
            if (startSection >= 1 && startSection <= 2) {
                this.dayOfWeek = 1;
            } else if (startSection >= 3 && startSection <= 4) {
                this.dayOfWeek = 2;
            } else if (startSection >= 5 && startSection <= 6) {
                this.dayOfWeek = 3;
            } else if (startSection >= 7 && startSection <= 8) {
                this.dayOfWeek = 4;
            } else if (startSection >= 9 && startSection <= 10) {
                this.dayOfWeek = 5;
            } else if (startSection >= 11 && startSection <= 12) {
                this.dayOfWeek = 6;
            } else {
                this.dayOfWeek = 7;
            }

            Log.d("CourseTimeParse", "基于节次推断星期: " + dayOfWeek + " (节次:" + startSection + ")");

        } catch (Exception e) {
            Log.e("Course", "推断星期失败", e);
            this.dayOfWeek = 1; // 默认星期一
        }
    }

    // 改进的周数检查方法
    public boolean isInCurrentWeek(int currentWeek) {
        // 检查周数范围
        if (currentWeek < startWeek || currentWeek > endWeek) {
            Log.d("CourseTimeParse", "课程 " + courseName + " 不在周数范围: " + currentWeek + " vs " + startWeek + "-" + endWeek);
            return false;
        }

        // 检查单双周
        if ("单周".equals(weekType)) {
            boolean result = currentWeek % 2 == 1; // 单周：1,3,5,7...
            Log.d("CourseTimeParse", "单周课程 " + courseName + " 第" + currentWeek + "周: " + result);
            return result;
        } else if ("双周".equals(weekType)) {
            boolean result = currentWeek % 2 == 0; // 双周：2,4,6,8...
            Log.d("CourseTimeParse", "双周课程 " + courseName + " 第" + currentWeek + "周: " + result);
            return result;
        }

        Log.d("CourseTimeParse", "全部周课程 " + courseName + " 第" + currentWeek + "周: 匹配");
        return true; // 全部周
    }

    @Override
    public String toString() {
        return String.format("%s\n星期%d %s-%s节 %d-%d周 %s\n%s %s",
                courseName, dayOfWeek, startSection, endSection,
                startWeek, endWeek, weekType, location, teacher);
    }

    // 获取简化的显示信息（用于列表项）
    public String getDisplayInfo() {
        return String.format("%s - 星期%d - %s", courseName, dayOfWeek, location);
    }

    // 获取详细显示信息（用于列表显示）
    public String getDetailedInfo() {
        // 构建时间信息，只有单双周课程才显示周类型
        String timeInfo;
        if ("单周".equals(weekType) || "双周".equals(weekType)) {
            timeInfo = String.format("星期%d %d-%d节 %d-%d周 %s",
                    dayOfWeek, startSection, endSection, startWeek, endWeek, weekType);
        } else {
            timeInfo = String.format("星期%d %d-%d节 %d-%d周",
                    dayOfWeek, startSection, endSection, startWeek, endWeek);
        }

        return String.format("教师: %s | 地点: %s\n时间: %s",
                teacher, location, timeInfo);
    }
}