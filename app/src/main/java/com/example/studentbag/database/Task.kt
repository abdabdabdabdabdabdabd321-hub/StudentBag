package com.example.studentbag.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val type: String,
    val date: String,
    val time: String,
    val duration: String,
    val subject: String,
    val isMandatory: Boolean
)