# Quy trình làm việc ViePilot (ViePilot Standard Workflow)

ViePilot là một hệ thống Agentic Workflow, cho phép bạn chia nhỏ quá trình phát triển phần mềm thành nhiều bước rõ ràng, giúp AI hiểu sâu và ít bị sai lệch.

## Vòng đời của một Task (Feature/Bugfix)

Một quy trình chuẩn luôn trải qua 3 giai đoạn:

### Giai đoạn 1: Lên ý tưởng & Lên kế hoạch (Planning)
1. **Dùng lệnh:** `@[/vp-brainstorm] Ý tưởng của tôi là...`
2. **AI sẽ:** Bật giao diện thảo luận, đặt câu hỏi cho bạn.
3. **Mục tiêu:** Chốt lại yêu cầu, thống nhất về UI/UX, CSDL. Toàn bộ nội dung sẽ được ghi vào file `docs/brainstorm/session-[date].md`.
4. **Phím tắt hữu ích:** Dùng `--ui` nếu bạn muốn AI render thử giao diện ra file HTML ảo; Dùng `--architect` để thiết kế luồng hệ thống.

### Giai đoạn 2: Kết tinh & Chuyển đổi thành Task (Crystallize)
1. **Dùng lệnh:** `@[/vp-crystallize]`
2. **AI sẽ:** Đọc file brainstorm vừa tạo ở Giai đoạn 1, sau đó đúc kết thành Kiến trúc, lộ trình và bẻ nhỏ thành các Task ghi vào thư mục `.viepilot/tasks/`.
3. **Mục tiêu:** Chuyển ý tưởng thô thành các tài liệu có cấu trúc mà máy (Agent) có thể thực thi chính xác.

### Giai đoạn 3: Tự động thực thi (Auto Execution)
1. **Dùng lệnh:** `@[/vp-auto]` (hoặc `@[/vp-task]`)
2. **AI sẽ:** Lấy các task từ `.viepilot/tasks/` ra và bắt đầu tự động code, tự động kiểm thử, sửa lỗi cho đến khi xong toàn bộ Phase.
3. **Mục tiêu:** Sinh ra mã nguồn (code) hoàn thiện.

---

## Các kỹ năng hỗ trợ khác (Utility Skills)
- `@[/vp-status]`: Hiển thị bảng tổng hợp tiến độ hiện tại của dự án.
- `@[/vp-audit]`: Yêu cầu AI kiểm toán lại toàn bộ mã nguồn xem có code rác, có tuân thủ đúng kiến trúc hay không.
- `@[/vp-debug]`: Khi hệ thống gặp lỗi khó, dùng skill này để AI đi vào trạng thái dò lỗi cực sâu, lưu vết các trạng thái.
- `@[/vp-docs]`: Yêu cầu AI cập nhật toàn bộ tài liệu dự án dựa trên số code hiện có.
