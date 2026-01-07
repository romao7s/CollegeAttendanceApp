package com.example.collegeattendanceapp.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.databinding.FragmentSignUpBinding
import com.example.collegeattendanceapp.staff.StaffActivity
import com.example.collegeattendanceapp.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
internal const val ARG_PARAM1 = "param1"
internal const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    lateinit var binding: FragmentSignUpBinding
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
        binding= FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }

        val roles = listOf("Select Role", "Student", "Staff")

        val adapter = object : ArrayAdapter<String>(
            mainActivity,
            R.layout.spinner_item,
            roles
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        binding.spinnerRole.adapter = adapter

        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val role = roles[position]
                when (role) {
                    "Student" -> {
                        binding.etStudentRollno.visibility = View.VISIBLE
                        binding.etStaffDepartment.visibility = View.GONE
                    }
                    "Staff" -> {
                        binding.etStudentRollno.visibility = View.GONE
                        binding.etStaffDepartment.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.etStudentRollno.visibility = View.GONE
                        binding.etStaffDepartment.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etname.text.toString().trim()
            val email = binding.etemail.text.toString().trim()
            val password = binding.etpassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            if (role == "Select Role") {
                Toast.makeText(mainActivity, "Please select a valid role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rollNo = binding.etStudentRollno.text.toString().trim()
            val department = binding.etStaffDepartment.text.toString().trim()
            if (name.isNullOrEmpty()   ){
                Toast.makeText(mainActivity, "Enter Required filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }
            if (password.isNullOrEmpty()){
                Toast.makeText(mainActivity, "Enter Password filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isNullOrEmpty()){
                Toast.makeText(mainActivity, "Enter Name filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (role == "Student" && rollNo.isEmpty()) {
                Toast.makeText(mainActivity, "Enter Roll No", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role == "Staff" && department.isEmpty()) {
                Toast.makeText(mainActivity, "Enter Department", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signupUser(name, email, password, role, rollNo, department)
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

    private fun signupUser(
        name: String,
        email: String,
        password: String,
        role: String,
        rollNo: String,
        department: String
    ) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to role,
                    "rollno" to rollNo,
                    "department" to department,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {

                        binding.progressBar.visibility = View.GONE

                        when (role) {
                            "Student" -> {
                                Toast.makeText(mainActivity, "Welcome Student", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(mainActivity, StudentActivity::class.java))
                            }
                            "Staff" -> {
                                Toast.makeText(mainActivity, "Welcome Staff", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(mainActivity, StaffActivity::class.java))
                            }
                        }
                        requireActivity().finish()
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(mainActivity, "Problem saving user info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(mainActivity, "Signup failed:  Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}