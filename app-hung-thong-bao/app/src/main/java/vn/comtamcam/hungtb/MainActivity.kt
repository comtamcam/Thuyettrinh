package vn.comtamcam.hungtb

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvTrangThai: TextView
    private lateinit var tvSo: TextView
    private lateinit var tvDanhSach: TextView
    private lateinit var btnQuyen: Button
    private lateinit var btnPin: Button
    private lateinit var btnTuChay: Button
    private lateinit var btnGuiLai: Button
    private lateinit var btnKiemTra: Button

    private val lamMoi = object : Runnable {
        override fun run() { capNhat(); handler.postDelayed(this, 5000) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTrangThai = findViewById(R.id.tvTrangThai)
        tvSo = findViewById(R.id.tvSo)
        tvDanhSach = findViewById(R.id.tvDanhSach)
        btnQuyen = findViewById(R.id.btnQuyen)
        btnPin = findViewById(R.id.btnPin)
        btnTuChay = findViewById(R.id.btnTuChay)
        btnGuiLai = findViewById(R.id.btnGuiLai)
        btnKiemTra = findViewById(R.id.btnKiemTra)

        btnQuyen.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, "Bật \"Hứng thông báo Tấm Cám\" trong danh sách", Toast.LENGTH_LONG).show()
        }
        btnPin.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        btnTuChay.setOnClickListener { moTuKhoiChay() }
        btnGuiLai.setOnClickListener {
            val n = HangDoi.lay(this).moLai48h()
            TbListener.instance?.quetLai()
            GuiWorker.day(this)
            Toast.makeText(this, "Đang gửi lại $n tin (48 giờ gần nhất)", Toast.LENGTH_SHORT).show()
            capNhat()
        }
        btnKiemTra.setOnClickListener {
            btnKiemTra.isEnabled = false
            Thread {
                val kq = try {
                    MayChu.nhipTim(HangDoi.lay(this).soCho(), TrangThai.dangNghe)
                    TrangThai.nhipCuoi = System.currentTimeMillis()
                    TrangThai.loiMang = ""
                    "Máy chủ OK"
                } catch (e: Exception) { "Lỗi: ${e.message}" }
                runOnUiThread {
                    btnKiemTra.isEnabled = true
                    Toast.makeText(this, kq, Toast.LENGTH_LONG).show()
                    capNhat()
                }
            }.start()
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        if (!GuardService.dangChay) GuardService.batDau(this)
    }

    override fun onResume() {
        super.onResume()
        // Neu listener da duoc cap quyen nhung chua ket noi thi xin ket noi lai
        if (coQuyenNghe() && TbListener.instance == null) {
            try {
                android.service.notification.NotificationListenerService.requestRebind(ComponentName(this, TbListener::class.java))
            } catch (_: Exception) {}
        }
        handler.post(lamMoi)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(lamMoi)
    }

    private fun coQuyenNghe(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(packageName)
    }

    private fun capNhat() {
        val hd = HangDoi.lay(this)
        val quyen = coQuyenNghe()
        val nghe = TrangThai.dangNghe && TbListener.instance != null
        val pm = getSystemService(PowerManager::class.java)
        val pinOk = pm.isIgnoringBatteryOptimizations(packageName)

        val sb = StringBuilder()
        sb.append(if (!quyen) "🔴 CHƯA CÓ QUYỀN đọc thông báo — bấm nút bên dưới"
                  else if (!nghe) "🟡 Có quyền nhưng chưa kết nối — tắt/bật lại quyền hoặc mở lại app"
                  else "🟢 Đang nghe TPBank + TPBank Biz")
        sb.append('\n').append(if (pinOk) "🟢 Pin: không hạn chế" else "🟡 Pin: còn bị tối ưu — nên tắt")
        if (TrangThai.loiMang.isNotEmpty()) sb.append("\n🔴 Mạng: ").append(TrangThai.loiMang)
        tvTrangThai.text = sb

        val cuoi = hd.lanGuiCuoi()
        tvSo.text = "Chờ gửi: ${hd.soCho()}   ·   Đã gửi hôm nay: ${hd.soGuiHomNay()}\n" +
            "Gửi cuối: ${gio(cuoi)}   ·   Nhịp tim cuối: ${gio(TrangThai.nhipCuoi)}"

        btnQuyen.text = if (quyen) "Quyền đọc thông báo: ĐÃ CẤP" else "Cấp quyền đọc thông báo"
        btnPin.text = if (pinOk) "Pin: ĐÃ không hạn chế" else "Tắt tối ưu pin cho app"

        val f = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        tvDanhSach.text = hd.ganNhat(30).joinToString("\n\n") { t ->
            val tt = if (t.daGui) "✅ ${f.format(Date(t.guiLuc))}" else "⏳ chờ (${t.soLanLoi} lỗi) ${t.loiCuoi}"
            "$tt\n${t.noiDung.take(220)}"
        }.ifEmpty { "Chưa có tin nào." }
    }

    private fun gio(ms: Long): String =
        if (ms <= 0) "—" else SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(ms))

    /** Mo trang Tu khoi chay cua Xiaomi/HyperOS; may khac thi mo thong tin app. */
    private fun moTuKhoiChay() {
        val cacIntent = listOf(
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        )
        for (i in cacIntent) {
            try { startActivity(i); return } catch (_: Exception) {}
        }
    }
}
