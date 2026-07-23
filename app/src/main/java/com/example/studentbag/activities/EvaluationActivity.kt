package com.example.studentbag.activities

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.example.studentbag.database.AppDatabase
import kotlinx.coroutines.launch

class EvaluationActivity : AppCompatActivity() {

    private lateinit var txtPercent: TextView
    private lateinit var txtTasks: TextView
    private lateinit var txtReport: TextView

    private lateinit var cardPercent: LinearLayout
    private lateinit var cardTasks: LinearLayout
    private lateinit var cardReport: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_evaluation)

        txtPercent = findViewById(R.id.txtPercent)
        txtTasks = findViewById(R.id.txtTasks)
        txtReport = findViewById(R.id.txtReport)

        cardPercent = findViewById(R.id.cardPercent)
        cardTasks = findViewById(R.id.cardTasks)
        cardReport = findViewById(R.id.cardReport)

        calculateEvaluation()
    }

    private fun calculateEvaluation() {

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@EvaluationActivity)
            val list = db.studyPlanDao().getAll()

            if (list.isEmpty()) {

                txtPercent.text = "0%"
                txtTasks.text = "لا يوجد أعمال"
                txtReport.text = "لا يوجد تقييم"

                applyBlueStyle(0)

                return@launch
            }

            val done = list.count { it.isDone }
            val total = list.size

            val percent = (done * 100) / total

            val report = when {
                percent >= 90 -> "🔥 ممتاز جداً"
                percent >= 70 -> "👍 جيد"
                percent >= 50 -> "⚠️ متوسط"
                else -> "❗ ضعيف"
            }

            txtPercent.text = "$percent%"
            txtTasks.text = "$done من $total"
            txtReport.text = report

            applyBlueStyle(percent)
        }
    }

    private fun applyBlueStyle(percent: Int) {

        /*
         * يبدأ من الأزرق الفاتح الموجود بالصورة
         * ثم يصبح أزرق أقوى تدريجياً
         * بدون أي لون أخضر
         */

        val red = 187 - (percent * 40 / 100)
        val green = 222 - (percent * 70 / 100)
        val blue = 251

        val color = Color.rgb(red, green, blue)

        setRoundedCard(cardPercent, color)
        setRoundedCard(cardTasks, color)
        setRoundedCard(cardReport, color)
    }

    private fun setRoundedCard(card: LinearLayout, color: Int) {

        val drawable = GradientDrawable()

        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 40f
        drawable.setColor(color)

        card.background = drawable
    }
}