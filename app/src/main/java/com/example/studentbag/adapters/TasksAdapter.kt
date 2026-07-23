package com.example.studentbag.adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.activities.TasksActivity
import com.example.studentbag.database.AppDatabase
import com.example.studentbag.database.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksAdapter(private var tasks: MutableList<Task>) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textType: TextView = itemView.findViewById(R.id.textType)
        val textSubject: TextView = itemView.findViewById(R.id.textSubject)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val textDuration: TextView = itemView.findViewById(R.id.textDuration)

        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val task = tasks[position]

        holder.textType.text = task.type
        holder.textSubject.text = task.subject
        holder.textDate.text = task.date
        holder.textTime.text = task.time
        holder.textDuration.text = task.duration

        // 🎨 ألوان حسب النوع
        when (task.type) {
            "اختبار" -> holder.textType.setTextColor(Color.RED)
            "محاضرة" -> holder.textType.setTextColor(Color.BLUE)
            "مذاكرة" -> holder.textType.setTextColor(Color.GREEN)
            "تسليم تكليف" -> holder.textType.setTextColor(Color.MAGENTA)
            else -> holder.textType.setTextColor(Color.BLACK)
        }

        // 🗑 حذف
        holder.btnDelete.setOnClickListener {

            val context = holder.itemView.context
            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            AlertDialog.Builder(context)
                .setTitle("حذف المهمة")
                .setMessage("هل تريد حذف هذه المهمة؟")
                .setPositiveButton("نعم") { _, _ ->

                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        db.taskDao().deleteTask(task)
                    }

                    tasks.removeAt(currentPosition)
                    notifyItemRemoved(currentPosition)

                    Toast.makeText(context, "تم حذف المهمة", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        // ✏️ تعديل
        holder.btnEdit.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, TasksActivity::class.java)

            // 🔥 إرسال كل البيانات (مهم جداً)
            intent.putExtra("taskId", task.id)
            intent.putExtra("type", task.type)
            intent.putExtra("subject", task.subject)
            intent.putExtra("date", task.date)
            intent.putExtra("time", task.time)
            intent.putExtra("duration", task.duration)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tasks.size

    // 🔄 تحديث القائمة (اختياري للمستقبل)
    fun updateList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}
