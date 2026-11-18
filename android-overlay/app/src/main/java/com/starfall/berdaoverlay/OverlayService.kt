package com.starfall.berdaoverlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var panel: LinearLayout? = null
    private var injected = false

    private val monitor = object : Runnable {
        override fun run() {
            val shouldShow = isBerdaForeground()
            if (shouldShow && panel == null) showPanel()
            if (!shouldShow && panel != null) hidePanel()
            handler.postDelayed(this, 700)
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val channel = NotificationChannel("berda_overlay", "Berda Overlay", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = Notification.Builder(this, "berda_overlay")
            .setContentTitle("Berda Overlay активен")
            .setContentText("Панель появится только при открытии Berda")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(41, notification)
        handler.post(monitor)
    }

    private fun isBerdaForeground(): Boolean {
        val usage = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val events = usage.queryEvents(end - 5000, end)
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundPackage = event.packageName
            }
        }
        return foregroundPackage == MainActivity.TARGET_PACKAGE
    }

    private fun showPanel() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(0xEE14242B.toInt())
        }
        box.addView(TextView(this).apply {
            text = "BERDA  ✦  MENU"
            textSize = 17f
            setTextColor(0xFF72E0C1.toInt())
        })
        val inject = TextView(this).apply {
            text = "INJECT"
            textSize = 16f
            setTextColor(0xFF101820.toInt())
            setBackgroundColor(0xFF72E0C1.toInt())
            setPadding(24, 14, 24, 14)
            setOnClickListener {
                injected = !injected
                text = if (injected) "VISUAL MODE: ON" else "INJECT"
            }
        }
        box.addView(inject)
        listOf("VISUAL PRESET: OFF", "CROSSHAIR PREVIEW: OFF", "FPS COUNTER: OFF", "HIDE").forEach { item ->
            box.addView(TextView(this).apply {
                text = item
                textSize = 14f
                setTextColor(0xFFE5F1EF.toInt())
                setPadding(4, 12, 4, 12)
                setOnClickListener {
                    if (item == "HIDE") hidePanel()
                    else text = if (text.endsWith("OFF")) item.replace("OFF", "ON") else item.replace("ON", "OFF")
                }
            })
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 24; y = 120 }
        wm.addView(box, lp)
        panel = box
    }

    private fun hidePanel() {
        panel?.let { if (it.isAttachedToWindow) wm.removeView(it) }
        panel = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitor)
        hidePanel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
