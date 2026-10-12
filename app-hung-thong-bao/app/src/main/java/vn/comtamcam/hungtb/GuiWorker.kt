package vn.comtamcam.hungtb

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Day het tin cho gui len may chu. Tin nao 2xx thi danh dau da gui; con lai thu lai (backoff). */
class GuiWorker(ctx: Context, p: WorkerParameters) : Worker(ctx, p) {

    override fun doWork(): Result {
        val hd = HangDoi.lay(applicationContext)
        val ds = hd.choGui()
        if (ds.isEmpty()) return Result.success()
        var loi = 0
        for (tin in ds) {
            try {
                MayChu.guiTin(tin)
                hd.danhDauGui(tin.id)
                TrangThai.loiMang = ""
            } catch (e: Exception) {
                loi++
                val msg = e.message ?: e.javaClass.simpleName
                hd.ghiLoi(tin.id, msg)
                TrangThai.loiMang = msg
                Log.w("HungTB", "Gui loi ${tin.khoa}: $msg")
            }
        }
        return if (loi == 0) Result.success() else Result.retry()
    }

    companion object {
        const val TEN = "gui_tin"

        fun day(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<GuiWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(ctx).enqueueUniqueWork(TEN, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
        }
    }
}
