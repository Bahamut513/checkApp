package com.example.check;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Course.class}, version = 1, exportSchema = false)
public abstract class CourseDatabase extends RoomDatabase {
    public abstract CourseDao courseDao();

    private static volatile CourseDatabase INSTANCE;

    public static CourseDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CourseDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CourseDatabase.class,
                            "course_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}