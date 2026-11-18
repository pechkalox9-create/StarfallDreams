package com.heesoyam.berda
import android.app.Service
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var crosshair: View? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (panel != null) return START_STICKY
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(Color.rgb(28, 31, 39))
        }
        root.addView(TextView(this).apply { text = "BERDA • SURVIVAL HUD"; setTextColor(Color.WHITE); textSize = 16f })
        val status = TextView(this).apply { text = "Overlay ready"; setTextColor(Color.GREEN); setPadding(0, 12, 0, 12) }
        root.addView(status)
        root.addView(Button(this).apply {
            text = "CROSSHAIR: OFF"
            setOnClickListener {
                if (crosshair == null) {
                    val mark = CrosshairView()
                    val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    val p = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT)
                    wm.addView(mark, p)
                    crosshair = mark
                    text = "CROSSHAIR: ON"
                    status.text = "Crosshair enabled (visual overlay)"
                } else {
                    wm.removeView(crosshair)
                    crosshair = null
                    text = "CROSSHAIR: OFF"
                    status.text = "Crosshair disabled"
                }
            }
        })
        root.addView(Button(this).apply {
            text = "INJECT"
            setOnClickListener { status.text = "Demo button only — no game memory access" }
        })
        root.addView(Button(this).apply { text = "CLOSE"; setOnClickListener { stopSelf() } })
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.END; x = 24; y = 120
        }
        wm.addView(root, params)
        panel = root
        return START_STICKY
    }

    override fun onDestroy() {
        crosshair?.let { wm.removeView(it) }
        crosshair = null
        panel?.let { wm.removeView(it) }
        panel = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    private inner class CrosshairView : View(this@OverlayService) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GREEN; strokeWidth = 3f; style = Paint.Style.STROKE }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val x = width / 2f
            val y = height / 2f
            canvas.drawLine(x - 14, y, x - 5, y, paint)
            canvas.drawLine(x + 5, y, x + 14, y, paint)
            canvas.drawLine(x, y - 14, x, y - 5, paint)
            canvas.drawLine(x, y + 5, x, y + 14, paint)
        }
    }
}
