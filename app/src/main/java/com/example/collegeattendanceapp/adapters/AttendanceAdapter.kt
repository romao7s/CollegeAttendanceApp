package com.example.collegeattendanceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeattendanceapp.R

class AttendanceAdapter(
    private val list: MutableList<AttendanceData>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvRollDept: TextView = itemView.findViewById(R.id.tvRollNo)
        val tvAddress: TextView = itemView.findViewById(R.id.tvCheckOutAddress)
        val tvCheckIn: TextView = itemView.findViewById(R.id.tvCheckInTime)
        val tvCheckOut: TextView = itemView.findViewById(R.id.tvCheckOutTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }


    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = list[position]

        holder.tvName.text = item.name
        holder.tvRollDept.text = item.rollOrDept

        holder.tvCheckIn.text = "In: ${item.markIn.ifEmpty { "--:--" }}"
        holder.tvCheckOut.text = "Out: ${item.markOut.ifEmpty { "--:--" }}"
        if (item.markIn.isNullOrEmpty()) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        } else {
            val params = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20, 16, 20, 16)

            holder.itemView.layoutParams = params

            holder.itemView.visibility = View.VISIBLE


        }
        if (item.rollOrDept.isNullOrEmpty()){
            holder.tvRollDept.visibility= View.GONE
        }
        if (item.markOut.isNullOrEmpty()){
            holder.tvCheckOut.visibility= View.GONE
        }
        if (item.markInAddress.isNullOrEmpty()){
            holder.tvAddress.visibility=View.GONE
        }
        holder.tvAddress.text =
            if (item.markOutAddress.isNotEmpty()) item.markOutAddress
            else item.markInAddress
    }

    override fun getItemCount(): Int = list.size
}
