package vn.comtamcam.hungtb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Tin(
    val id: Long,
    val khoa: String,
    val noiDung: String,
    val tao: Long,
    val daGui: Boolean,
    val guiLuc: Long,
    val soLanLoi: Int,
    val loiCuoi: String
)

/** Hang doi tin trong may (SQLite). Chi xoa khi may chu tra 200. */
class HangDoi private constructor(ctx: Context) : SQLiteOpenHelper(ctx, "hangdoi.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE tin(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                khoa TEXT NOT NULL UNIQUE,
                noi_dung TEXT NOT NULL,
                tao INTEGER NOT NULL,
                da_gui INTEGER NOT NULL DEFAULT 0,
                gui_luc INTEGER NOT NULL DEFAULT 0,
                so_lan_loi INTEGER NOT NULL DEFAULT 0,
                loi_cuoi TEXT NOT NULL DEFAULT ''
            )"""
        )
        db.execSQL("CREATE INDEX idx_da_gui ON tin(da_gui, tao)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    /** Them tin neu khoa chua co. Tra ve true neu moi. */
    @Synchronized
    fun them(khoa: String, noiDung: String): Boolean {
        val cv = ContentValues().apply {
            put("khoa", khoa)
            put("noi_dung", noiDung)
            put("tao", System.currentTimeMillis())
        }
        val id = writableDatabase.insertWithOnConflict("tin", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        return id != -1L
    }

    @Synchronized
    fun choGui(): List<Tin> = doc("da_gui=0", "tao ASC", 200)

    @Synchronized
    fun ganNhat(n: Int): List<Tin> = doc(null, "tao DESC", n)

    @Synchronized
    fun soCho(): Int = demSql("SELECT COUNT(*) FROM tin WHERE da_gui=0")

    @Synchronized
    fun soGuiHomNay(): Int {
        val dau = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return demSql("SELECT COUNT(*) FROM tin WHERE da_gui=1 AND gui_luc>=$dau")
    }

    @Synchronized
    fun lanGuiCuoi(): Long = demSqlLong("SELECT MAX(gui_luc) FROM tin WHERE da_gui=1")

    @Synchronized
    fun danhDauGui(id: Long) {
        val cv = ContentValues().apply {
            put("da_gui", 1); put("gui_luc", System.currentTimeMillis()); put("loi_cuoi", "")
        }
        writableDatabase.update("tin", cv, "id=?", arrayOf(id.toString()))
    }

    @Synchronized
    fun ghiLoi(id: Long, loi: String) {
        writableDatabase.execSQL(
            "UPDATE tin SET so_lan_loi=so_lan_loi+1, loi_cuoi=? WHERE id=?",
            arrayOf(loi.take(200), id)
        )
    }

    /** "Gui lai tat ca": mo lai tin trong 48 gio de gui lai (may chu tu chong trung). */
    @Synchronized
    fun moLai48h(): Int {
        val moc = System.currentTimeMillis() - 48L * 3600 * 1000
        writableDatabase.execSQL("UPDATE tin SET da_gui=0 WHERE tao>=?", arrayOf(moc))
        return soCho()
    }

    /** Xoa tin da gui qua 14 ngay cho nhe may. */
    @Synchronized
    fun donDep() {
        val moc = System.currentTimeMillis() - 14L * 24 * 3600 * 1000
        writableDatabase.delete("tin", "da_gui=1 AND tao<?", arrayOf(moc.toString()))
    }

    private fun doc(where: String?, order: String, limit: Int): List<Tin> {
        val out = ArrayList<Tin>()
        readableDatabase.query("tin", null, where, null, null, null, order, limit.toString()).use { c ->
            while (c.moveToNext()) {
                out += Tin(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    khoa = c.getString(c.getColumnIndexOrThrow("khoa")),
                    noiDung = c.getString(c.getColumnIndexOrThrow("noi_dung")),
                    tao = c.getLong(c.getColumnIndexOrThrow("tao")),
                    daGui = c.getInt(c.getColumnIndexOrThrow("da_gui")) == 1,
                    guiLuc = c.getLong(c.getColumnIndexOrThrow("gui_luc")),
                    soLanLoi = c.getInt(c.getColumnIndexOrThrow("so_lan_loi")),
                    loiCuoi = c.getString(c.getColumnIndexOrThrow("loi_cuoi"))
                )
            }
        }
        return out
    }

    private fun demSql(sql: String): Int = readableDatabase.rawQuery(sql, null).use { c ->
        if (c.moveToFirst()) c.getInt(0) else 0
    }

    private fun demSqlLong(sql: String): Long = readableDatabase.rawQuery(sql, null).use { c ->
        if (c.moveToFirst()) c.getLong(0) else 0L
    }

    companion object {
        @Volatile private var inst: HangDoi? = null
        fun lay(ctx: Context): HangDoi = inst ?: synchronized(this) {
            inst ?: HangDoi(ctx.applicationContext).also { inst = it }
        }
    }
}
