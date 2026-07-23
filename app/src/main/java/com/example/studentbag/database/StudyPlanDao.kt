package com.example.studentbag.database

import androidx.room.*

@Dao
interface StudyPlanDao {

    @Insert
    suspend fun insert(plan: StudyPlan)

    @Query("SELECT * FROM study_plan")
    suspend fun getAll(): List<StudyPlan>

    @Query("DELETE FROM study_plan")
    suspend fun deleteAll()

    @Update
    suspend fun update(plan: StudyPlan)
}