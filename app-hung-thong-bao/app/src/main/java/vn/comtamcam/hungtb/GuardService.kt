package vn.comtamcam.hungtb

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/** Dich vu nen co bieu tuong co dinh de HyperOS khong giet app. */
class GuardService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n: Notification = NotificationCompat.Builder(this, App.CHANNEL_GUARD)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("Đang hứng thông báo TPBank")
            .setContentText("Tin ngân hàng tự gửi về máy chủ Cơm Tấm Cám")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, n)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun batDau(ctx: Context) {
            try {
                ContextCompat.startForegroundService(ctx, Intent(ctx, GuardService::class.java))
            } catch (_: Exception) {
                // Android 12+ co the chan khoi dong tu nen; worker/listener se thu lai sau
            }
        }
    }
}
