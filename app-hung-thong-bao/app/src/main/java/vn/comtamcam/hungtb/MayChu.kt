package vn.comtamcam.hungtb

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** Goi may chu n8n. */
object MayChu {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /** Gui mot tin. Thanh cong khi HTTP 2xx. Nem loi neu khong. */
    fun guiTin(tin: Tin) {
        val url = CauHinh.WEBHOOK_TIN.toHttpUrl().newBuilder()
            .addQueryParameter("tieu_de", "noi_dung" + tin.noiDung)
            .addQueryParameter("nguon", CauHinh.NGUON)
            .addQueryParameter("khoa_app", tin.khoa)
            .build()
        // Webhook may chu nhan POST (giong MacroDroid): tham so tren query, body rong
        val req = Request.Builder().url(url).header("User-Agent", "hungtb/${CauHinh.PHIEN_BAN}")
            .post(ByteArray(0).toRequestBody(null)).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
        }
    }

    fun nhipTim(soCho: Int, dangNghe: Boolean) {
        val url = CauHinh.WEBHOOK_NHIP.toHttpUrl().newBuilder()
            .addQueryParameter("cho", soCho.toString())
            .addQueryParameter("nghe", if (dangNghe) "1" else "0")
            .addQueryParameter("ban", CauHinh.PHIEN_BAN)
            .build()
        val req = Request.Builder().url(url).header("User-Agent", "hungtb/${CauHinh.PHIEN_BAN}").get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
        }
    }
}
