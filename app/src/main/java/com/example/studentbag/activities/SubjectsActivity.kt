package com.example.studentbag.activities

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.adapters.SubjectsAdapter
import com.example.studentbag.database.AppDatabase
import com.example.studentbag.database.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubjectsActivity : AppCompatActivity() {

    lateinit var editSubjectName: EditText
    lateinit var btnSaveSubject: Button
    lateinit var btnBack: Button
    lateinit var recyclerSubjects: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subjects)

        editSubjectName = findViewById(R.id.editSubjectName)
        btnSaveSubject = findViewById(R.id.btnSaveSubject)
        btnBack = findViewById(R.id.btnBack)
        recyclerSubjects = findViewById(R.id.recyclerSubjects)

        recyclerSubjects.layoutManager = LinearLayoutManager(this)

        loadSubjects()

        btnSaveSubject.setOnClickListener {
            saveSubject()
        }

        btnBack.setOnClickListener {
            finish()
        }

        editSubjectName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveSubject()
                true
            } else false
        }
    }

    private fun saveSubject() {

        val name = editSubjectName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "اكتب اسم المادة", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@SubjectsActivity)

            // 🔥 تحقق من التكرار
            val exists = withContext(Dispatchers.IO) {
                db.subjectDao().isSubjectExists(name)
            }

            if (exists) {
                Toast.makeText(
                    this@SubjectsActivity,
                    "⚠️ هذه المادة مسجلة مسبقاً",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            //حفظ الماده
            withContext(Dispatchers.IO) {
                db.subjectDao().insertSubject(
                    Subject(name = name)
                )
            }

            editSubjectName.setText("")
            loadSubjects()

            Toast.makeText(
                this@SubjectsActivity,
                "✅ تم حفظ المادة",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadSubjects() {

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@SubjectsActivity)

            val subjects = withContext(Dispatchers.IO) {
                db.subjectDao().getAllSubjects()
            }

            //حذف الماده
            val adapter = SubjectsAdapter(subjects) { subject ->

                lifecycleScope.launch {

                    withContext(Dispatchers.IO) {
                        db.subjectDao().deleteSubject(subject)
                    }

                    loadSubjects()
                }
            }

            recyclerSubjects.adapter = adapter
        }
    }
}