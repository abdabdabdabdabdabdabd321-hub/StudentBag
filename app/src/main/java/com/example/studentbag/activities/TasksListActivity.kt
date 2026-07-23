package com.example.studentbag.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.example.studentbag.adapters.TasksAdapter
import com.example.studentbag.database.AppDatabase
import kotlinx.coroutines.launch

class TasksListActivity : AppCompatActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnSmartPlan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tasks_list)

        recyclerTasks = findViewById(R.id.recyclerTasks)
        btnBack = findViewById(R.id.btnBack)
        btnSmartPlan = findViewById(R.id.btnSmartPlan)

        recyclerTasks.layoutManager = LinearLayoutManager(this)

        loadTasks()

        btnBack.setOnClickListener {
            finish()
        }

        btnSmartPlan.setOnClickListener {
            startActivity(Intent(this, SmartPlanActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@TasksListActivity)
            val tasks = db.taskDao().getAllTasks()
            val adapter = TasksAdapter(tasks.toMutableList())
            recyclerTasks.adapter = adapter
        }
    }
}