package com.example.collegeattendanceapp.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.admin.AdminActivity
import com.example.collegeattendanceapp.admin.AdminHomeActivity
import com.example.collegeattendanceapp.staff.StaffActivity
import com.example.collegeattendanceapp.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid

                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role") ?: ""

                            when (role) {
                                "Admin" -> {
                                    startActivity(Intent(this, AdminHomeActivity::class.java))
                                }

                                "Staff" -> {
                                    startActivity(Intent(this, StaffActivity::class.java))
                                }

                                "Student" -> {
                                    startActivity(Intent(this, StudentActivity::class.java))
                                }

                                else -> {
                                    startActivity(Intent(this, StudentActivity::class.java))
                                }
                            }
                            finish()
                        } else {
                            startActivity(Intent(this, StudentActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, StudentActivity::class.java))
                        finish()
                    }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }, 3000)
    }
}

