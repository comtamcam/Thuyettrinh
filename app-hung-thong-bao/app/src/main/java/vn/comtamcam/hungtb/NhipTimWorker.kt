package vn.comtamcam.hungtb

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Moi 15 phut: don dep, quet lai thong bao, day hang doi, bao nhip tim len may chu. */
class NhipTimWorker(ctx: Context, p: WorkerParameters) : Worker(ctx, p) {
    override fun doWork(): Result {
        val hd = HangDoi.lay(applicationContext)
        hd.donDep()
        TbListener.instance?.quetLai()
        if (hd.soCho() > 0) GuiWorker.day(applicationContext)
        GuardService.batDau(applicationContext)
        return try {
            MayChu.nhipTim(hd.soCho(), TrangThai.dangNghe)
            TrangThai.nhipCuoi = System.currentTimeMillis()
            Result.success()
        } catch (e: Exception) {
            TrangThai.loiMang = e.message ?: "loi mang"
            Result.retry()
        }
    }

    companion object { const val TEN = "nhip_tim" }
}
