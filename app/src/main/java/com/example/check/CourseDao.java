package com.example.check;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface CourseDao {
    @Query("SELECT * FROM courses")
    List<Course> getAllCourses();

    @Insert
    void insertCourse(Course course);

    @Insert
    void insertAllCourses(List<Course> courses);

    @Query("DELETE FROM courses")
    void deleteAllCourses();

    @Query("SELECT COUNT(*) FROM courses")
    int getCourseCount();

    // 添加调试查询
    @Query("SELECT * FROM courses LIMIT 5")
    List<Course> getFirstFiveCourses();
}