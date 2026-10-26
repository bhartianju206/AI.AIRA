package com.example.screenshare

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopCapturing()
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_PAUSE) {
            _isPaused.value = true
            updateNotification("Screen Share Paused")
            return START_STICKY
        } else if (action == ACTION_RESUME) {
            _isPaused.value = false
            updateNotification("Aira AI Screen Share Active")
            return START_STICKY
        }

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (resultCode == 0 || resultData == null) {
            Log.e(TAG, "Missing resultCode or resultData in intent")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceWithNotification()

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (projectionManager == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
        if (mediaProjection == null) {
            Log.e(TAG, "Failed to get MediaProjection")
            stopSelf()
            return START_NOT_STICKY
        }

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopCapturing()
            }
        }, handler)

        setupVirtualDisplay()
        _isCapturing.value = true
        _isPaused.value = false

        return START_STICKY
    }

    @SuppressLint("WrongConstant")
    private fun setupVirtualDisplay() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        // Scale resolution to 720p maximum to ensure fast capture and optimal Gemini token payload
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val scale = minOf(1.0f, 720f / screenWidth)
        val width = ((screenWidth * scale).toInt() / 2) * 2
        val height = ((screenHeight * scale).toInt() / 2) * 2
        val density = (metrics.densityDpi * scale).toInt()

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            if (_isPaused.value) return@setOnImageAvailableListener
            try {
                reader.acquireLatestImage()?.use { image ->
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width

                    val bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    // Crop to exact width and height if padding exists
                    val finalBitmap = if (rowPadding != 0) {
                        Bitmap.createBitmap(bitmap, 0, 0, width, height)
                    } else {
                        bitmap
                    }

                    _latestBitmap.value = finalBitmap
                    _lastCaptureTimestamp.value = System.currentTimeMillis()

                    // Compress to JPEG Base64 for Gemini Vision
                    val outputStream = ByteArrayOutputStream()
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    _latestBase64Jpeg.value = base64
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error acquiring frame from ImageReader", e)
            }
        }, handler)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AiraScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification("Aira AI Screen Share Active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aira AI v1.2 Screen Share")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_aira_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aira Screen Share",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active status when Aira AI is sharing and inspecting screen content"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopCapturing() {
        _isCapturing.value = false
        _isPaused.value = false
        _latestBitmap.value = null
        _latestBase64Jpeg.value = null

        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing capture resources", e)
        }
    }

    override fun onDestroy() {
        stopCapturing()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val CHANNEL_ID = "aira_screen_share_channel"
        private const val NOTIFICATION_ID = 2001

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        const val ACTION_STOP = "com.example.action.STOP_SCREEN_SHARE"
        const val ACTION_PAUSE = "com.example.action.PAUSE_SCREEN_SHARE"
        const val ACTION_RESUME = "com.example.action.RESUME_SCREEN_SHARE"

        private val _isCapturing = MutableStateFlow(false)
        val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _latestBitmap = MutableStateFlow<Bitmap?>(null)
        val latestBitmap: StateFlow<Bitmap?> = _latestBitmap.asStateFlow()

        private val _latestBase64Jpeg = MutableStateFlow<String?>(null)
        val latestBase64Jpeg: StateFlow<String?> = _latestBase64Jpeg.asStateFlow()

        private val _lastCaptureTimestamp = MutableStateFlow(0L)
        val lastCaptureTimestamp: StateFlow<Long> = _lastCaptureTimestamp.asStateFlow()

        fun start(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
