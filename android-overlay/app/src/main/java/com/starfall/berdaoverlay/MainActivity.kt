package com.starfall.berdaoverlay

import android.app.Activity
import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 48, 28, 28)
            setBackgroundColor(0xFF101820.toInt())
        }
        root.addView(TextView(this).apply {
            text = "BERDA  /  OVERLAY"
            textSize = 25f
            setTextColor(0xFF72E0C1.toInt())
        })
        root.addView(TextView(this).apply {
            text = "Панель отображается только когда открыто Berda ($TARGET_PACKAGE). Без root."
            textSize = 15f
            setTextColor(0xFFE5F1EF.toInt())
            setPadding(0, 18, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "Разрешить поверх окон"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        root.addView(Button(this).apply {
            text = "Разрешить доступ к использованию приложений"
            setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Включить авто-показ для Berda"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else if (!hasUsageAccess()) {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } else {
                    startForegroundService(Intent(this@MainActivity, OverlayService::class.java))
                    finish()
                }
            }
        })
        setContentView(root)
    }

    companion object {
        const val TARGET_PACKAGE = "com.heesoyam.berda"
    }
}
