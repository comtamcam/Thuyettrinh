package vn.comtamcam.hungtb

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

/**
 * Moi ~60 phut: don dep, day hang doi neu con, bao nhip tim.
 * Tiet kiem pin: ngoai gio 6-22h khong bao; vua gui tin that trong 50 phut thi khong bao (tin = nhip).
 */
class NhipTimWorker(ctx: Context, p: WorkerParameters) : Worker(ctx, p) {
    override fun doWork(): Result {
        val hd = HangDoi.lay(applicationContext)
        hd.donDep()
        if (hd.soCho() > 0) {
            TbListener.instance?.quetLai()
            GuiWorker.day(applicationContext)
        }
        val gio = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (gio < 6 || gio >= 22) return Result.success()
        val guiCuoi = hd.lanGuiCuoi()
        if (System.currentTimeMillis() - guiCuoi < 50L * 60 * 1000) return Result.success()
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
