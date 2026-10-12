package vn.comtamcam.hungtb

/** Cau hinh co dinh — khong co man hinh cai dat. */
object CauHinh {
    /** Webhook dang nhan tu MacroDroid (workflow sip3k7PhCsmk1Nun). GET ?tieu_de=noi_dung(TPBank): ... */
    const val WEBHOOK_TIN = "https://comtamcambot.tino.page/webhook/ctc-nh-tb-7k3q9x"

    /** Webhook nhip tim (workflow canh app). GET ?cho=<so tin cho>&ban=<version>&nghe=<1/0> */
    const val WEBHOOK_NHIP = "https://comtamcambot.tino.page/webhook/ctc-app-tb-nhip-q8v2m7"

    /** Goi app duoc nghe. Chi nghe TPBank Mobile (TK1, TK rieng) va TPBank Biz (TK HKD). */
    val GOI_NGHE = setOf(
        "com.tpb.mb.gprsandroid", // TPBank Mobile
        "com.tpb.ebmobile"        // TPBank Biz
    )

    const val NGUON = "app"
    const val PHIEN_BAN = "1.1"
}
