package com.example.studentbag.adapters

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.activities.SubjectDetailsActivity
import com.example.studentbag.database.Subject

class SubjectsAdapter(
    private var subjects: List<Subject>,
    private val onDelete: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textSubjectName: TextView =
            itemView.findViewById(R.id.textSubjectName)

        val iconDelete: ImageView =
            itemView.findViewById(R.id.iconDelete)

        // 👇 هذا مهم (أيقونة المجلد)
        val iconFolder: ImageView =
            itemView.findViewById(R.id.iconFolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)

        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {

        val subject = subjects[position]

        holder.textSubjectName.text = subject.name

        // ❌ حذف (كما هو)
        holder.iconDelete.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("حذف المادة")
                .setMessage("هل تريد حذف المادة؟")
                .setPositiveButton("نعم") { _, _ ->
                    onDelete(subject)
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        // ✅ فتح التفاصيل فقط من أيقونة المجلد
        holder.iconFolder.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, SubjectDetailsActivity::class.java)
            intent.putExtra("subjectName", subject.name)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return subjects.size
    }
}