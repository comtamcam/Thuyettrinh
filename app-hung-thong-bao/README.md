# Hứng thông báo Tấm Cám (Android)

App nghe thông báo **TPBank** và **TPBank Biz**, tách từng giao dịch theo `SO GD`, lưu hàng đợi trong máy, gửi về máy chủ n8n của quán (đúng dạng MacroDroid cũ). Chỉ xóa khỏi hàng đợi khi máy chủ trả 200; mỗi 15 phút quét lại thông báo còn trên máy, gửi bù và báo nhịp tim.

- Tải APK: Release `hungtb-latest` của repo này (GitHub Actions dựng tự động mỗi khi push vào `app-hung-thong-bao/`).
- Sau khi cài: mở app → **Cấp quyền đọc thông báo** → **Tắt tối ưu pin** → **Bật Tự khởi chạy** → khóa app trong đa nhiệm.
