package com.example.studentbag.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.example.studentbag.database.AppDatabase
import com.example.studentbag.database.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.view.View
import android.view.ViewGroup

class TasksActivity : AppCompatActivity() {

    lateinit var spinnerTaskType: Spinner
    lateinit var spinnerSubject: Spinner

    lateinit var editDate: EditText
    lateinit var editTimeRange: EditText
    lateinit var editDuration: EditText

    lateinit var btnSaveTask: Button
    lateinit var btnShowTasks: Button
    lateinit var btnBack: Button

    lateinit var prefs: SharedPreferences

    private var isEditMode = false
    private var taskId = 0

    // 🔥 نخزن القيم الأصلية
    private var originalTime = ""
    private var originalDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        prefs = getSharedPreferences("studentbag", MODE_PRIVATE)

        spinnerTaskType = findViewById(R.id.spinnerTaskType)
        spinnerSubject = findViewById(R.id.spinnerSubject)

        editDate = findViewById(R.id.editDate)
        editTimeRange = findViewById(R.id.editTimeRange)
        editDuration = findViewById(R.id.editDuration)

        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnShowTasks = findViewById(R.id.btnShowTasks)
        btnBack = findViewById(R.id.btnBack)

        setupTaskTypes()
        loadSubjects()
        setupDatePicker()
        setupTimeRangePicker()
        setupDurationPicker()

        btnBack.setOnClickListener { finish() }

        checkEditMode()

        btnSaveTask.setOnClickListener {
            validateAndSave()
        }

        btnShowTasks.setOnClickListener {
            startActivity(
                android.content.Intent(
                    this,
                    TasksListActivity::class.java
                )
            )
        }
    }

    private fun checkEditMode() {

        taskId = intent.getIntExtra("taskId", 0)

        if (taskId != 0) {
            isEditMode = true

            val date = intent.getStringExtra("date") ?: ""
            val time = intent.getStringExtra("time") ?: ""
            val duration = intent.getStringExtra("duration") ?: ""
            val subject = intent.getStringExtra("subject") ?: ""
            val type = intent.getStringExtra("type") ?: ""

            editDate.setText(date)
            editTimeRange.setText(time)
            editDuration.setText(duration)

            originalTime = time
            originalDate = date

            btnSaveTask.text = "تحديث المهمة"

            // 🔥 تعيين المادة والنوع بعد تحميل الـ spinner
            spinnerSubject.post {
                val adapter = spinnerSubject.adapter as ArrayAdapter<String>
                val pos = adapter.getPosition(subject)
                if (pos >= 0) spinnerSubject.setSelection(pos)
            }

            spinnerTaskType.post {
                val adapter = spinnerTaskType.adapter as ArrayAdapter<String>
                val pos = adapter.getPosition(type)
                if (pos >= 0) spinnerTaskType.setSelection(pos)
            }
        }
    }

    private fun yellowDropdownAdapter(items: List<String>): ArrayAdapter<String> {

        return object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        ) {

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {

                val view = super.getDropDownView(
                    position,
                    convertView,
                    parent
                ) as TextView

                view.setTextColor(Color.parseColor("#FFD54F"))

                return view
            }
        }
    }

    private fun setupTaskTypes() {
        val types = arrayOf(
            "اختبار",
            "تسليم تكليف",
            "مناقشة مشروع",
            "محاضرة",
            "عمل شخصي",
            "استراحة"
        )

        spinnerTaskType.adapter =
            yellowDropdownAdapter(types.toList())
    }

    private fun loadSubjects() {
        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@TasksActivity)

            val subjects = withContext(Dispatchers.IO) {
                db.subjectDao().getAllSubjects()
            }

            val names = mutableListOf("بدون مادة")

            for (s in subjects) {
                names.add(s.name)
            }

            spinnerSubject.adapter =
                yellowDropdownAdapter(names)
        }
    }

    private fun setupDatePicker() {
        editDate.setOnClickListener {

            val cal = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, y, m, d ->
                    editDate.setText("$d/${m + 1}/$y")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimeRangePicker() {
        editTimeRange.setOnClickListener {

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_time_picker)

            val startHour = dialog.findViewById<NumberPicker>(R.id.startHourPicker)
            val startMinute = dialog.findViewById<NumberPicker>(R.id.startMinutePicker)

            val endHour = dialog.findViewById<NumberPicker>(R.id.endHourPicker)
            val endMinute = dialog.findViewById<NumberPicker>(R.id.endMinutePicker)

            val btnOk = dialog.findViewById<Button>(R.id.btnOk)

            listOf(startHour, endHour).forEach {
                it.minValue = 0
                it.maxValue = 23
            }

            listOf(startMinute, endMinute).forEach {
                it.minValue = 0
                it.maxValue = 59
            }

            btnOk.setOnClickListener {

                val startMinutes = startHour.value * 60 + startMinute.value
                val endMinutes = endHour.value * 60 + endMinute.value

                if (endMinutes <= startMinutes) {
                    Toast.makeText(this, "وقت النهاية يجب أن يكون بعد البداية", Toast.LENGTH_LONG).show()
                    shakePhone()
                    return@setOnClickListener
                }

                val start = String.format(
                    Locale.US,
                    "%02d:%02d",
                    startHour.value,
                    startMinute.value
                )

                val end = String.format(
                    Locale.US,
                    "%02d:%02d",
                    endHour.value,
                    endMinute.value
                )

                editTimeRange.setText("$start - $end")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupDurationPicker() {
        editDuration.setOnClickListener {

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_duration_picker)

            val hourPicker = dialog.findViewById<NumberPicker>(R.id.hourPicker)
            val minutePicker = dialog.findViewById<NumberPicker>(R.id.minutePicker)
            val btnOk = dialog.findViewById<Button>(R.id.btnOk)

            hourPicker.minValue = 0
            hourPicker.maxValue = 12

            minutePicker.minValue = 0
            minutePicker.maxValue = 59

            btnOk.setOnClickListener {

                val h = hourPicker.value
                val m = minutePicker.value

                val duration = String.format(
                    Locale.US,
                    "%02d:%02d",
                    h,
                    m
                )
                editDuration.setText(duration)

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun validateAndSave() {

        val type = spinnerTaskType.selectedItem.toString()
        val subject = spinnerSubject.selectedItem.toString()

        if (editDate.text.isEmpty()) {
            showError(editDate); return
        }

        if (editTimeRange.text.isEmpty()) {
            showError(editTimeRange); return
        }

        if (editDuration.text.isEmpty()) {
            showError(editDuration); return
        }

        if (type in listOf("اختبار", "تسليم تكليف", "مناقشة مشروع", "محاضرة")
            && subject == "بدون مادة"
        ) {
            Toast.makeText(this, "يجب اختيار مادة", Toast.LENGTH_LONG).show()
            shakePhone()
            return
        }

        if (!isDateWithinPlan(editDate.text.toString())) {
            Toast.makeText(this, "التاريخ خارج مدة المخطط", Toast.LENGTH_LONG).show()
            shakePhone()
            return
        }

        checkConflictAndSave()
    }

    private fun isDateWithinPlan(dateStr: String): Boolean {

        val startDateStr = prefs.getString("planStartDate", null) ?: return false
        val days = prefs.getInt("planDurationDays", 0)

        val format = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

        val startDate = format.parse(startDateStr) ?: return false
        val selectedDate = format.parse(dateStr) ?: return false

        val cal = Calendar.getInstance()
        cal.time = startDate
        cal.add(Calendar.DAY_OF_YEAR, days)

        val endDate = cal.time

        return !selectedDate.before(startDate) && !selectedDate.after(endDate)
    }

    private fun checkConflictAndSave() {

        val date = editDate.text.toString()
        val timeRange = editTimeRange.text.toString()

        // 🔥 إذا نفس الوقت ونفس التاريخ → تجاهل التعارض
        if (isEditMode && date == originalDate && timeRange == originalTime) {
            saveTask()
            return
        }

        val parts = timeRange.split(" - ")
        val start = convertToMinutes(parts[0])
        val end = convertToMinutes(parts[1])

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@TasksActivity)

            val tasks = withContext(Dispatchers.IO) {
                db.taskDao().getTasksByDate(date)
            }

            for (task in tasks) {

                if (isEditMode && task.id == taskId) continue

                val t = task.time.split(" - ")
                if (t.size < 2) continue

                val s = convertToMinutes(t[0])
                val e = convertToMinutes(t[1])

                if (start < e && end > s) {
                    Toast.makeText(this@TasksActivity, "الوقت متعارض مع مهمة أخرى", Toast.LENGTH_LONG).show()
                    shakePhone()
                    return@launch
                }
            }

            saveTask()
        }
    }

    private fun convertToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun saveTask() {

        val type = spinnerTaskType.selectedItem.toString()

        val task = Task(
            id = if (isEditMode) taskId else 0,
            title = type,
            type = type,
            date = editDate.text.toString(),
            time = editTimeRange.text.toString(),
            duration = editDuration.text.toString(),
            subject = spinnerSubject.selectedItem.toString(),
            isMandatory = true
        )

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@TasksActivity)

            withContext(Dispatchers.IO) {
                if (isEditMode) {
                    db.taskDao().updateTask(task)
                } else {
                    db.taskDao().insertTask(task)
                }
            }

            Toast.makeText(
                this@TasksActivity,
                if (isEditMode) "تم تحديث المهمة" else "تم حفظ المهمة",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }

    private fun showError(edit: EditText) {
        edit.error = "حقل إجباري"
        val shake = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        edit.startAnimation(shake)
        shakePhone()
    }

    private fun shakePhone() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                android.os.VibrationEffect.createOneShot(
                    200,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(200)
        }
    }
}