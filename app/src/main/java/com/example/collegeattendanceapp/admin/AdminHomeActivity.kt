package com.example.collegeattendanceapp.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegeattendanceapp.MapActivity
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.adapters.AttendanceAdapter
import com.example.collegeattendanceapp.adapters.AttendanceData
import com.example.collegeattendanceapp.databinding.ActivityAdminHomeBinding
import com.example.collegeattendanceapp.main.MainActivity
import com.example.collegeattendanceapp.service.AttendanceService
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val attendanceList = mutableListOf<AttendanceData>()
    private val fs = FirebaseFirestore.getInstance()
    private val TAG = "ADMIN_ATTENDANCE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecycler()

        loadAllTodayAttendance()

        findViewById<Chip>(R.id.chipStudent).setOnClickListener {
            Log.d(TAG, "Student Chip Clicked → Loading student attendance")
            // db.collection("student_attendance")
            //                .document(userId)
            //                .collection("dates")
            //                .document(dateKey)
            //                .get()


            loadTodayAttendance("student_attendance")
        }

        findViewById<Chip>(R.id.chipStaff).setOnClickListener {
            Log.d(TAG, "Staff Chip Clicked → Loading staff attendance")
            loadTodayAttendance("staff_attendance")

        }

        binding.ivLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    val serviceIntent = Intent(this, AttendanceService::class.java)
                    stopService(serviceIntent)
                    val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("isAdminLoggedIn", false).apply()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()

        }
        binding.ivMap.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)


        }
        binding.btnaddLoc.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)


        }


    }

    private fun setupRecycler() {
        binding.recyclerTodayAttendance.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        attendanceAdapter = AttendanceAdapter(attendanceList)
        binding.recyclerTodayAttendance.adapter = attendanceAdapter
    }

    private fun loadAllTodayAttendance() {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        fs.collection("users")
            .get()
            .addOnSuccessListener { usersSnapshot ->

                attendanceList.clear()

                for (userDoc in usersSnapshot) {

                    val uid = userDoc.id
                    val role = userDoc.getString("role") ?: ""
                    val name = userDoc.getString("name") ?: ""

                    val rollOrDept = if (role == "Student") {
                        userDoc.getString("rollno") ?: "--"
                    } else {
                        userDoc.getString("department") ?: "--"
                    }

                    val attendanceRef = fs.collection(
                        if (role == "Student") "student_attendance" else "staff_attendance"
                    ).document(uid).collection("dates").document(today)

                    attendanceRef.get()
                        .addOnSuccessListener { snap ->

                            if (!snap.exists()) {
                                attendanceList.add(
                                    AttendanceData(
                                        name = name,
                                        rollOrDept = rollOrDept,
                                        date = today,
                                        markIn = "",
                                        markOut = "",
                                        markInAddress = "",
                                        markOutAddress = ""
                                    )
                                )
                            } else {
                                attendanceList.add(
                                    AttendanceData(
                                        name = name,
                                        rollOrDept = rollOrDept,
                                        date = today,
                                        markIn = snap.getString("markIn") ?: "",
                                        markOut = snap.getString("markOut") ?: "",
                                        markInAddress = snap.getString("markInAddress") ?: "",
                                        markOutAddress = snap.getString("markOutAddress") ?: ""
                                    )
                                )
                            }

                            attendanceAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Error fetching attendance for $name")
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch users")
            }
    }
    private fun loadTodayAttendance(collectionName: String) {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        fs.collection("users")
            .get()
            .addOnSuccessListener { usersSnapshot ->

                attendanceList.clear()

                for (userDoc in usersSnapshot) {

                    val uid = userDoc.id
                    val role = userDoc.getString("role") ?: ""
                    val name = userDoc.getString("name") ?: ""

                    val rollOrDept = if (role == "Student") {
                        userDoc.getString("rollno") ?: "--"
                    } else {
                        userDoc.getString("department") ?: "--"
                    }

                    val attendanceRef = fs.collection(
                        "$collectionName"
                    ).document(uid).collection("dates").document(today)

                    attendanceRef.get()
                        .addOnSuccessListener { snap ->

                            if (!snap.exists()) {
                                attendanceList.add(
                                    AttendanceData(
                                        name = name,
                                        rollOrDept = rollOrDept,
                                        date = today,
                                        markIn = "",
                                        markOut = "",
                                        markInAddress = "",
                                        markOutAddress = ""
                                    )
                                )
                            } else {
                                attendanceList.add(
                                    AttendanceData(
                                        name = name,
                                        rollOrDept = rollOrDept,
                                        date = today,
                                        markIn = snap.getString("markIn") ?: "",
                                        markOut = snap.getString("markOut") ?: "",
                                        markInAddress = snap.getString("markInAddress") ?: "",
                                        markOutAddress = snap.getString("markOutAddress") ?: ""
                                    )
                                )
                            }

                            attendanceAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Error fetching attendance for $name")
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch users")
            }
    }

}
