package com.example.notification_overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import androidx.cardview.widget.CardView
import android.util.TypedValue

class NotificationOverlay(private val context: Context) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private lateinit var layout: View
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private val handler = Handler(Looper.getMainLooper())

    fun show(message: String) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dpToPx(16)
            y = dpToPx(32)
        }

        layout = createNotificationLayout()
        textView.text = message

        windowManager.addView(layout, params)

    }

    private fun createNotificationLayout(): View {
        return CardView(context).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setCardBackgroundColor(Color.parseColor("#FF424242"))
            
            val contentLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            }

            imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
                    marginEnd = dpToPx(16)
                    gravity = Gravity.CENTER_VERTICAL
                }
                setImageResource(R.drawable.mawaqit_logo)
            }
            contentLayout.addView(imageView)

            textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                setTextColor(Color.WHITE)
                textSize = 16f
                maxWidth = dpToPx(240)
            }
            contentLayout.addView(textView)

            addView(contentLayout)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    fun hide() {
        if (::layout.isInitialized) {
            handler.removeCallbacksAndMessages(null)
            windowManager.removeView(layout)
        }
    }
}