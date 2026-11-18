package com.starfall.berdaoverlay
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,48,28,28); setBackgroundColor(0xFF101820.toInt()) }
  root.addView(TextView(this).apply { text="BERDA  /  OVERLAY"; textSize=25f; setTextColor(0xFF72E0C1.toInt()) })
  root.addView(TextView(this).apply { text="Плавающая панель без root и вмешательства в игру."; textSize=15f; setTextColor(0xFFE5F1EF.toInt()); setPadding(0,18,0,24) })
  root.addView(Button(this).apply { text="Разрешить поверх окон"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName"))) } })
  root.addView(Button(this).apply { text="Запустить панель"; setOnClickListener { if(Settings.canDrawOverlays(this@MainActivity)) startService(Intent(this@MainActivity,OverlayService::class.java)) else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName"))) } })
  setContentView(root)
 }
}
