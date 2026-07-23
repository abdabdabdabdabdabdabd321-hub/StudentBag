package com.example.studentbag.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SummaryDao {

    @Insert
    suspend fun insert(summary: Summary)

    @Query("SELECT * FROM summary WHERE subjectName = :name LIMIT 1")
    suspend fun getSummary(name: String): Summary?
}