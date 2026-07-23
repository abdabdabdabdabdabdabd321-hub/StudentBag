package com.example.studentbag.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_content")
data class SubjectContent(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val subjectName: String,
    val type: String, // file / image / note
    val content: String
)