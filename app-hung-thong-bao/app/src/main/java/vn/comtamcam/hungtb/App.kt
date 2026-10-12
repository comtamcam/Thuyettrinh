package vn.comtamcam.hungtb

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.work.Configuration
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
        // Nhip tim + don hang doi moi 15 phut
        val hb = PeriodicWorkRequestBuilder<NhipTimWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NhipTimWorker.TEN, ExistingPeriodicWorkPolicy.KEEP, hb
        )
    }

    companion object {
        const val CHANNEL_GUARD = "guard"
    }
}
