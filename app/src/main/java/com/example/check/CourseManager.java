package com.example.check;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseManager {
    private static CourseManager instance;
    private CourseDao courseDao;
    private ExecutorService executorService;
    private Context context;

    // 添加回调接口
    public interface DatabaseOperationCallback {
        void onOperationCompleted(boolean success);
        void onCoursesLoaded(List<Course> courses);
    }

    private CourseManager(Context context) {
        this.context = context.getApplicationContext();
        this.courseDao = CourseDatabase.getInstance(this.context).courseDao();
        this.executorService = Executors.newSingleThreadExecutor();
        Log.d("CourseManager", "初始化CourseManager with Database");
    }

    public static CourseManager getInstance(Context context) {
        if (instance == null) {
            instance = new CourseManager(context);
        }
        return instance;
    }

    // 同步获取所有课程
    public List<Course> getAllCourses() {
        try {
            List<Course> courses = courseDao.getAllCourses();
            Log.d("CourseManager", "从数据库获取所有课程，数量: " + courses.size());
            return courses;
        } catch (Exception e) {
            Log.e("CourseManager", "获取课程失败", e);
            return new ArrayList<>();
        }
    }

    // 异步获取所有课程（带回调）
    public void getAllCourses(DatabaseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                List<Course> courses = courseDao.getAllCourses();
                Log.d("CourseManager", "从数据库获取所有课程，数量: " + courses.size());
                if (callback != null) {
                    callback.onCoursesLoaded(courses);
                }
            } catch (Exception e) {
                Log.e("CourseManager", "获取课程失败", e);
                if (callback != null) {
                    callback.onCoursesLoaded(new ArrayList<>());
                }
            }
        });
    }

    // 异步添加单个课程
    public void addCourse(Course course) {
        executorService.execute(() -> {
            try {
                courseDao.insertCourse(course);
                Log.d("CourseManager", "添加课程到数据库: " + course.courseName);
            } catch (Exception e) {
                Log.e("CourseManager", "添加课程失败", e);
            }
        });
    }

    // 异步批量添加课程
    public void addAllCourses(List<Course> courses) {
        executorService.execute(() -> {
            try {
                // 先清空再添加
                courseDao.deleteAllCourses();
                courseDao.insertAllCourses(courses);
                Log.d("CourseManager", "批量添加课程到数据库，数量: " + courses.size());
            } catch (Exception e) {
                Log.e("CourseManager", "批量添加课程失败", e);
            }
        });
    }

    // 异步清空课程
    public void clearCourses() {
        executorService.execute(() -> {
            try {
                courseDao.deleteAllCourses();
                Log.d("CourseManager", "清空数据库中的所有课程");
            } catch (Exception e) {
                Log.e("CourseManager", "清空课程失败", e);
            }
        });
    }

    // 同步检查是否有课程
    public boolean hasCourses() {
        try {
            int count = courseDao.getCourseCount();
            boolean hasCourses = count > 0;
            Log.d("CourseManager", "检查数据库是否有课程: " + hasCourses + " (数量: " + count + ")");
            return hasCourses;
        } catch (Exception e) {
            Log.e("CourseManager", "检查课程数量失败", e);
            return false;
        }
    }

    // 异步检查是否有课程（带回调）
    public void hasCourses(DatabaseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                int count = courseDao.getCourseCount();
                boolean hasCourses = count > 0;
                Log.d("CourseManager", "检查数据库是否有课程: " + hasCourses + " (数量: " + count + ")");
                List<Course> courses = hasCourses ? courseDao.getAllCourses() : new ArrayList<>();
                if (callback != null) {
                    callback.onCoursesLoaded(courses);
                }
            } catch (Exception e) {
                Log.e("CourseManager", "检查课程数量失败", e);
                if (callback != null) {
                    callback.onCoursesLoaded(new ArrayList<>());
                }
            }
        });
    }

    // 同步获取当前课程
    public Course getCurrentCourse() {
        try {
            List<Course> courses = courseDao.getAllCourses();
            Course currentCourse = !courses.isEmpty() ? courses.get(0) : null;
            Log.d("CourseManager", "从数据库获取当前课程: " + (currentCourse != null ? currentCourse.courseName : "null"));
            return currentCourse;
        } catch (Exception e) {
            Log.e("CourseManager", "获取当前课程失败", e);
            return null;
        }
    }

    // 异步获取当前课程（带回调）
    public void getCurrentCourse(DatabaseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                List<Course> courses = courseDao.getAllCourses();
                Course currentCourse = !courses.isEmpty() ? courses.get(0) : null;
                Log.d("CourseManager", "从数据库获取当前课程: " + (currentCourse != null ? currentCourse.courseName : "null"));
                List<Course> result = new ArrayList<>();
                if (currentCourse != null) {
                    result.add(currentCourse);
                }
                if (callback != null) {
                    callback.onCoursesLoaded(result);
                }
            } catch (Exception e) {
                Log.e("CourseManager", "获取当前课程失败", e);
                if (callback != null) {
                    callback.onCoursesLoaded(new ArrayList<>());
                }
            }
        });
    }
}