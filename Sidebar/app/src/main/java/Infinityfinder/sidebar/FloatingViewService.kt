//
// SPDX-FileCopyrightText: 2025 Littlenine & Uwugl
// SPDX-License-Identifier: GPL-3.0-only
//

package Infinityfinder.sidebar

import android.app.Instrumentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import android.net.Uri
import android.os.SystemClock

class FloatingViewService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var params: WindowManager.LayoutParams = WindowManager.LayoutParams()  // init params
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_view, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.START or Gravity.TOP
        params.x = 0
        params.y = 0

        windowManager.addView(floatingView, params)

        setupButtonClickListeners()

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialTouchX = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialTouchX = event.rawX
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupButtonClickListeners() {
        floatingView.findViewById<ImageButton>(R.id.btn_volume_up)?.setOnClickListener {
            adjustVolume(true)
        }
        floatingView.findViewById<ImageButton>(R.id.btn_volume_down)?.setOnClickListener {
            adjustVolume(false)
        }
        floatingView.findViewById<ImageButton>(R.id.btn_power)?.apply {
            setOnClickListener { handlePowerButton(false) }
            setOnLongClickListener {
                handlePowerButton(true)
                true
            }
        }
        floatingView.findViewById<ImageButton>(R.id.btn_screenshot)?.setOnClickListener {
            takeScreenshot()
        }
        floatingView.findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            performBack()
        }
        floatingView.findViewById<ImageButton>(R.id.btn_home)?.setOnClickListener {
            goToHome()
        }
    }

    private fun adjustVolume(increase: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    private fun handlePowerButton(longPress: Boolean) {
        if (longPress) {
            startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            return
        }

        // get PowerManager instance
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Control screen using `WakeLock` on Android 12+
            val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyApp::ScreenLock")
            wakeLock.acquire(10 * 60 * 1000L)  // WakeLock will valid for 10min
        }
    }

    private fun takeScreenshot() {
        try {
            val process = Runtime.getRuntime().exec("screencap -p /sdcard/Pictures/screenshot.png")
            process.waitFor()
            notifyGallery("/sdcard/Pictures/screenshot.png")
        } catch (e: Exception) {
            Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyGallery(filePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val fileUri = Uri.parse("file://$filePath")
        mediaScanIntent.data = fileUri
        sendBroadcast(mediaScanIntent)
    }

    private fun performBack() {
        val inst = Instrumentation()
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
    }

    private fun goToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?) = null
}
