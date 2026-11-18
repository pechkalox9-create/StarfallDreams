package com.heesoyam.berda

import android.app.Service
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var crosshair: View? = null
    private var status: TextView? = null

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (panel != null) return START_STICKY
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(22, 27, 33))
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(73, 115, 82))
            }
        }
        val header = TextView(this).apply {
            text = "BERDA  /  HUD"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(header)
        root.addView(TextView(this).apply {
            text = "VISUAL OVERLAY"
            textSize = 10f
            setTextColor(Color.rgb(125, 196, 139))
            setPadding(0, 0, 0, dp(8))
        })
        status = TextView(this).apply {
            text = "STATUS: READY"
            textSize = 12f
            setTextColor(Color.rgb(175, 205, 180))
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(status)

        root.addView(controlButton("CROSSHAIR: OFF") { button ->
            if (crosshair == null) {
                val mark = CrosshairView()
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                wm.addView(mark, params)
                crosshair = mark
                button.text = "CROSSHAIR: ON"
                status?.text = "STATUS: CROSSHAIR ACTIVE"
            } else {
                removeCrosshair()
                button.text = "CROSSHAIR: OFF"
                status?.text = "STATUS: READY"
            }
        })

        root.addView(controlButton("INJECT  /  DEMO") {
            status?.text = "DEMO ONLY — no game code or memory is accessed"
        })
        root.addView(controlButton("OPEN BERDA") {
            val launch = packageManager.getLaunchIntentForPackage("com.heesoyam.berda")
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launch)
            } else status?.text = "BERDA NOT FOUND"
        })
        root.addView(controlButton("CLOSE HUD") { stopSelf() })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(18)
            y = dp(110)
        }
        header.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX - (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        wm.updateViewLayout(root, params)
                        return true
                    }
                }
                return false
            }
        })
        wm.addView(root, params)
        panel = root
        return START_STICKY
    }

    private fun controlButton(label: String, action: (Button) -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 12f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(48, 78, 56))
        setOnClickListener { action(this) }
    }

    private fun removeCrosshair() {
        crosshair?.let { runCatching { wm.removeView(it) } }
        crosshair = null
    }

    override fun onDestroy() {
        removeCrosshair()
        panel?.let { runCatching { wm.removeView(it) } }
        panel = null
        status = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private inner class CrosshairView : View(this@OverlayService) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            strokeWidth = dp(2).toFloat()
        }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val arm = dp(13).toFloat()
            val gap = dp(5).toFloat()
            canvas.drawLine(cx - arm, cy, cx - gap, cy, paint)
            canvas.drawLine(cx + gap, cy, cx + arm, cy, paint)
            canvas.drawLine(cx, cy - arm, cx, cy - gap, paint)
            canvas.drawLine(cx, cy + gap, cx, cy + arm, paint)
        }
    }
}
