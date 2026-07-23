package com.example.studentbag.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.studentbag.R
import com.example.studentbag.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var startDate: String? = null
    private var durationDays: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val settingsPrefs = getSharedPreferences("settings", MODE_PRIVATE)

        val darkMode =
            settingsPrefs.getBoolean("darkMode", false)

        AppCompatDelegate.setDefaultNightMode(

            if (darkMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

        )
        if (FirebaseAuth.getInstance().currentUser == null) {

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()

            return
        }
        setContentView(R.layout.activity_main)

        val btnDuration: Button = findViewById(R.id.btnSelectDuration)
        val cardTasks: CardView = findViewById(R.id.cardTasks)
        val cardSubjects: CardView = findViewById(R.id.cardSubjects)
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)

        prefs = getSharedPreferences("studentbag", MODE_PRIVATE)

        btnSettings.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )

        }

        startDate = prefs.getString("planStartDate", null)
        durationDays = prefs.getInt("planDurationDays", 0)

        if (startDate != null && durationDays != 0) {

            btnDuration.text =
                "📅 بداية المدة: $startDate\n⏳ مدتها: $durationDays يوم"
        }

        btnDuration.setOnClickListener {

            val options = arrayOf(
                "يوم",
                "أسبوع",
                "شهر",
                "تحديد مدة أخرى",
                "حذف المدة"
            )

            AlertDialog.Builder(this)
                .setTitle("تنظيم مدة المخطط")
                .setItems(options) { _, which ->

                    when (options[which]) {

                        "يوم" -> pickStartDate(1)

                        "أسبوع" -> pickStartDate(7)

                        "شهر" -> pickStartDate(30)

                        "تحديد مدة أخرى" -> {

                            val input = EditText(this)
                            input.hint = "عدد الأيام"

                            AlertDialog.Builder(this)
                                .setTitle("مدة مخصصة")
                                .setView(input)
                                .setPositiveButton("التالي") { _, _ ->

                                    val days = input.text.toString()

                                    if (days.isNotEmpty()) {

                                        pickStartDate(days.toInt())

                                    } else {

                                        Toast.makeText(
                                            this,
                                            "أدخل عدد الأيام",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .setNegativeButton("إلغاء", null)
                                .show()
                        }

                        "حذف المدة" -> deletePlan(btnDuration)
                    }
                }
                .show()
        }

        cardSubjects.setOnClickListener {

            if (startDate == null) {

                Toast.makeText(
                    this,
                    "حدد مدة المخطط أولاً",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                startActivity(
                    Intent(this, SubjectsActivity::class.java)
                )
            }
        }

        cardTasks.setOnClickListener {

            if (startDate == null) {

                Toast.makeText(
                    this,
                    "حدد مدة المخطط أولاً",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                startActivity(
                    Intent(this, TasksActivity::class.java)
                )
            }
        }
    }

    private fun pickStartDate(days: Int) {

        val cal = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(y, m, d)

                val today = Calendar.getInstance()

                // ❌ منع اختيار تاريخ في الماضي
                if (selectedCal.before(today.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    })
                ) {
                    Toast.makeText(this, "لا يمكن اختيار تاريخ في الماضي", Toast.LENGTH_LONG).show()
                    return@DatePickerDialog
                }

                val date = "$d/${m + 1}/$y"

                savePlan(date, days)

                val btnDuration: Button = findViewById(R.id.btnSelectDuration)

                btnDuration.text =
                    "📅 بداية المدة: $date\n⏳ مدتها: $days يوم"

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

// 🔥 يمنع حتى اختيار الماضي من التقويم نفسه
        dialog.datePicker.minDate = System.currentTimeMillis()

        dialog.show()
    }

    private fun savePlan(date: String, days: Int) {

        val db = AppDatabase.getDatabase(this)

        val oldDate = prefs.getString("planStartDate", null)

        if (oldDate != null) {

            AlertDialog.Builder(this)
                .setTitle("تنبيه")
                .setMessage("تغيير المدة سيحذف جميع المهام المسجلة. هل تريد المتابعة؟")
                .setPositiveButton("نعم") { _, _ ->

                    db.taskDao().deleteAllTasks()

                    prefs.edit()
                        .putString("planStartDate", date)
                        .putInt("planDurationDays", days)
                        .apply()
                }
                .setNegativeButton("إلغاء", null)
                .show()

        } else {

            prefs.edit()
                .putString("planStartDate", date)
                .putInt("planDurationDays", days)
                .apply()
        }

        startDate = date
        durationDays = days
    }

    private fun deletePlan(btn: Button) {

        if (startDate == null) {

            Toast.makeText(
                this,
                "لا توجد مدة",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val db = AppDatabase.getDatabase(this)

        AlertDialog.Builder(this)
            .setTitle("حذف المدة")
            .setMessage("سيتم حذف جميع المهام المرتبطة بالمخطط")
            .setPositiveButton("حذف") { _, _ ->

                db.taskDao().deleteAllTasks()

                prefs.edit().clear().apply()

                startDate = null
                durationDays = 0

                btn.text = "⏳ اختر مدة المخطط"
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}