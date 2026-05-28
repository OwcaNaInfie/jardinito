package pl.edu.pb.jardinito.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.View
import androidx.compose.ui.graphics.toArgb
import pl.edu.pb.jardinito.ui.theme.AppColors
import android.os.Build
import android.graphics.Point
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.core.content.res.ResourcesCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.manager.FocusSessionManager
import javax.inject.Inject

@AndroidEntryPoint
class FocusOverlayService : android.app.Service() {

    @Inject
    lateinit var sessionManager: FocusSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var gracePeriodJob: Job? = null
    private var failCountdownJob: Job? = null
    private var remainingSeconds = FAIL_COUNTDOWN_SECONDS

    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            if (sessionManager.isTimerRunning.value) {
                startGracePeriod()
            }
        }
        override fun onStart(owner: LifecycleOwner) {
            cancelAllCountdowns()
            removeOverlay()
        }
    }

    companion object {
        const val ACTION_START = "pl.edu.pb.jardinito.FOCUS_START"
        const val ACTION_STOP  = "pl.edu.pb.jardinito.FOCUS_STOP"
        private const val FOREGROUND_CHANNEL = "focus_foreground_channel"
        private const val ALERT_CHANNEL      = "focus_alert_channel"
        private const val FOREGROUND_NOTIF_ID = 101
        private const val FAIL_NOTIF_ID       = 102
        private const val GRACE_PERIOD_MS     = 1_000L
        private const val FAIL_COUNTDOWN_SECONDS = 15
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannels()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification())
            ACTION_STOP  -> {
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        cancelAllCountdowns()
        removeOverlay()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
        serviceScope.cancel()
    }

    // ==========================================
    // COUNTDOWN
    // ==========================================

    private fun startGracePeriod() {
        gracePeriodJob?.cancel()
        gracePeriodJob = serviceScope.launch {
            delay(GRACE_PERIOD_MS)
            if (sessionManager.isTimerRunning.value) {
                showOverlay()
                startFailCountdown()
            }
        }
    }

    private fun startFailCountdown() {
        failCountdownJob?.cancel()
        remainingSeconds = FAIL_COUNTDOWN_SECONDS
        failCountdownJob = serviceScope.launch {
            while (remainingSeconds > 0) {
                delay(1_000L)
                remainingSeconds--
                updateCountdownText()
            }
            removeOverlay()
            sessionManager.triggerFail()
            sendFailNotification()
        }
    }

    private fun cancelAllCountdowns() {
        gracePeriodJob?.cancel()
        failCountdownJob?.cancel()
    }

    // ==========================================
    // OVERLAY
    // ==========================================

    private fun showOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) return

        val view = buildOverlayView()
        overlayView = view

        val screenWidth: Int
        val screenHeight: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            val realSize = android.graphics.Point()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealSize(realSize)
            screenWidth = realSize.x
            screenHeight = realSize.y
        }

        val params = WindowManager.LayoutParams(
            screenWidth,
            screenHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        windowManager.addView(view, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
            overlayView = null
        }
    }

    private fun buildOverlayView(): LinearLayout {
        val dp = { value: Int -> (value * resources.displayMetrics.density).toInt() }

        val poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppins_semibold)
        val poppinsRegular  = ResourcesCompat.getFont(this, R.font.poppins_regular)

        val colorPrimary50   = AppColors.Primary50.toArgb()
        val colorPrimary900  = AppColors.Primary900.toArgb()
        val colorNeutralGray = AppColors.NeutralGray.toArgb()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.argb(160, 0, 0, 0))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = GradientDrawable().apply {
                setColor(colorPrimary50)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(2), colorPrimary900)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(24)
                marginEnd = dp(24)
            }
        }

        val title = TextView(this).apply {
            text = getString(R.string.focus_overlay_title)
            textSize = 18f
            typeface = poppinsRegular
            setTextColor(colorNeutralGray)
            setPadding(0, 0, 0, dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val message = TextView(this).apply {
            text = getString(R.string.focus_overlay_message)
            textSize = 14f
            typeface = poppinsSemiBold
            setTextColor(colorNeutralGray)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val countdownText = TextView(this).apply {
            tag = "countdown"
            text = getString(R.string.focus_overlay_countdown, FAIL_COUNTDOWN_SECONDS)
            textSize = 12f
            typeface = poppinsRegular
            setTextColor(colorPrimary900)
            setPadding(0, dp(8), 0, dp(24))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val buttonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val dismissButton = Button(this).apply {
            text = getString(R.string.close)
            textSize = 14f
            typeface = poppinsRegular
            setTextColor(AppColors.Primary300.toArgb())
            background = GradientDrawable().apply {
                setColor(AppColors.Primary100.toArgb())
                cornerRadius = dp(50).toFloat()
            }
            elevation = 0f
            stateListAnimator = null
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(30)
            ).apply { rightMargin = dp(8) }
            setOnClickListener {
                cancelAllCountdowns()
                removeOverlay()
                sessionManager.triggerFail()
                sendFailNotification()
            }
        }

        val returnButton = Button(this).apply {
            text = getString(R.string.focus_overlay_return)
            textSize = 14f
            typeface = poppinsRegular
            setTextColor(AppColors.NeutralDark.toArgb())
            background = GradientDrawable().apply {
                setColor(AppColors.Primary900.toArgb())
                cornerRadius = dp(50).toFloat()
            }
            elevation = 0f
            stateListAnimator = null
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(30)
            )
            setOnClickListener {
                cancelAllCountdowns()
                removeOverlay()
                packageManager.getLaunchIntentForPackage(packageName)
                    ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP }
                    ?.let { startActivity(it) }
            }
        }

        buttonsRow.addView(dismissButton)
        buttonsRow.addView(returnButton)
        card.addView(title)
        card.addView(message)
        card.addView(countdownText)
        card.addView(buttonsRow)
        root.addView(card)
        return root
    }

    private fun updateCountdownText() {
        overlayView?.findViewWithTag<TextView>("countdown")
            ?.let { it.text = getString(R.string.focus_overlay_countdown, remainingSeconds)  }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(FOREGROUND_CHANNEL, getString(R.string.notif_focus_channel_name), NotificationManager.IMPORTANCE_LOW)
                .apply { description = getString(R.string.notif_focus_channel_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(ALERT_CHANNEL, getString(R.string.notif_alert_channel_name), NotificationManager.IMPORTANCE_HIGH)
                .apply { description = getString(R.string.notif_alert_channel_desc) }
        )
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_focus_title))
            .setContentText(getString(R.string.notif_focus_text))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun sendFailNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this, 1,
            packageManager.getLaunchIntentForPackage(packageName)
                ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_fail_title))
            .setContentText(getString(R.string.notif_fail_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java).notify(FAIL_NOTIF_ID, notification)
    }
}