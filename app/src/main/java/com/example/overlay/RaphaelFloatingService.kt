package com.example.overlay

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * RAPHAEL System Overlay Service: Floating draggable sacred sigil orb via SYSTEM_ALERT_WINDOW
 * that stays on top of all Android apps, games, and home screen with physics-based edge snapping.
 */
class RaphaelFloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    companion object {
        const val CHANNEL_ID = "raphael_overlay_channel"
        const val NOTIFICATION_ID = 902

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, RaphaelFloatingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RaphaelFloatingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupFloatingSigilOrb()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RAPHAEL System Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active floating sacred sigil orb for instant access to Raphael"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RAPHAEL Active")
            .setContentText("Lord of Wisdom is guarding your system. Tap to summon.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun setupFloatingSigilOrb() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val sizePx = (68 * resources.displayMetrics.density).toInt()

        layoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        val container = FrameLayout(this)

        // Custom drawn sacred flower sigil view
        val sigilView = object : View(this) {
            private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3300E5FF")
                style = Paint.Style.FILL
            }
            private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00E5FF")
                strokeWidth = 2f * resources.displayMetrics.density
                style = Paint.Style.STROKE
            }
            private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#09090D")
                style = Paint.Style.FILL
            }
            private val petalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8000E5FF")
                strokeWidth = 1.5f * resources.displayMetrics.density
                style = Paint.Style.STROKE
            }
            private val coreDiamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val cx = width / 2f
                val cy = height / 2f
                val radius = (Math.min(width, height) / 2f) * 0.85f

                // Outer Aura
                canvas.drawCircle(cx, cy, radius * 1.12f, auraPaint)
                // Obsidian Background
                canvas.drawCircle(cx, cy, radius, bgPaint)
                // Cyan Boundary Ring
                canvas.drawCircle(cx, cy, radius, borderPaint)

                // 8 Geometric Sacred Petals
                val petalRadius = radius * 0.65f
                for (i in 0 until 8) {
                    val angle = (i * 45 * PI / 180.0).toFloat()
                    val px = cx + petalRadius * cos(angle)
                    val py = cy + petalRadius * sin(angle)
                    val path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(px, py)
                    }
                    canvas.drawPath(path, petalPaint)
                    canvas.drawCircle(px, py, 2.5f * resources.displayMetrics.density, coreDiamondPaint)
                }

                // Center Core Diamond
                val diamondSize = radius * 0.28f
                val diamond = Path().apply {
                    moveTo(cx, cy - diamondSize)
                    lineTo(cx + diamondSize * 0.65f, cy)
                    lineTo(cx, cy + diamondSize)
                    lineTo(cx - diamondSize * 0.65f, cy)
                    close()
                }
                canvas.drawPath(diamond, coreDiamondPaint)
            }
        }

        container.addView(
            sigilView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(deltaX) > 12 || Math.abs(deltaY) > 12) {
                        isClick = false
                    }
                    layoutParams?.x = initialX + deltaX
                    layoutParams?.y = initialY + deltaY
                    windowManager?.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Summon Raphael
                        val launchIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(launchIntent)
                    } else {
                        // Physics-based edge snapping
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = container
        windowManager?.addView(floatingView, layoutParams)
    }

    /**
     * Smooth deceleration physics snapping the orb to the left or right screen edge.
     */
    private fun snapToEdge() {
        val currentX = layoutParams?.x ?: return
        val screenWidth = resources.displayMetrics.widthWidth()
        val targetX = if (currentX + (floatingView?.width ?: 0) / 2 < screenWidth / 2) {
            20 // Snap left
        } else {
            screenWidth - (floatingView?.width ?: 0) - 20 // Snap right
        }

        val animator = ValueAnimator.ofInt(currentX, targetX)
        animator.duration = 250
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { anim ->
            layoutParams?.x = anim.animatedValue as Int
            floatingView?.let { windowManager?.updateViewLayout(it, layoutParams) }
        }
        animator.start()
    }

    private fun android.util.DisplayMetrics.widthWidth(): Int {
        return widthPixels
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        floatingView = null
    }
}
