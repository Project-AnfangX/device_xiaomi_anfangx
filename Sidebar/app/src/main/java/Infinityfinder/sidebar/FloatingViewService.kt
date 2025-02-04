//
// SPDX-FileCopyrightText: 2025 Littlenine & Uwugl
// SPDX-License-Identifier: GPL-3.0-only
//

package Infinityfinder.sidebar

import android.app.Instrumentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import android.net.Uri
import android.os.SystemClock
import android.os.Environment
import android.hardware.input.InputManager
import android.view.KeyEvent
import java.io.File
import java.io.FileOutputStream
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.core.content.ContextCompat

class FloatingViewService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var params: WindowManager.LayoutParams = WindowManager.LayoutParams()  // init params
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mediaProjection: MediaProjection

    override fun onCreate() {
        super.onCreate()
	setupFloatingWindow()
        setupButtonClickListeners()
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_view, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }

        windowManager.addView(floatingView, params)

        setupTouchListener()
    }

    private fun setupTouchListener() {
        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialTouchX = 0f

	    override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
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
        with(floatingView) {
            findViewById<ImageButton>(R.id.btn_volume_up)?.setOnClickListener {
                adjustVolume(AudioManager.ADJUST_RAISE)
            }
            findViewById<ImageButton>(R.id.btn_volume_down)?.setOnClickListener {
                adjustVolume(AudioManager.ADJUST_LOWER)
            }
            findViewById<ImageButton>(R.id.btn_power)?.apply {
                setOnClickListener { handlePowerAction(false) }
                setOnLongClickListener {
                    handlePowerAction(true)
                    true
                }
            }
            findViewById<ImageButton>(R.id.btn_screenshot)?.setOnClickListener {
                takeSystemScreenshot()
            }
            findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
                sendKeyEvent(KeyEvent.KEYCODE_BACK)
            }
            findViewById<ImageButton>(R.id.btn_home)?.setOnClickListener {
                sendKeyEvent(KeyEvent.KEYCODE_HOME)
            }
        }
    }

    private fun adjustVolume(direction: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun handlePowerAction(isLongPress: Boolean) {
        try {
            Runtime.getRuntime().exec("input keyevent 26")
        } catch (e: Exception) {
            Toast.makeText(this, "无法执行锁屏命令", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeSystemScreenshot() {
        try {
            val intent = Intent(this, ScreenshotActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "截图失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendKeyEvent(keyCode: Int, holdTime: Long = 0) {
        try {
            val inst = Instrumentation()
            inst.sendKeyDownUpSync(keyCode)
        } catch (e: SecurityException) {
            Toast.makeText(this, "需要系统权限", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?) = null
}
