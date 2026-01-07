package com.example.collegeattendanceapp.staff

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.databinding.ActivityScanQrBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.collegeattendanceapp.Custom.ScannerOverlay
import com.example.collegeattendanceapp.student.StudentActivity

import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ScanQrActivity : AppCompatActivity() {
    private var resultShown = false
    private var isFlashOn = false
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private val locationPermissionCode = 1001
    private val CAMERA_REQUEST_CODE = 2002
    private lateinit var cameraManager: CameraManager
    lateinit var binding: ActivityScanQrBinding

    private var cameraId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityScanQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val scannerOverlay = findViewById<ScannerOverlay>(R.id.scannerOverlay)
        scannerOverlay.apply {
            scanType = ScannerOverlay.ScanType.QR
            animateShapeChange()
            invalidate()
        }
//        startCamera()
      checkCameraPermission()

    }



//    private fun checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            checkCameraPermission()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                locationPermissionCode
//            )
//        }
//    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    Toast.makeText(
                        this,
                        "Camera permission is required to mark attendance",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        checkCameraPermission()
//    }
    private fun isWithinAllowedRadius(
        currentLat: Double,
        currentLon: Double,
        targetLat: Double,
        targetLon: Double,
        radiusInMeters: Float
    ): Boolean {
        val currentLocation = Location("").apply {
            latitude = currentLat
            longitude = currentLon
        }
        val targetLocation = Location("").apply {
            latitude = targetLat
            longitude = targetLon
        }
        val distance = currentLocation.distanceTo(targetLocation)
        Log.d("LocationCheck", "Distance from office: $distance m")
        return distance <= radiusInMeters
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val scanner = BarcodeScanning.getClient()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                val mediaImage = imageProxy?.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
//                            toggleFlashlight(requireContext(),true)
//                            Handler(Looper.getMainLooper()).postDelayed({
//                                toggleFlashlight(requireContext(),false)
//                            }, 1000)


                            for (barcode in barcodes) {
                                if (!resultShown) {
                                    resultShown = true

                                    val scannedValue = barcode.rawValue.toString()
                                  if  ( scannedValue=="123456"){
                                      startActivity(Intent(this, FillDetailActivity::class.java))
                                      finish()
                                  }
                                    //handleScannedCode(scannedValue)
                                }
                            }

                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                binding.btnflash.visibility= View.VISIBLE
                binding.btnflash.setOnClickListener {
                    isFlashOn = !isFlashOn

                    camera.cameraControl.enableTorch(isFlashOn)
                    binding.btnflash.setImageResource(

                        if (isFlashOn) R.drawable.camera_flash_on_svgrepo_com
                        else R.drawable.camera_flash_off_svgrepo_com

                    )
                }

            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
                Toast.makeText(this, "Failed to start camera/flash", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }




//    fun toggleFlashlight(context: Context, turnOn: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val cameraManager =
//                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//            try {
//                val cameraId = cameraManager.cameraIdList[0]
//                cameraManager.setTorchMode(cameraId, turnOn)
//            } catch (e: Exception) {
//                Log.e("Flashlight error", "fash light error", e)
//
//            }
//
//        }
//    }





    private fun checkIfLate(time: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val markIn = sdf.parse(time)
        val lateLimit = sdf.parse("09:10:00")
        return markIn!!.after(lateLimit)
    }

    private fun checkIfAfterNoon(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 6
    }
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentFormattedTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            cameraManager.setTorchMode(cameraId, false)
        } catch (_: Exception) {}
    }


}



