package com.example.studentbag.database

import androidx.room.*

@Dao
interface SubjectContentDao {

    @Insert
    fun insert(content: SubjectContent)

    @Query("SELECT * FROM subject_content WHERE subjectName = :name AND type = :type")
    fun getByType(name: String, type: String): List<SubjectContent>

    // 🔥 الجديد (لحل المشكلة)
    @Query("SELECT * FROM subject_content WHERE subjectName = :name")
    suspend fun getAllContents(name: String): List<SubjectContent>

    @Delete
    fun delete(content: SubjectContent)
}