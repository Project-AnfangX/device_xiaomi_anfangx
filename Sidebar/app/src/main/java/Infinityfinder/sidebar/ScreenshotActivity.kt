//
// SPDX-FileCopyrightText: 2025 Littlenine & Uwugl
// SPDX-License-Identifier: GPL-3.0-only
//

package Infinityfinder.sidebar

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.PixelCopy
import android.view.SurfaceView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class ScreenshotActivity : Activity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1001)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            captureScreenshot()
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun captureScreenshot() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val surfaceView = SurfaceView(this)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(surfaceView.holder.surface, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmap(bitmap)
            }
            finish()
        }, handler)
    }
    private fun saveBitmap(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(null), "screenshot.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Toast.makeText(this, "截图已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
