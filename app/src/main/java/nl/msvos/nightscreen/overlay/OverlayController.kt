package nl.msvos.nightscreen.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverlayController(context: Context) {
    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(WindowManager::class.java)

    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    val isShowing: Boolean
        get() = overlayView != null

    fun show(brightnessPercent: Int): Result<Unit> = runCatching {
        check(Settings.canDrawOverlays(appContext)) {
            "Display-over-other-apps permission is not granted"
        }

        if (overlayView != null) {
            update(brightnessPercent).getOrThrow()
            return@runCatching
        }

        val view = View(appContext).apply {
            setBackgroundColor(Color.BLACK)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            alpha = windowAlpha(brightnessPercent)
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        windowManager.addView(view, params)
        overlayView = view
        layoutParams = params
    }

    fun update(brightnessPercent: Int): Result<Unit> = runCatching {
        val view = checkNotNull(overlayView) { "Dimming overlay is not visible" }
        val params = checkNotNull(layoutParams) { "Dimming overlay has no layout parameters" }
        val newAlpha = windowAlpha(brightnessPercent)

        if (params.alpha != newAlpha) {
            params.alpha = newAlpha
            windowManager.updateViewLayout(view, params)
        }
    }

    fun hide(): Result<Unit> {
        val view = overlayView
        overlayView = null
        layoutParams = null

        return if (view == null) {
            Result.success(Unit)
        } else {
            runCatching {
                windowManager.removeView(view)
            }
        }
    }

    private fun windowAlpha(brightnessPercent: Int): Float =
        BrightnessMapper.toWindowAlpha(brightnessPercent)
}
