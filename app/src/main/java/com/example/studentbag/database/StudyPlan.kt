package com.example.studentbag.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plan")
data class StudyPlan(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val taskTitle: String,
    val date: String, // ✅ جديد
    val startTime: String,
    val endTime: String,
    val isDone: Boolean = false
)