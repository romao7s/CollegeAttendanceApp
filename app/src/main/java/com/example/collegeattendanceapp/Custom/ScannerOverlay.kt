package com.example.collegeattendanceapp.Custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.math.min

class ScannerOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    enum class ScanType { QR, BARCODE }

    var scanType: ScanType = ScanType.QR
    private val laserPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var laserY = 0f
    private val laserAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000L
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            laserY = it.animatedFraction
            invalidate()
        }
    }

    private val cornerPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#00000000")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()

        val (left, top, right, bottom) = when (scanType) {
            ScanType.QR -> {
                val size = min(overlayWidth, overlayHeight) * 1f
                val l = (overlayWidth - size) / 2
                val t = (overlayHeight - size) / 2
                arrayOf(l, t, l + size, t + size)
            }

            ScanType.BARCODE -> {
                val boxWidth = overlayWidth * 0.85f
                val boxHeight = overlayHeight * 0.25f
                val l = (overlayWidth - boxWidth) / 2
                val t = (overlayHeight - boxHeight) / 2
                arrayOf(l, t, l + boxWidth, t + boxHeight)
            }
        }

        val rect = RectF(left, top, right, bottom)

        canvas.drawRect(0f, 0f, overlayWidth, overlayHeight, backgroundPaint)

        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        canvas.drawRect(rect, clearPaint)

        val cornerLength = 60f

        canvas.drawLine(left, top, left + cornerLength, top, cornerPaint)
        canvas.drawLine(left, top, left, top + cornerLength, cornerPaint)

        canvas.drawLine(right, top, right - cornerLength, top, cornerPaint)
        canvas.drawLine(right, top, right, top + cornerLength, cornerPaint)

        canvas.drawLine(left, bottom, left + cornerLength, bottom, cornerPaint)
        canvas.drawLine(left, bottom, left, bottom - cornerLength, cornerPaint)

        canvas.drawLine(right, bottom, right - cornerLength, bottom, cornerPaint)
        canvas.drawLine(right, bottom, right, bottom - cornerLength, cornerPaint)

        val laserPosition = top + (bottom - top) * laserY
        canvas.drawLine(left + 10f, laserPosition, right - 10f, laserPosition, laserPaint)
    }

    fun updateScanTypeFromDetectedFormat(format: Int) {

        scanType = when (format) {
            Barcode.FORMAT_QR_CODE -> ScanType.QR
            else -> ScanType.BARCODE
        }
        invalidate()
    }
    fun animateShapeChange() {
        val scaleAnimation = ScaleAnimation(
            1f, 1.05f, 1f, 1.05f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 300
        scaleAnimation.repeatMode = Animation.REVERSE
        scaleAnimation.repeatCount = 1
        startAnimation(scaleAnimation)
    }


    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        laserAnimator.start()
    }
}