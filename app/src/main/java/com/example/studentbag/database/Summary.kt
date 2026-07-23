package com.example.studentbag.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summary")
data class Summary(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val subjectName: String,
    val content: String
)