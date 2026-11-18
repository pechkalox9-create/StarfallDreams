package com.heesoyam.berda

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(42), dp(24), dp(24))
            setBackgroundColor(Color.rgb(15, 18, 23))
        }
        root.addView(TextView(this).apply {
            text = "BERDA"
            textSize = 32f
            setTextColor(Color.WHITE)
            letterSpacing = 0.12f
        })
        root.addView(TextView(this).apply {
            text = "SURVIVAL HUD  /  ANDROID"
            textSize = 12f
            setTextColor(Color.rgb(128, 205, 145))
            setPadding(0, dp(4), 0, dp(28))
        })
        root.addView(TextView(this).apply {
            text = "Floating visual tools for com.heesoyam.berda. This app runs independently and does not modify the game process or read its memory."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(24))
        })

        root.addView(actionButton("GRANT OVERLAY + START HUD") {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
            } else {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        root.addView(actionButton("OPEN BERDA") {
            val launch = packageManager.getLaunchIntentForPackage("com.heesoyam.berda")
            if (launch != null) startActivity(launch)
            else statusText?.text = "Berda is not installed (or package is unavailable)."
        })
        statusText = TextView(this).apply {
            text = if (Settings.canDrawOverlays(this@MainActivity)) "Overlay permission: granted" else "Overlay permission: required"
            textSize = 13f
            setTextColor(Color.rgb(160, 170, 180))
            setPadding(0, dp(20), 0, 0)
        }
        root.addView(statusText)
        setContentView(root)
    }

    private var statusText: TextView? = null

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(54, 105, 68))
        setPadding(dp(12), dp(8), dp(12), dp(8))
        setOnClickListener { action() }
    }
}
