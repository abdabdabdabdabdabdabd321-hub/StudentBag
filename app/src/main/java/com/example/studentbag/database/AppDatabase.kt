package com.example.studentbag.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Task::class,
        SubjectContent::class,
        StudyPlan::class,
        Summary::class   // ✅ الجديد
    ],
    version = 3, // 🔥 رفعنا النسخة
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
    abstract fun subjectContentDao(): SubjectContentDao

    abstract fun studyPlanDao(): StudyPlanDao

    abstract fun summaryDao(): SummaryDao // ✅ الجديد

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studentbag_db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}