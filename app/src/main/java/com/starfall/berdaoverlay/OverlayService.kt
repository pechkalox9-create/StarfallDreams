package com.starfall.berdaoverlay
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
 private lateinit var wm: WindowManager
 private var panel: LinearLayout? = null
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  if (panel != null) return START_NOT_STICKY
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,14,18,14); setBackgroundColor(0xEE14242B.toInt()) }
  box.addView(TextView(this).apply { text="BERDA  ✦  MENU"; textSize=17f; setTextColor(0xFF72E0C1.toInt()) })
  listOf("VISUAL PRESET: OFF","CROSSHAIR PREVIEW: OFF","FPS COUNTER: OFF","CLOSE").forEach { item ->
   box.addView(TextView(this).apply {
    text=item; textSize=14f; setTextColor(0xFFE5F1EF.toInt()); setPadding(4,12,4,12)
    setOnClickListener { if(item=="CLOSE") stopSelf() else text=if(text.endsWith("OFF")) item.replace("OFF","ON") else item.replace("ON","OFF") }
   })
  }
  val lp=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.END; x=24; y=120 }
  wm.addView(box,lp); panel=box
  return START_NOT_STICKY
 }
 override fun onDestroy(){ panel?.let{wm.removeView(it)}; panel=null; super.onDestroy() }
 override fun onBind(intent: Intent?): IBinder? = null
}
