package com.example.check;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseParser {
    private static final String TAG = "CourseParser";

    // 课程类型标记
    private static final String[] COURSE_TYPE_MARKS = {"★", "☆", "■", "●", "◆", "▲"};

    // 排除的标题关键词
    private static final String[] EXCLUDED_HEADER_KEYWORDS = {
            "学年第", "学期", "的课表", "学号：", "时间段", "节次",
            "课程表", "课表", "教学周", "红色斜体", "蓝色为已选上"
    };

    /**
     * 主解析方法 - 面向用户的稳定版本
     */
    public static ParseResult parseCourseTable(String html, String studentInfo) {
        ParseResult result = new ParseResult();
        try {
            Log.d(TAG, "开始解析课表，学生信息: " + studentInfo);

            Document doc = Jsoup.parse(html);
            List<Course> courses = new ArrayList<>();
            Set<String> uniqueCourses = new HashSet<>();

            // 多种方式查找课程元素
            Elements courseElements = findCourseElementsComprehensive(doc);
            Log.d(TAG, "找到潜在课程元素数量: " + courseElements.size());

            for (Element element : courseElements) {
                Course course = extractCompleteCourse(element);
                if (course != null && isValidCompleteCourse(course)) {
                    String courseKey = generateCourseKey(course);
                    if (!uniqueCourses.contains(courseKey)) {
                        uniqueCourses.add(courseKey);
                        courses.add(course);
                        Log.d(TAG, "✅ 成功解析课程: " + course.getDisplayInfo());
                    }
                }
            }

            result.setCourses(courses);
            result.setSuccess(true);
            result.setMessage("成功解析 " + courses.size() + " 门课程");

            Log.d(TAG, "解析完成: " + result.getMessage());

        } catch (Exception e) {
            Log.e(TAG, "解析课表失败", e);
            result.setSuccess(false);
            result.setMessage("解析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 综合查找课程元素
     */
    private static Elements findCourseElementsComprehensive(Document doc) {
        Elements elements = new Elements();

        // 优先级1: 包含完整课程信息的元素
        String[] coursePatterns = {
                "div:matches((\\d+-\\d+节).*(\\d+-\\d+周).*[A-Z]-\\d+)",
                "td:matches((\\d+-\\d+节).*(\\d+-\\d+周).*[A-Z]-\\d+)",
                "[class*='course']:matches((\\d+-\\d+节).*(\\d+-\\d+周))",
                ".timetable_course", ".course_item", ".kbcontent"
        };

        for (String pattern : coursePatterns) {
            elements.addAll(doc.select(pattern));
            if (!elements.isEmpty()) break;
        }

        // 优先级2: 包含关键信息的元素
        if (elements.isEmpty()) {
            elements.addAll(doc.select("div:matches((\\d+-\\d+节).*[\\u4e00-\\u9fa5]{4,})"));
            elements.addAll(doc.select("td:matches((\\d+-\\d+节).*[\\u4e00-\\u9fa5]{4,})"));
        }

        // 优先级3: 通用课程元素
        if (elements.isEmpty()) {
            elements.addAll(doc.select(".timetable_con, [class*='timetable'], [class*='course']"));
        }

        return elements;
    }

    /**
     * 提取完整课程信息
     */
    private static Course extractCompleteCourse(Element element) {
        try {
            String text = element.text().trim();

            // 跳过标题和无效内容
            if (isHeaderOrInvalidContent(text)) {
                return null;
            }

            // 提取各项信息
            String courseName = extractRobustCourseName(text);
            String teacher = extractRobustTeacher(text);
            String location = extractRobustLocation(text);
            String time = extractRobustTime(text);
            int dayOfWeek = extractRobustDayOfWeek(element, text);

            // 验证必要信息
            if (courseName.equals("未知课程") || time.equals("未知时间")) {
                return null;
            }

            Course course = new Course(courseName, teacher, location, time);
            course.dayOfWeek = dayOfWeek;

            return course;

        } catch (Exception e) {
            Log.e(TAG, "提取课程失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 提取健壮的课程名称
     */
    private static String extractRobustCourseName(String text) {
        // 模式1: 课程名称 + 符号 + 节次
        Pattern pattern1 = Pattern.compile("([\\u4e00-\\u9fa5]{4,30}?[★☆■]?)\\s*[\\(（]?\\d+-\\d+节");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            return cleanCourseName(matcher1.group(1));
        }

        // 模式2: 符号标记的课程名称
        Pattern pattern2 = Pattern.compile("([\\u4e00-\\u9fa5]{4,30}[★☆■])");
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            return cleanCourseName(matcher2.group(1));
        }

        // 模式3: 在节次前的最长中文字符串
        Pattern pattern3 = Pattern.compile("([\\u4e00-\\u9fa5]{4,30}?)\\s*\\(?\\d+-\\d+节");
        Matcher matcher3 = pattern3.matcher(text);
        if (matcher3.find()) {
            return cleanCourseName(matcher3.group(1));
        }

        return "未知课程";
    }

    /**
     * 提取健壮的教师信息 - 修复版本
     */
    private static String extractRobustTeacher(String text) {
        try {
            Log.d(TAG, "开始提取教师信息，文本: " + text);

            // 模式1: "教师 ：姓名" 格式（注意中文冒号）
            Pattern teacherPattern1 = Pattern.compile("教师\\s*[：:]\\s*([\\u4e00-\\u9fa5]{2,4})");
            Matcher matcher1 = teacherPattern1.matcher(text);
            if (matcher1.find()) {
                String teacher = matcher1.group(1);
                Log.d(TAG, "模式1找到教师: " + teacher);
                return teacher;
            }

            // 模式2: "教师 姓名" 格式（没有冒号）
            Pattern teacherPattern2 = Pattern.compile("教师\\s+([\\u4e00-\\u9fa5]{2,4})");
            Matcher matcher2 = teacherPattern2.matcher(text);
            if (matcher2.find()) {
                String teacher = matcher2.group(1);
                Log.d(TAG, "模式2找到教师: " + teacher);
                return teacher;
            }

            // 模式3: 在"教师"关键词后面查找姓名
            int teacherIndex = text.indexOf("教师");
            if (teacherIndex != -1) {
                String afterTeacher = text.substring(teacherIndex + 2);
                Pattern namePattern = Pattern.compile("^\\s*([\\u4e00-\\u9fa5]{2,4})");
                Matcher nameMatcher = namePattern.matcher(afterTeacher);
                if (nameMatcher.find()) {
                    String teacher = nameMatcher.group(1);
                    Log.d(TAG, "模式3找到教师: " + teacher);
                    return teacher;
                }
            }

            // 模式4: 查找常见的教师姓名（放宽条件）
            Pattern namePattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,3})");
            Matcher nameMatcher = namePattern.matcher(text);
            while (nameMatcher.find()) {
                String name = nameMatcher.group(1);
                if (isLikelyTeacherName(name, text)) {
                    Log.d(TAG, "模式4找到教师: " + name);
                    return name;
                }
            }

            Log.d(TAG, "未找到教师信息");
            return "未知教师";

        } catch (Exception e) {
            Log.e(TAG, "提取教师信息异常", e);
            return "未知教师";
        }
    }

    /**
     * 放宽教师姓名判断条件
     */
    private static boolean isLikelyTeacherName(String name, String context) {
        if (name == null || name.length() < 2 || name.length() > 3) {
            return false;
        }

        // 常见教师姓氏（扩展列表）
        String[] commonSurnames = {
                "陈", "李", "张", "王", "刘", "杨", "赵", "黄", "周", "吴",
                "罗", "穆", "宁", "安", "董", "徐", "孙", "马", "朱", "林",
                "郭", "何", "高", "郑", "冯", "谢", "曹", "袁", "邓", "许",
                "傅", "沈", "曾", "彭", "吕", "苏", "卢", "蒋", "蔡", "魏",
                "叶", "杜", "夏", "钟", "田", "任", "姜", "范", "方", "石",
                "薛", "雷", "贺", "倪", "汤", "滕", "殷", "郝", "龚", "邵",
                "余", "胡", "宋", "韩", "唐", "于", "萧", "程", "贾", "丁",
                "阎", "潘", "戴", "汪", "姚", "谭", "廖", "邹", "熊", "金",
                "陆", "孔", "白", "崔", "康", "毛", "邱", "秦", "江", "史",
                "顾", "侯", "孟", "龙", "万", "段", "章", "钱", "尹", "黎",
                "易", "常", "武", "乔", "赖", "文"
        };

        // 检查是否以常见姓氏开头
        for (String surname : commonSurnames) {
            if (name.startsWith(surname)) {
                // 检查上下文，确保不是在课程名称中
                if (!context.contains(name + "课程") &&
                        !context.contains(name + "实验") &&
                        !context.contains(name + "实践") &&
                        !context.contains(name + "项目")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 提取健壮的地点信息
     */
    private static String extractRobustLocation(String text) {
        // 标准教室格式
        Pattern locationPattern = Pattern.compile("[A-Z]{1,3}-\\d{2,4}");
        Matcher matcher = locationPattern.matcher(text);
        if (matcher.find()) {
            return "航空港校区 " + matcher.group();
        }

        // 其他校区格式
        if (text.contains("航空港校区")) {
            Pattern campusPattern = Pattern.compile("航空港校区[^，。]*?([A-Z]{1,3}-?\\d{2,4}|[\\u4e00-\\u9fa5]{2,10})");
            Matcher campusMatcher = campusPattern.matcher(text);
            if (campusMatcher.find()) {
                return "航空港校区 " + campusMatcher.group(1);
            }
            return "航空港校区";
        }

        return "未知地点";
    }

    /**
     * 增强的时间信息提取 - 支持网页课表格式
     */
    private static String extractRobustTime(String text) {
        StringBuilder timeBuilder = new StringBuilder();

        // 节次信息 - 匹配 (5-6节) 格式
        Pattern sectionPattern = Pattern.compile("\\((\\d+)[-~至](\\d+)节\\)");
        Matcher sectionMatcher = sectionPattern.matcher(text);
        if (sectionMatcher.find()) {
            timeBuilder.append(sectionMatcher.group(1)).append("-")
                    .append(sectionMatcher.group(2)).append("节");
        } else {
            // 如果没有括号格式，尝试普通格式
            sectionPattern = Pattern.compile("(\\d+)[-~至](\\d+)节");
            sectionMatcher = sectionPattern.matcher(text);
            if (sectionMatcher.find()) {
                timeBuilder.append(sectionMatcher.group(1)).append("-")
                        .append(sectionMatcher.group(2)).append("节");
            }
        }

        // 周数信息 - 匹配 1-15周(单) 或 2-16周(双) 格式
        Pattern weekPattern = Pattern.compile("(\\d+)[-~至](\\d+)周\\s*\\((单|双)\\)");
        Matcher weekMatcher = weekPattern.matcher(text);
        if (weekMatcher.find()) {
            if (timeBuilder.length() > 0) {
                timeBuilder.append(" ");
            }
            timeBuilder.append(weekMatcher.group(1)).append("-")
                    .append(weekMatcher.group(2)).append("周");

            // 识别单双周
            String weekType = weekMatcher.group(3);
            if ("单".equals(weekType)) {
                timeBuilder.append("(单周)");
            } else if ("双".equals(weekType)) {
                timeBuilder.append("(双周)");
            }
        } else {
            // 如果没有括号单双周格式，尝试普通周数格式
            weekPattern = Pattern.compile("(\\d+)[-~至](\\d+)周");
            weekMatcher = weekPattern.matcher(text);
            if (weekMatcher.find()) {
                if (timeBuilder.length() > 0) {
                    timeBuilder.append(" ");
                }
                timeBuilder.append(weekMatcher.group(1)).append("-")
                        .append(weekMatcher.group(2)).append("周");
            }
        }

        return timeBuilder.length() > 0 ? timeBuilder.toString() : "未知时间";
    }

    /**
     * 提取健壮的星期信息
     */
    private static int extractRobustDayOfWeek(Element element, String text) {
        // 从文本中提取
        if (text.contains("星期一") || text.contains("周一")) return 1;
        if (text.contains("星期二") || text.contains("周二")) return 2;
        if (text.contains("星期三") || text.contains("周三")) return 3;
        if (text.contains("星期四") || text.contains("周四")) return 4;
        if (text.contains("星期五") || text.contains("周五")) return 5;
        if (text.contains("星期六") || text.contains("周六")) return 6;
        if (text.contains("星期日") || text.contains("周日")) return 7;

        // 从元素位置推断
        return inferDayOfWeekFromPosition(element);
    }

    /**
     * 从元素位置推断星期
     */
    private static int inferDayOfWeekFromPosition(Element element) {
        try {
            Element current = element;
            for (int i = 0; i < 3; i++) {
                if (current == null) break;

                String id = current.id();
                String className = current.className();

                // 从ID推断 (格式: 1-1, 2-3 等)
                if (id != null && id.matches("\\d+-\\d+")) {
                    String[] parts = id.split("-");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[0]);
                    }
                }

                // 从类名推断
                if (className != null) {
                    Pattern pattern = Pattern.compile("(?:col|day|weekday|week)[-_]?(\\d+)");
                    Matcher matcher = pattern.matcher(className);
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                }

                current = current.parent();
            }
        } catch (Exception e) {
            Log.e(TAG, "推断星期失败", e);
        }

        return 1; // 默认星期一
    }

    /**
     * 检查是否为标题或无效内容
     */
    private static boolean isHeaderOrInvalidContent(String text) {
        if (text.length() < 10) return true;

        for (String keyword : EXCLUDED_HEADER_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        // 检查学年学期格式
        if (text.matches(".*\\d{4}-\\d{4}.*学年.*学期.*")) {
            return true;
        }

        return false;
    }

    /**
     * 清理课程名称
     */
    private static String cleanCourseName(String courseName) {
        return courseName.replace("★", "").replace("☆", "").replace("■", "")
                .replace("【调】", "").replace("（", "").replace("）", "")
                .replace("(", "").replace(")", "").trim();
    }

    /**
     * 验证完整课程信息
     */
    private static boolean isValidCompleteCourse(Course course) {
        return course != null &&
                !course.courseName.equals("未知课程") &&
                course.courseName.length() >= 4 &&
                !course.time.equals("未知时间") &&
                course.dayOfWeek >= 1 && course.dayOfWeek <= 7 &&
                !course.location.equals("未知地点");
    }

    /**
     * 生成课程唯一标识
     */
    private static String generateCourseKey(Course course) {
        return course.courseName + "|" + course.dayOfWeek + "|" +
                course.time + "|" + course.location;
    }

    /**
     * 解析结果封装类
     */
    public static class ParseResult {
        private boolean success;
        private String message;
        private List<Course> courses;

        public ParseResult() {
            this.courses = new ArrayList<>();
        }

        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public List<Course> getCourses() { return courses; }
        public void setCourses(List<Course> courses) { this.courses = courses; }
    }
}