# Prompt Cheatsheet (Bảng lệnh mồi AI chuẩn)

Sử dụng các prompt này để AI hiểu chính xác mong muốn của bạn mà không bị lệch hướng.

## 1. Khởi tạo dự án hoàn toàn mới
> `@[/vp-brainstorm] Tôi muốn xây dựng hệ thống [Tên dự án]. Hãy chạy quy trình --architect để xác định trước kiến trúc, sau đó bắt đầu thiết lập.`

## 2. Dọn dẹp Code / Refactoring (An toàn)
> `@[/vp-audit] Hãy rà soát toàn bộ dự án hiện tại, tìm các đoạn code không sử dụng (dead code) hoặc các file đang vi phạm nguyên tắc Separation of Concerns trong file 01-PROJECT_ARCHITECTURE_TEMPLATE.md. Vui lòng hỏi tôi trước khi xoá bất kỳ thứ gì.`

## 3. Sửa lỗi logic phức tạp
> `@[/vp-debug] File [tên-file.cpp/py] đang bị lỗi [mô tả lỗi]. Hãy kiểm tra xem lỗi này có liên quan đến hệ thống event-loop hay thư viện ngoại vi không.`

## 4. Fix lỗi UI/CSS nhanh gọn (Kết hợp với Agentation)
*(Khi bạn dùng tính năng copy của `agentation`, bạn sẽ nhận được một chuỗi JSON hoặc Markdown mô tả đúng phần tử bị lỗi).*
> `Tôi cần sửa giao diện. Dưới đây là thông tin element lấy từ Agentation:
> [Paste Markdown của Agentation vào đây]
> Hãy đổi màu nền thành xanh lá và thêm viền cong.`

## 5. Ra lệnh AI triển khai tự động
> `@[/vp-auto] Đã hoàn thành thảo luận ở brainstorm. Hãy tự động viết code cho toàn bộ Phase tiếp theo. Lưu ý tuân thủ file TRACKER.md.`

## 6. Cập nhật tiến độ & Viết tài liệu (Cuối ngày)
> `@[/vp-docs] Hãy tổng hợp lại những gì chúng ta đã sửa trong hôm nay và ghi vào file CHANGELOG.md. Đồng thời cập nhật trạng thái các task trong .viepilot/TRACKER.md.`
