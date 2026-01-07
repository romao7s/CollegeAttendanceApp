package com.example.collegeattendanceapp.adapters

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeattendanceapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class StudentWorkHoursAdapter(
    private val list: MutableList<AttendanceData>
) : RecyclerView.Adapter<StudentWorkHoursAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMarkInTime: TextView = itemView.findViewById(R.id.tvMarkInTime)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressAttendance)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_item, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = list[position]

        val totalMinutes = getTotalMinutes(item.markIn, item.markOut)

        holder.progressBar.max = 480

        val animator = ObjectAnimator.ofInt(holder.progressBar, "progress", 0, totalMinutes)
        animator.duration = 1200
        animator.start()

        holder.tvDate.text = item.date
        holder.tvMarkInTime.text = getTimeDifference(item.markIn, item.markOut)
    }


    private fun getTimeDifference(markIn: String, markOut: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val inTime = format.parse(markIn)
            val outTime = format.parse(markOut)

            if (inTime != null && outTime != null) {
                val diff = outTime.time - inTime.time

                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60

                String.format("%02dh %02dm", hours, minutes)
            } else {
                "--"
            }
        } catch (e: Exception) {
            "--"
        }
    }

    private fun getTotalMinutes(markIn: String, markOut: String): Int {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val inTime = format.parse(markIn)
            val outTime = format.parse(markOut)

            if (inTime != null && outTime != null) {
                val diff = outTime.time - inTime.time
                (diff / (1000 * 60)).toInt() // total minutes
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    override fun getItemCount(): Int = list.size
}

