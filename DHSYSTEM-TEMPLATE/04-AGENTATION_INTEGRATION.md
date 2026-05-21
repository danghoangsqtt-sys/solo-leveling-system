# Tích Hợp Agentation (Visual Feedback)

Tài liệu này quy định việc sử dụng công cụ **agentation** (https://github.com/benjitaylor/agentation) cho tất cả các dự án Web Frontend nhằm tối ưu quá trình làm việc giữa Con người và AI.

## Môi trường áp dụng
- **BẮT BUỘC** áp dụng cho tất cả các dự án sử dụng React 18+ (Vite, NextJS,...).
- **KHÔNG ÁP DỤNG** cho các dự án Desktop GUI (Tkinter, PyQt) hoặc Embedded (C++ Firmware) vì công cụ này chỉ dành cho Web.

## Cách cài đặt tự động
Bất kỳ khi nào AI Agent khởi tạo một dự án Frontend React mới, Agent phải tự động chạy lệnh:
```bash
npm install agentation -D
```

## Cú pháp code chuẩn
Trong file gốc của dự án (ví dụ `App.tsx` hoặc `layout.tsx`), Agentation phải được bọc có điều kiện để **chỉ chạy trong môi trường phát triển (Development)**, không được xuất hiện ở bản Production.

**Ví dụ (Vite/React):**
```tsx
import { Agentation } from 'agentation';

function App() {
  return (
    <>
      <MainAppComponents />
      {/* Chỉ kích hoạt Agentation trong môi trường DEV */}
      {import.meta.env.DEV && <Agentation />}
    </>
  );
}
```

## Quy trình gỡ lỗi UI (User Workflow)
Khi bạn thấy một nút bấm, một khung hình hoặc văn bản bị lỗi hiển thị (CSS/UI) trên web:
1. Mở trang web đang chạy dev trên trình duyệt.
2. Click vào icon Agentation ở góc dưới.
3. Click vào phần tử bị lỗi trên màn hình.
4. Chọn Copy (Agentation sẽ copy cấu trúc HTML/CSS/Selector vào Clipboard).
5. Quay lại trình chat với ViePilot/AI, dán đoạn nội dung vừa copy và yêu cầu AI sửa.

> Lợi ích: AI sẽ nhìn thấy chính xác class, cấu trúc DOM và file mà không bị nhầm lẫn, giúp sửa đúng 100% chỉ trong 1 lệnh (Zero Hallucination).
