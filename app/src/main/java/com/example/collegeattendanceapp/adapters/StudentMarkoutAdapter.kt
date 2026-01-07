package com.example.collegeattendanceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeattendanceapp.R


class StudentMarkoutAdapter(
    private val list: MutableList<AttendanceData>
) : RecyclerView.Adapter<StudentMarkoutAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMarkInTime: TextView = itemView.findViewById(R.id.tvMarkInTime)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_markin_markout, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = list[position]
        holder.tvMarkInTime.text = "${item.markOut}"
        holder.tvDate.text = item.date
    }

    override fun getItemCount(): Int = list.size
}

