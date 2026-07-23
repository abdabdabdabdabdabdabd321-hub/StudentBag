package com.example.studentbag.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubjectDao {

    @Insert
    suspend fun insertSubject(subject: Subject)

    @Query("SELECT * FROM Subject")
    suspend fun getAllSubjects(): List<Subject>

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // 🔥 دالة التحقق من التكرار
    @Query("SELECT EXISTS(SELECT 1 FROM Subject WHERE name = :name)")
    suspend fun isSubjectExists(name: String): Boolean
}