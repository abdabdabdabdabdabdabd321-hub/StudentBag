package com.example.studentbag.adapters

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.database.SubjectContent

class FilesAdapter(
    private val files: List<SubjectContent>,
    private val onDelete: (SubjectContent) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtFileName)
        val deleteBtn: ImageView = view.findViewById(R.id.iconDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {

        val item = files[position]

        // عرض الاسم
        holder.txtName.text =
            if (item.type == "note") item.content
            else Uri.parse(item.content).lastPathSegment

        // الضغط على العنصر
        holder.itemView.setOnClickListener {

            // 🔥 لو ملاحظة → نعرضها كاملة داخل Dialog
            if (item.type == "note") {

                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("📌 الملاحظة")
                    .setMessage(item.content)
                    .setPositiveButton("إغلاق", null)
                    .show()

            } else {
                // 🔥 لو ملف أو صورة → نفتحه
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val uri = Uri.parse(item.content)

                    if (item.type == "image") {
                        intent.setDataAndType(uri, "image/*")
                    } else {
                        intent.setDataAndType(uri, "*/*")
                    }

                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    holder.itemView.context.startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(
                        holder.itemView.context,
                        "لا يمكن فتح الملف",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // حذف
        holder.deleteBtn.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount() = files.size
}