package vn.comtamcam.hungtb

/** Trang thai tam trong bo nho de man hinh chinh hien. */
object TrangThai {
    @Volatile var dangNghe: Boolean = false
    @Volatile var tinCuoi: String = ""
    @Volatile var nhipCuoi: Long = 0L
    @Volatile var loiMang: String = ""
}
