package com.example.collegeattendanceapp.student

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.adapters.AttendanceData
import com.example.collegeattendanceapp.adapters.StudentAttendanceAdapter
import com.example.collegeattendanceapp.adapters.StudentMarkoutAdapter
import com.example.collegeattendanceapp.adapters.StudentWorkHoursAdapter
import com.example.collegeattendanceapp.databinding.ActivityStudentBinding
import com.example.collegeattendanceapp.main.MainActivity
import com.example.collegeattendanceapp.service.AttendanceService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.text.compareTo

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = "AdminActivity"


    private val PERMISSION_CODE = 2002
//    lateinit var MarkInAdapter: StudentAttendanceAdapter
//    lateinit var MarkOutsAdapter: StudentMarkoutAdapter
//    lateinit var WorkHoursAdapter: StudentWorkHoursAdapter


    val markInList = mutableListOf<AttendanceData>()
    val markOutList = mutableListOf<AttendanceData>()
    val workHoursList = mutableListOf<AttendanceData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        binding.recyclerMarkIn.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        MarkInAdapter = StudentAttendanceAdapter(markInList)
//        binding.recyclerMarkIn.adapter = MarkInAdapter
//
//        binding.recyclerMarkout.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        MarkOutsAdapter = StudentMarkoutAdapter(markOutList)
//        binding.recyclerMarkout.adapter = MarkOutsAdapter
//
//        binding.recyclerHours.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        WorkHoursAdapter = StudentWorkHoursAdapter(workHoursList)
//        binding.recyclerHours.adapter = WorkHoursAdapter
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        loadTodayMarkIn(uid, binding.tvName)
        requestAllPermissions()

        startLocationService()

        setTodayDateUI()
        loadUserName()
        loadTodayMarks(uid)
       // loadStudentAttendance(uid)
        loadMonthlyAttendance()
         loadAttendance("student_attendance")
        loadWeeklyAttendance()
        binding.ivLogout.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    val serviceIntent = Intent(this, AttendanceService::class.java)
                    stopService(serviceIntent)
                    getSharedPreferences("attendance_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
        }

//
//        if (uid != null) {
//            Log.d("userLog","$uid")
//            val prefs = getSharedPreferences("attendance_prefs", Context.MODE_PRIVATE)
//            prefs.edit().putString("saved_uid", uid).apply()
//        }

        FirebaseFirestore.getInstance().collection("student_attendance")
            .get()
            .addOnSuccessListener { snap ->
                for (d in snap) {
                    Log.e("TEST", "DOC → ${d.id}")
                }
            }

            .addOnFailureListener {
                Log.e("TEST", "ERROR → ${it.message}")
            }
        binding.main.setOnRefreshListener {
            setTodayDateUI()
            loadMonthlyAttendance()
            loadTodayMarks(uid)

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun getThisWeekDates(): List<String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dates = mutableListOf<String>()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        for (i in 0 until 5) {
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dateKey = String.format("%02d-%02d-%04d", day, month, year)
            dates.add(dateKey)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    private fun loadWeeklyAttendance() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid

        val db = FirebaseFirestore.getInstance()
        val weekDates = getThisWeekDates()

        val imageViews = listOf(
            binding.ivMonday,
            binding.ivTuesday,
            binding.ivWednesday,
            binding.ivThursday,
            binding.ivFriday
        )

        for (i in weekDates.indices) {
            val dateKey = weekDates[i]
            val targetView = imageViews[i]

            db.collection("student_attendance")
                .document(userId)
                .collection("dates")
                .document(dateKey)
                .get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
                        val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false

                        if (isMarkedIn || isMarkedOut) {
                            targetView.setImageResource(R.drawable.letter_uppercase_circle_p_svgrepo_com)
                            targetView.setBackgroundResource(R.drawable.bg_present_circle)
                        } else {
                            targetView.setImageResource(R.drawable.letter_uppercase_circle_a_svgrepo_com)
                            targetView.setBackgroundResource(R.drawable.bg_absent_circle)
                        }

                    } else {

                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val date = sdf.parse(dateKey)
                        val today = Calendar.getInstance().time

                        if (date.before(today)) {
                            targetView.setImageResource(R.drawable.letter_uppercase_circle_a_svgrepo_com)
                            targetView.setBackgroundResource(R.drawable.bg_absent_circle)

                        } else {
                            targetView.setImageDrawable(null)
                            targetView.setBackgroundResource(R.drawable.bg_empty_circle)
                        }
                    }

                }
        }
    }

    private fun loadMonthlyAttendance() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val db = FirebaseFirestore.getInstance()

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        var presentCount = 0
        var absentCount = 0
        var holidayCount = 0

        var completedRequests = 0

        for (day in 1..daysInMonth) {

            val dateKey = String.format("%02d-%02d-%04d", day, month, year)

            calendar.set(year, month - 1, day)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isSunday = dayOfWeek == Calendar.SUNDAY
            val isSaturday = dayOfWeek == Calendar.SATURDAY

            db.collection("student_attendance")
                .document(userId)
                .collection("dates")
                .document(dateKey)
                .get()
                .addOnSuccessListener { doc ->

                    if (isSunday || isSaturday) {
                        holidayCount++
                    } else {
                        if (doc.exists()) {
                            val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
                            val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false

                            if (isMarkedIn || isMarkedOut) {
                                presentCount++
                            } else {
                                absentCount++
                            }
                        } else {
                            absentCount++
                        }
                    }

                    completedRequests++

                    if (completedRequests == daysInMonth) {

                        val total = presentCount + absentCount + holidayCount

                        val presentPercent = if (total > 0) (presentCount * 100) / total else 0
                        val absentPercent = if (total > 0) (absentCount * 100) / total else 0
                        val holidayPercent = if (total > 0) (holidayCount * 100) / total else 0

                        updateCircularBars(presentPercent, absentPercent, holidayPercent)

                        binding.cpbHolidays.apply {
                            binding.txtHolidayPercent.text = "$holidayCount"
                        }
                        Log.d(
                            "MonthSummary",
                            "Present=$presentCount, Absent=$absentCount, Holidays=$holidayCount"
                        )
                    }
                }
        }
    }

    private fun requestAllPermissions() {

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= 34) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_CODE
        )
    }



    private fun loadTodayMarks(uid: String) {

        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()

        db.collection("student_attendance")
            .document(uid)
            .collection("dates")
            .document(today)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    Log.e("Admin", "No attendance document for $uid on $today")
                    return@addOnSuccessListener
                    binding.main.isRefreshing = false

                }

                val markIn = doc.getString("markIn") ?: ""
                val markOut = doc.getString("markOut") ?: ""

                val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
                val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false

                val markInLoc = doc.get("markInLocation") as? Map<String, Any> ?: emptyMap()
                val markOutLoc = doc.get("markOutLocation") as? Map<String, Any> ?: emptyMap()
                val markInAddress=doc.getString("markInAddress")?:""
                val markOutAddress=doc.getString("markOutAddress")?:""


                Log.d(
                    "Admin",
                    "MarkIn=$markIn MarkOut=$markOut  LocIn=$markInLoc LocOut=$markOutLoc"
                )


                if (isMarkedIn && markIn.isNotEmpty()) {

                    binding.tvCheckInTime.text = markIn

                    val lat = (markInLoc["lat"] as? Double) ?: 0.0
                    val lon = (markInLoc["lon"] as? Double) ?: 0.0

                        binding.tvCheckInAddress.text =markInAddress ?:""


                    binding.tvCheckInLatLong.text = "Lat: $lat, Lon: $lon"
                    binding.main.isRefreshing = false


                } else {
                    binding.tvCheckInTime.text = "Not Marked In"
                    binding.tvCheckInAddress.text = "-"
                    binding.tvCheckInLatLong.text = "-"
                    binding.main.isRefreshing = false
                    binding.main.isRefreshing = false


                }



                if (isMarkedOut && markOut.isNotEmpty()) {

                    binding.tvCheckOutTime.text = markOut

                    val lat = (markOutLoc["lat"] as? Double) ?: 0.0
                    val lon = (markOutLoc["lon"] as? Double) ?: 0.0

                        binding.tvCheckOutAddress.text =markOutAddress ?:""


                    binding.tvCheckOutLatLong.text = "Lat: $lat, Lon: $lon"
                    binding.main.isRefreshing = false

                } else {
                    binding.tvCheckOutTime.text = "Not Marked Out"
                    binding.tvCheckOutAddress.text = "-"
                    binding.tvCheckOutLatLong.text = "-"
                    binding.main.isRefreshing = false

                }
                getTimeDifference(markIn,markOut)
            }
            .addOnFailureListener {
                Log.e("Admin", "Failed to load attendance: $it")
                binding.main.isRefreshing = false

            }
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


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {

            if (hasAllPermissions()) {
            //    startAttendanceService()

            } else {
                Toast.makeText(
                    this,
                    "All permissions required to mark attendance!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateCircularBars(p: Int, a: Int, h: Int) {

        binding.cpbPresent.apply {
            setProgressWithAnimation(p.toFloat(), 1000)
            binding.txtPresentPercent.text = "$p%"

        }

        binding.cpbAbsent.apply {
            setProgressWithAnimation(a.toFloat(), 1000)
            binding.txtAbsentPercent.text = "$a%"
        }

        binding.cpbHolidays.apply {
            setProgressWithAnimation(h.toFloat(), 1000)
          //  binding.txtHolidayPercent.text = "$h%"
        }
    }


    private fun hasAllPermissions(): Boolean {
        val notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            else true
        val fine = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fgService =
            if (Build.VERSION.SDK_INT >= 34)
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            else true

        return fine && coarse && fgService && notification
    }

    private fun setTodayDateUI() {

        val calendar = Calendar.getInstance()

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        binding.date.text = dayOfMonth.toString()

        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        binding.day.text = dayName

        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        binding.fulldate.text = monthYear
    }
    private fun startLocationService() {
        if (hasAllPermissions()) {
            val serviceIntent = Intent(this, AttendanceService::class.java).apply {
                action = "ACTION_START_LOCATION_UPDATES"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            requestAllPermissions()
        }
    }

    private fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "User"
                binding.tvName.text = name
            }
    }
//
//    private fun startAttendanceService() {
//        val intent = Intent(this, AttendanceService::class.java)
//        ContextCompat.startForegroundService(this, intent)
//    }

    private fun loadStudentAttendance(uid: String) {

        val ref = FirebaseFirestore.getInstance()
            .collection("student_attendance")
            .document(uid)
            .collection("dates")

        ref.get().addOnSuccessListener { query ->

            markInList.clear()
            markOutList.clear()
            workHoursList.clear()

            for (doc in query) {
                val date = doc.id
                val markIn = doc.getString("markIn") ?: ""
                val markOut = doc.getString("markOut") ?: ""
                val markInLoc = doc.getString("markInLoc") ?: ""
                val markOutLoc = doc.getString("markOutLoc") ?: ""

                val workHours = if (markIn.isNotEmpty() && markOut.isNotEmpty()) {
                    calculateHours(markIn, markOut)
                } else ""

                val data = AttendanceData(
                    date,
                    markIn,
                    markOut,
                    workHours,
                    markInLoc,
                    markOutLoc
                )

                if (markIn.isNotEmpty()) markInList.add(data)
                if (markOut.isNotEmpty()) markOutList.add(data)
                if (workHours.isNotEmpty()) workHoursList.add(data)
            }

//            MarkInAdapter.notifyDataSetChanged()
//            MarkOutsAdapter.notifyDataSetChanged()
//            WorkHoursAdapter.notifyDataSetChanged()
        }
    }

    private fun calculateHours(markIn: String, markOut: String): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val inTime = format.parse(markIn)
        val outTime = format.parse(markOut)

        val diff = outTime.time - inTime.time
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60

        return "$hours hr $minutes min"
    }

    private fun loadAttendance(collectionName: String) {

        val today =SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "Admin: Loading $collectionName for date = $today")
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { allUsers ->
                Log.d(TAG, "Admin: Total users = ${allUsers.size()}")

                for (userDoc in allUsers) {

                    val uid = userDoc.id
                    Log.d(TAG, "Admin: Found UID = $uid")

                    db.collection(collectionName)
                        .document(uid)
                        .collection("dates")
                        .document(today.trim())
                        .get()
                        .addOnSuccessListener { doc ->

                            if (!doc.exists()) {
                                Log.w(TAG, "Admin: No attendance today for UID=$uid")
                                return@addOnSuccessListener
                            }

                            val markIn = doc.getString("markInLoc") ?: ""
                            val markOut = doc.getString("markOutLoc") ?: ""
                            val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
                            val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false

                            Log.d(
                                TAG,
                                "Admin: UID=$uid | In=$isMarkedIn | Out=$isMarkedOut"
                            )

                            if (isMarkedIn && markIn.isNotEmpty()) {
                                val (lat, lng) = markIn.split(",").map { it.trim().toDouble() }
                                val loc = LatLng(lat, lng)

                                Log.d(TAG, "Admin: Adding YELLOW IN dot → $uid")


                                if (isMarkedIn && isMarkedOut && markOut.isNotEmpty()) {
                                    val (latO, lngO) = markOut.split(",")
                                        .map { it.trim().toDouble() }
                                    val locO = LatLng(latO, lngO)

                                    Log.d(TAG, "Admin: Adding BLUE OUT dot → $uid")

                                }
                            }
                        }
                }
            }
    }
}

