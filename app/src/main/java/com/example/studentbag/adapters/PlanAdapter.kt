package com.example.studentbag.adapters

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.studentbag.R
import com.example.studentbag.database.StudyPlan

class PlanAdapter(
    private val list: List<StudyPlan>,
    private val onCheck: (StudyPlan, Boolean) -> Unit
) : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val id: TextView = v.findViewById(R.id.txtId)
        val title: TextView = v.findViewById(R.id.txtTitle)
        val date: TextView = v.findViewById(R.id.txtDate)
        val time: TextView = v.findViewById(R.id.txtTime)
        val check: CheckBox = v.findViewById(R.id.checkDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.id.text = (position + 1).toString()
        holder.title.text = item.taskTitle
        holder.date.text = item.date
        holder.time.text = "${item.startTime} - ${item.endTime}"
        holder.check.isChecked = item.isDone

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            onCheck(item, isChecked)
        }
    }

    override fun getItemCount() = list.size
}