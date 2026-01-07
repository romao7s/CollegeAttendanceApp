package com.example.collegeattendanceapp.staff

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.lifecycle.lifecycleScope
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.RequestCode
import com.example.collegeattendanceapp.RequestCode.REQUEST_STORAGE_IMAGE
import com.example.collegeattendanceapp.databinding.ActivityFillDetailBinding
import com.example.collegeattendanceapp.dataclass.TeacherAll
import com.example.collegeattendanceapp.dataclass.VisitorData
import com.example.collegeattendanceapp.retrofit.ApiService
import com.example.reminderapp.retrofit.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.File

//    val token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2OTI5NGNmNjdjZTljMzJkOWY4NjgzYjkiLCJlbWFpbCI6Imd1YXJkMUBnbWFpbC5jb20iLCJuYW1lIjoiJDJiJDEwJDdrY2pyT25KeUpIeEpEWnFDY0xldWVCLmJzamFESFROOFQ2dmRyVjdWQXR2bVJ0YWx1M0ZXIiwidXNlclR5cGUiOjIsImlhdCI6MTc2NTAxMzcwMn0.aO52jAgrtU6nFeAPam00Bz-INH7bvmsCiMkoGfetqww"
class FillDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFillDetailBinding
    lateinit var selectedUserFile: File


    private var teacherId: String? = null
    lateinit var userImageFile: File
    lateinit var idProofFile: File
     lateinit var vehicleImageFile: File

    private val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2OTI5NGNmNjdjZTljMzJkOWY4NjgzYjkiLCJlbWFpbCI6Imd1YXJkMUBnbWFpbC5jb20iLCJuYW1lIjoiJDJiJDEwJDdrY2pyT25KeUpIeEpEWnFDY0xldWVCLmJzamFESFROOFQ2dmRyVjdWQXR2bVJ0YWx1M0ZXIiwidXNlclR5cGUiOjIsImlhdCI6MTc2NTAxMzcwMn0.aO52jAgrtU6nFeAPam00Bz-INH7bvmsCiMkoGfetqww"
    private var currentUploadType = ""
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImage.launch("image/*")
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->

            val file = uriToFile(this, selectedUri)

            when (currentUploadType) {

                "ID_PROOF" -> {
                    idProofFile = file
                    binding.etProofId.setText("ID Selected ✔")
                }

                "USER_IMG" -> {
                    userImageFile = file
                    binding.etYourImg.setText("User Image Selected ✔")
                }

                "VEHICLE_IMG" -> {
                    vehicleImageFile = file
                    binding.etVehicleId.setText("Vehicle Image Selected ✔")
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFillDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadTeachers()
        binding.btnback.setOnClickListener {
            finish()
        }
        binding.btnSubmit.setOnClickListener {
            if (validateFields()) {
                submitVisitor()
            }
        }

        binding.etYourImg.setOnClickListener {
            currentUploadType = "USER_IMG"
            checkPermission()
        }

        binding.etProofId.setOnClickListener {
            currentUploadType = "ID_PROOF"
            checkPermission()
        }

        binding.etVehicleId.setOnClickListener {
            currentUploadType = "VEHICLE_IMG"
            checkPermission()
        }

    }


    private fun validateFields(): Boolean {
        var isValid = true

        fun err(v: EditText, msg: String) {
            v.error = msg
            isValid = false
        }

        fun err2(v: TextView, msg: String) {
            v.error = msg
            isValid = false
        }

        if (binding.etName.text.isNullOrBlank()) err(binding.etName, "Enter name")
        if (binding.etEmail.text.isNullOrBlank()) err(binding.etEmail, "Enter email")
        if (binding.etReason.text.isNullOrBlank()) err(binding.etReason, "Enter reason")
        if (binding.etAddress.text.isNullOrBlank()) err(binding.etAddress, "Enter address")
        if (binding.etContact.text.isNullOrBlank()) err(binding.etContact, "Enter contact number")
        if (binding.etNumberOfPeople.text.isNullOrBlank()) err(binding.etNumberOfPeople, "Enter total number of people")

        if (userImageFile == null) err2(binding.etYourImg, "Upload your image")
        if (idProofFile == null) err2(binding.etProofId, "Upload proof ID")


        if (teacherId.isNullOrEmpty()) {
            Toast.makeText(this, "Please select a teacher!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    fun uriToFile(context: Context, uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("img_", ".jpg", context.filesDir)
        input.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return file
    }



    private fun submitVisitor() {
        val VehiclePart = createOptionalPart("vehicle_image_url")

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.visitorAdd(
                    token = token,

                    username = toPart(binding.etName.text.toString()),
                    email = toPart(binding.etEmail.text.toString()),
                    purpose = toPart(binding.etReason.text.toString()),
                    contact = toPart(binding.etContact.text.toString()),
                    address = toPart(binding.etAddress.text.toString()),
                    numPeople = toPart(binding.etNumberOfPeople.text.toString()),
                    vehicleNumber = toPart(binding.etVehcleNumber.text.toString()),

                    vehicleImage = VehiclePart,
                    idProofImage = createImagePart(idProofFile, "id_proof_url"),
                    userImage = createImagePart(userImageFile, "user_image_url"),

                    notes = toPart(binding.etNotes.text.toString()),
                    whomToMeet = toPart(teacherId!!)
                )



                if (response.isSuccessful) {
                    Toast.makeText(this@FillDetailActivity, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    Log.d("ApiResponse","${response.body()?.message}",)
                    Log.d("ApiResponse","Vehicle Image :$,userImage:$ ,id :$",)

                    finish()
                } else {
                    Toast.makeText(this@FillDetailActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FillDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()

                        Log.d("ApiResponse","Error: ${e.message}",)

            }
        }
    }

    fun createImagePart(file: File, fieldName: String): MultipartBody.Part {
        val req = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, req)
    }
    fun createOptionalPart(fileName: String?): MultipartBody.Part? {
        if (fileName.isNullOrEmpty()) return null

        val file = File(filesDir, fileName)
        if (!file.exists()) return null

        val req = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, req)
    }


    fun toPart(value: String) =
        value.toRequestBody("text/plain".toMediaType())


    private fun loadTeachers() {

        Log.d("API_CALL", "Calling GET teacher/all with token: $token")

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTeachers(token)

                Log.d("API_RESPONSE", "Raw: $response")

                if (response.isSuccessful) {

                    val body = response.body()
                    Log.d("API_RESPONSE_BODY", "Body: $body")

                    val list = body?.data ?: arrayListOf()

                    Log.d("API_LIST", "Teachers Count = ${list.size}")
                    Log.d("API_LIST", "Teachers = $list")

                    if (list.isEmpty()) {
                        Toast.makeText(this@FillDetailActivity, "No teachers found!", Toast.LENGTH_SHORT).show()
                    }

                    val names = list.map { it.name ?: "Unknown" }

                    val adapter = ArrayAdapter(
                        this@FillDetailActivity,
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWhomtoMeet.adapter = adapter

                    binding.spinnerWhomtoMeet.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                teacherId = list[position].Id ?: ""
                                Log.d("API_SELECT", "Selected Teacher ID: $teacherId")
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                } else {
                    Log.e("API_ERROR", "Error Code: ${response.code()}")
                    Log.e("API_ERROR", "Error Body: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Exception: ${e.localizedMessage}")
            }
        }
    }



    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            pickImage.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.REQUEST_CODE_PERMISSIONS) {
            val rejected = java.util.ArrayList<String>()
            val neverAskAgain = java.util.ArrayList<String>()
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permissions[i]
                        )
                    ) {
                        rejected.add(permissions[i])
                    } else neverAskAgain.add(permissions[i])
                }
            }
            if (!rejected.isEmpty()) {
                Log.e("Image", "in not empty")
                val builder = AlertDialog.Builder(this)

                builder.setTitle("Permissions Needed")
                    .setMessage("Following permissions required to upload image")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Retry"
                    ) { dialog, id ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                            ),
                            RequestCode.REQUEST_CODE_PERMISSIONS
                        )
                    }.setNegativeButton(
                        "No"
                    ) { dialog, which ->
                        Toast.makeText(
                            this,
                            resources.getText(R.string.cannot_upload_image),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                builder.create().show()
            } else {
                Log.e("ImageUpload", "in empty")

                showChooser()
            }
        }
    }
    private fun showChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_STORAGE_IMAGE)
    }




}
