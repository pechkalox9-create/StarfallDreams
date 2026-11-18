package com.heesoyam.berda
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
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        layout.addView(TextView(this).apply { text = "BERDA Overlay"; textSize = 24f })
        layout.addView(TextView(this).apply { text = "Standalone floating panel. No game process or memory access." })
        layout.addView(Button(this).apply {
            text = "Enable overlay"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) startService(Intent(this@MainActivity, OverlayService::class.java))
                else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        setContentView(layout)
    }
}
