package com.example.studentbag.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.example.studentbag.adapters.PlanAdapter
import com.example.studentbag.database.*
import com.example.studentbag.utils.NotificationReceiver
import kotlinx.coroutines.launch
import java.util.*

class SmartPlanActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnGenerate: Button
    private lateinit var btnEvaluation: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_smart_plan)

        recycler = findViewById(R.id.recyclerPlan)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnEvaluation = findViewById(R.id.btnEvaluation)

        recycler.layoutManager = LinearLayoutManager(this)

        loadPlan()

        btnGenerate.setOnClickListener {
            generatePlan()
        }

        btnEvaluation.setOnClickListener {
            startActivity(Intent(this, EvaluationActivity::class.java))
        }
    }

    private fun loadPlan() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@SmartPlanActivity)
            val data = db.studyPlanDao().getAll()

            recycler.adapter = PlanAdapter(data) { plan, checked ->
                lifecycleScope.launch {
                    db.studyPlanDao().update(plan.copy(isDone = checked))
                }
            }
        }
    }

    private fun generatePlan() {
        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@SmartPlanActivity)

            db.studyPlanDao().deleteAll()

            val tasks = db.taskDao().getAllTasks()

            var currentHour = 8

            for (task in tasks) {

                val hours = task.duration.split(":")[0].toInt()

                for (i in 0 until hours) {

                    val start = String.format("%02d:00", currentHour)
                    val end = String.format("%02d:00", currentHour + 1)

                    val title = "مذاكرة لـ ${task.type} ${task.subject}"
                    val date = task.date // ✅ ربط التاريخ

                    db.studyPlanDao().insert(
                        StudyPlan(
                            taskTitle = title,
                            date = date,
                            startTime = start,
                            endTime = end
                        )
                    )

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, currentHour)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)

                    scheduleNotification(
                        "باقي نصف ساعة",
                        title,
                        calendar.timeInMillis - (30 * 60 * 1000)
                    )

                    scheduleNotification(
                        "باقي 10 دقائق",
                        title,
                        calendar.timeInMillis - (10 * 60 * 1000)
                    )

                    scheduleNotification(
                        "حان الوقت",
                        title,
                        calendar.timeInMillis
                    )

                    currentHour++
                }
            }

            loadPlan()
        }
    }

    private fun scheduleNotification(title: String, message: String, timeInMillis: Long) {

        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Random().nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }
}