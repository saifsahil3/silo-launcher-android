package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.PassThroughManager

class PassThroughOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        if (Settings.canDrawOverlays(this)) {
            showFloatingOverlay()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pass-Through Mode Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating return button during Pass-Through mode"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXIT_PASSTHROUGH", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MorphLauncher Pass-Through Active")
            .setContentText("Tap floating overlay or notification to return")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showFloatingOverlay() {
        if (overlayView != null) return

        val density = resources.displayMetrics.density
        val metrics = resources.displayMetrics
        val sizePx = (56 * density).toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            alpha = 0.5f // 50% transparency for subtle floating overlay

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF00695C.toInt()) // Deep Pass-Through teal
                setStroke((2 * density).toInt(), 0xFF80CBC4.toInt()) // Cyan accent border
            }
            background = shape
            elevation = (12 * density)
        }

        val icon = ImageView(this).apply {
            setImageResource(com.example.R.mipmap.ic_launcher_round)
            layoutParams = LinearLayout.LayoutParams(
                (38 * density).toInt(),
                (38 * density).toInt()
            )
        }

        layout.addView(icon)

        val startX = (metrics.widthPixels - sizePx - (20 * density)).toInt().coerceAtLeast(0)
        val startY = (metrics.heightPixels - sizePx - (100 * density)).toInt().coerceAtLeast(0)

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = startX
            y = startY
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = true

        layout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    try {
                        windowManager?.updateViewLayout(layout, params)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        layout.performClick()
                    }
                    true
                }
                else -> false
            }
        }

        layout.setOnClickListener {
            exitPassThroughMode()
        }

        try {
            windowManager?.addView(layout, params)
            overlayView = layout
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun exitPassThroughMode() {
        PassThroughManager.setPassThroughActive(this, false)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXIT_PASSTHROUGH", true)
        }
        startActivity(intent)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    companion object {
        private const val CHANNEL_ID = "passthrough_overlay_channel"
        private const val NOTIFICATION_ID = 9001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, PassThroughOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        context.startForegroundService(intent)
                    } catch (e: Throwable) {
                        context.startService(intent)
                    }
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, PassThroughOverlayService::class.java)
                context.stopService(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
