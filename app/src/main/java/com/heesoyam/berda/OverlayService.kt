package com.heesoyam.berda
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (panel != null) return START_STICKY
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(Color.rgb(28, 31, 39))
        }
        root.addView(TextView(this).apply { text = "BERDA • OVERLAY"; setTextColor(Color.WHITE); textSize = 16f })
        root.addView(TextView(this).apply { text = "Ready"; setTextColor(Color.GREEN); setPadding(0, 12, 0, 12) })
        root.addView(Button(this).apply {
            text = "INJECT"
            setOnClickListener { (root.getChildAt(1) as TextView).text = "Demo action only — no memory access" }
        })
        root.addView(Button(this).apply { text = "CLOSE"; setOnClickListener { stopSelf() } })
        val type = if (android.os.Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.END; x = 24; y = 120
        }
        wm.addView(root, params)
        panel = root
        return START_STICKY
    }
    override fun onDestroy() { panel?.let { wm.removeView(it) }; panel = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
