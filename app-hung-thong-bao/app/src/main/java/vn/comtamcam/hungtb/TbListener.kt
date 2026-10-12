package vn.comtamcam.hungtb

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Nghe thong bao TPBank / TPBank Biz. Moi dong co "SO GD" la mot giao dich.
 * Doc het moi truong text (ke ca thong bao gom nhom) de khong rot tin.
 */
class TbListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        TrangThai.dangNghe = true
        Log.i(TAG, "Listener ket noi")
        GuardService.batDau(this)
        quetLai()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        TrangThai.dangNghe = false
        instance = null
        Log.w(TAG, "Listener mat ket noi — xin ket noi lai")
        try { requestRebind(android.content.ComponentName(this, TbListener::class.java)) } catch (_: Exception) {}
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        xuLy(sbn)
    }

    /** Quet cac thong bao con nam tren thanh trang thai — gui bu tin chua gui. */
    fun quetLai(): Int {
        var moi = 0
        try {
            val ds = activeNotifications ?: return 0
            for (sbn in ds) moi += xuLy(sbn)
        } catch (e: Exception) {
            Log.w(TAG, "quetLai loi: ${e.message}")
        }
        if (moi > 0) GuiWorker.day(this)
        return moi
    }

    /** Tra ve so tin MOI them vao hang doi. */
    private fun xuLy(sbn: StatusBarNotification): Int {
        if (sbn.packageName !in CauHinh.GOI_NGHE) return 0
        val n = sbn.notification ?: return 0
        val ex = n.extras ?: return 0

        val cacText = LinkedHashSet<String>()
        fun them(cs: CharSequence?) {
            val s = cs?.toString()?.trim()
            if (!s.isNullOrEmpty()) cacText += s
        }
        them(ex.getCharSequence(Notification.EXTRA_TEXT))
        them(ex.getCharSequence(Notification.EXTRA_BIG_TEXT))
        them(ex.getCharSequence(Notification.EXTRA_SUB_TEXT))
        them(ex.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))
        ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { them(it) }
        @Suppress("DEPRECATION")
        ex.getParcelableArray(Notification.EXTRA_MESSAGES)?.forEach { p ->
            (p as? Bundle)?.let { them(it.getCharSequence("text")) }
        }
        if (cacText.isEmpty()) return 0

        val tieuDe = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val hangDoi = HangDoi.lay(this)
        var moi = 0
        for (text in cacText) {
            for (giaoDich in tachGiaoDich(text)) {
                val khoa = khoaChongTrung(giaoDich) ?: continue
                val noiDung = chuanHoa(giaoDich, tieuDe)
                if (hangDoi.them(khoa, noiDung)) {
                    moi++
                    TrangThai.tinCuoi = noiDung
                    Log.i(TAG, "Tin moi: $khoa")
                }
            }
        }
        if (moi > 0) GuiWorker.day(this)
        return moi
    }

    companion object {
        private const val TAG = "HungTB"
        @Volatile var instance: TbListener? = null

        private val R_SOGD = Regex("""SO\s*GD\s*:?\s*([A-Z0-9]{6,})""", RegexOption.IGNORE_CASE)
        private val R_TK = Regex("""TK\s*:?\s*x*([0-9]{4,})""", RegexOption.IGNORE_CASE)
        private val R_PS = Regex("""PS\s*:?\s*([+-])""", RegexOption.IGNORE_CASE)

        /**
         * Mot text co the chua nhieu giao dich (TPBank Biz gom nhieu dong). Tach theo moc "(TPBank)".
         * Dong khong co SO GD (vd "Giao dich thanh cong!") se bi loai o khoaChongTrung.
         */
        fun tachGiaoDich(text: String): List<String> {
            val t = text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
            val moc = Regex("""\(TPBank\)\s*:?""", RegexOption.IGNORE_CASE)
            val viTri = moc.findAll(t).map { it.range.first }.toList()
            if (viTri.size <= 1) return listOf(t)
            val out = ArrayList<String>()
            for (i in viTri.indices) {
                val a = viTri[i]
                val b = if (i + 1 < viTri.size) viTri[i + 1] else t.length
                out += t.substring(a, b).trim()
            }
            return out
        }

        /** Khoa = SO GD + duoi TK + chieu, vi chuyen giua 2 TK cua chu sinh 2 tin cung SO GD. */
        fun khoaChongTrung(gd: String): String? {
            val soGd = R_SOGD.find(gd)?.groupValues?.get(1)?.uppercase() ?: return null
            val tk = R_TK.find(gd)?.groupValues?.get(1)?.takeLast(7) ?: "x"
            val chieu = when (R_PS.find(gd)?.groupValues?.get(1)) { "+" -> "vao"; "-" -> "ra"; else -> "k" }
            return "${soGd}_${tk}_$chieu"
        }

        /** Dua ve dung dang MacroDroid dang gui: "(TPBank): <noi dung>". */
        fun chuanHoa(gd: String, tieuDe: String): String {
            var s = gd
            if (!Regex("""^\(TPBank\)""", RegexOption.IGNORE_CASE).containsMatchIn(s)) {
                s = if (tieuDe.isNotEmpty() && tieuDe.contains("TPBank", true) && tieuDe.startsWith("(")) "$tieuDe $s"
                else "(TPBank): $s"
            }
            return s
        }
    }
}
