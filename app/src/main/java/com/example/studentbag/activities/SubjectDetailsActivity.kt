package com.example.studentbag.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.adapters.FilesAdapter
import com.example.studentbag.database.AppDatabase
import com.example.studentbag.database.SubjectContent
import kotlinx.coroutines.*

class SubjectDetailsActivity : AppCompatActivity() {

    lateinit var txtSubjectName: TextView

    lateinit var btnUploadPdf: Button
    lateinit var btnUploadImage: Button
    lateinit var btnNotes: Button

    lateinit var iconFiles: ImageView
    lateinit var iconImages: ImageView
    lateinit var iconNotes: ImageView

    lateinit var recyclerFiles: RecyclerView
    lateinit var recyclerImages: RecyclerView
    lateinit var recyclerNotes: RecyclerView

    lateinit var btnBack: Button
    lateinit var btnSave: Button
    lateinit var btnSummarize: Button

    val fileList = mutableListOf<SubjectContent>()
    val imageList = mutableListOf<SubjectContent>()
    val notesList = mutableListOf<SubjectContent>()

    lateinit var fileAdapter: FilesAdapter
    lateinit var imageAdapter: FilesAdapter
    lateinit var notesAdapter: FilesAdapter

    lateinit var subjectName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_details)

        txtSubjectName = findViewById(R.id.txtSubjectName)

        btnUploadPdf = findViewById(R.id.btnUploadPdf)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnNotes = findViewById(R.id.btnNotes)

        iconFiles = findViewById(R.id.iconFiles)
        iconImages = findViewById(R.id.iconImages)
        iconNotes = findViewById(R.id.iconNotes)

        recyclerFiles = findViewById(R.id.recyclerFiles)
        recyclerImages = findViewById(R.id.recyclerImages)
        recyclerNotes = findViewById(R.id.recyclerNotes)

        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        btnSummarize = findViewById(R.id.btnSummarize)

        subjectName = intent.getStringExtra("subjectName") ?: "مادة"
        txtSubjectName.text = subjectName

        fileAdapter = FilesAdapter(fileList) { deleteItem(it) }
        imageAdapter = FilesAdapter(imageList) { deleteItem(it) }
        notesAdapter = FilesAdapter(notesList) { deleteItem(it) }

        recyclerFiles.layoutManager = LinearLayoutManager(this)
        recyclerFiles.adapter = fileAdapter

        recyclerImages.layoutManager = LinearLayoutManager(this)
        recyclerImages.adapter = imageAdapter

        recyclerNotes.layoutManager = LinearLayoutManager(this)
        recyclerNotes.adapter = notesAdapter

        loadAll()

        btnUploadPdf.setOnClickListener { pickFile() }
        btnUploadImage.setOnClickListener { pickImage() }
        btnNotes.setOnClickListener { showNoteDialog() }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
        }

        btnSummarize.setOnClickListener {

            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("subjectName", subjectName)

            startActivity(intent)
        }

        iconFiles.setOnClickListener { toggle(recyclerFiles) }
        iconImages.setOnClickListener { toggle(recyclerImages) }
        iconNotes.setOnClickListener { toggle(recyclerNotes) }
    }

    private fun toggle(view: View) {
        view.visibility = if (view.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 100)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 200)
    }

    private fun showNoteDialog() {
        val input = EditText(this)
        input.minLines = 4

        AlertDialog.Builder(this)
            .setTitle("إضافة ملاحظة")
            .setView(input)
            .setPositiveButton("حفظ") { _, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) saveContent("note", text)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun saveContent(type: String, content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@SubjectDetailsActivity)

            db.subjectContentDao().insert(
                SubjectContent(
                    subjectName = subjectName,
                    type = type,
                    content = content
                )
            )

            withContext(Dispatchers.Main) {
                loadAll()
            }
        }
    }

    private fun loadAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@SubjectDetailsActivity)

            val files = db.subjectContentDao().getByType(subjectName, "file")
            val images = db.subjectContentDao().getByType(subjectName, "image")
            val notes = db.subjectContentDao().getByType(subjectName, "note")

            withContext(Dispatchers.Main) {
                fileList.clear()
                imageList.clear()
                notesList.clear()

                fileList.addAll(files)
                imageList.addAll(images)
                notesList.addAll(notes)

                fileAdapter.notifyDataSetChanged()
                imageAdapter.notifyDataSetChanged()
                notesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteItem(item: SubjectContent) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@SubjectDetailsActivity)
            db.subjectContentDao().delete(item)

            withContext(Dispatchers.Main) {
                loadAll()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        val uri = data.data ?: return

        try {

            // 🔥 حفظ الصلاحية (أهم سطر في المشروع كله)
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val mimeType = contentResolver.getType(uri) ?: ""

            // ================= الملفات =================
            if (requestCode == 100) {

                if (mimeType.contains("pdf") ||
                    mimeType.contains("ms-powerpoint") ||
                    mimeType.contains("presentation")
                ) {
                    saveContent("file", uri.toString())
                    Toast.makeText(this, "✅ تم حفظ الملف", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ اختر PDF أو PPT فقط", Toast.LENGTH_SHORT).show()
                }
            }

            // ================= الصور =================
            if (requestCode == 200) {

                if (mimeType.startsWith("image")) {
                    saveContent("image", uri.toString())
                    Toast.makeText(this, "✅ تم حفظ الصورة", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ اختر صورة فقط", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "⚠️ خطأ في قراءة الملف", Toast.LENGTH_SHORT).show()
        }
    }
}