package com.example.collegeattendanceapp.main

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.collegeattendanceapp.R
//import com.example.collegeattendanceapp.admin.AdminActivity
import com.example.collegeattendanceapp.admin.AdminHomeActivity
import com.example.collegeattendanceapp.databinding.FragmentSignInBinding
import com.example.collegeattendanceapp.staff.StaffActivity
import com.example.collegeattendanceapp.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    lateinit var binding: FragmentSignInBinding
    lateinit var mainActivity: MainActivity
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity=activity as MainActivity
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding.tvSignUp.setOnClickListener {
             findNavController().navigate(R.id.signUpFragment)
         }
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val prefs = mainActivity.getSharedPreferences("admin_prefs", MODE_PRIVATE)
        val adminLoggedBefore = prefs.getBoolean("isAdminLoggedIn", false)

        if (adminLoggedBefore) {
            val intent = Intent(mainActivity, AdminHomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.btnSignIn.setOnClickListener {
            val email = binding.etemail.text.toString().trim()
            val password = binding.etpassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(mainActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility=View.GONE

                return@setOnClickListener
            }



            if (email == "admin@gmail.com" && password == "admin") {
                    binding.progressBar.visibility=View.VISIBLE

                val prefs = requireActivity().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("isAdminLoggedIn", true).apply()


                Toast.makeText(mainActivity, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility=View.GONE

                val intent = Intent(mainActivity, AdminHomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                    binding.progressBar.visibility=View.VISIBLE

                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                binding.progressBar.visibility=View.GONE
                                Toast.makeText(mainActivity, "No user data found", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val role = document.getString("role")

                            when (role) {
                                "Student" -> {
                                    Toast.makeText(mainActivity, "Welcome Student", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(mainActivity, StudentActivity::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }

                                "Staff" -> {
                                    Toast.makeText(mainActivity, "Welcome Staff", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(mainActivity, StaffActivity::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()

                                }
                                else -> {
                                    binding.progressBar.visibility=View.GONE

                                    Toast.makeText(mainActivity, "Unknown role", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            binding.progressBar.visibility=View.GONE

                            Toast.makeText(mainActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility=View.GONE

                    Toast.makeText(mainActivity, "Login failed: Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
        }
        var isPasswordVisible = false


        binding.etpassword.setOnTouchListener { _, event ->
            if (event.rawX >= (binding.etpassword.right - binding.etpassword.compoundDrawables[2].bounds.width())) {

                isPasswordVisible = !isPasswordVisible

                if (isPasswordVisible) {
                    binding.etpassword.inputType =
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etpassword.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.eye_slash_svgrepo_com, 0
                    )
                } else {
                    binding.etpassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etpassword.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.eye_svgrepo_com, 0
                    )
                }

                binding.etpassword.setSelection(binding.etpassword.text?.length ?: 0)
                true
            } else false
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}