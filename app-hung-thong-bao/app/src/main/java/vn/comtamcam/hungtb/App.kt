package vn.comtamcam.hungtb

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class App : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_GUARD, "Đang hứng thông báo", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Biểu tượng cố định để hệ thống không tắt app"
                setShowBadge(false)
            }
        )
        // Nhip tim moi 60 phut (co the trễ 15 phut cho he thong gom chung voi app khac de tiet kiem pin),
        // chi khi co mang. Tin ngan hang gui len cung duoc may chu tinh la nhip tim.
        val hb = PeriodicWorkRequestBuilder<NhipTimWorker>(60, TimeUnit.MINUTES, 15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NhipTimWorker.TEN, ExistingPeriodicWorkPolicy.UPDATE, hb
        )
    }

    companion object {
        const val CHANNEL_GUARD = "guard"
    }
}
