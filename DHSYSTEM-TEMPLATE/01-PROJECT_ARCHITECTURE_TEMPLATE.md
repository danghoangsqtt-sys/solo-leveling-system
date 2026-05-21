# Tiêu Chuẩn Cấu Trúc Hệ Thống & Dự Án (Global Project Architecture Template)

Tài liệu này định nghĩa kiến trúc tiêu chuẩn cho mọi dự án phần mềm và firmware, được thiết kế đặc biệt để tối ưu hóa cho AI Agent (Vibe Coding). Việc tuân thủ nghiêm ngặt tài liệu này giúp:
1. **Chống ảo giác (Anti-Hallucination):** Giới hạn phạm vi, giúp AI không bị lạc lối trong code base lớn.
2. **Tính Module Hóa (Modularity):** Phân tách rõ ràng trách nhiệm của từng thành phần.
3. **Dễ bảo trì & mở rộng:** Áp dụng cho cả dự án mới và tái cấu trúc dự án hiện tại.

---

## 1. Kiến Trúc Thư Mục Tiêu Chuẩn (Standard Directory Structure)

Mọi dự án phải tuân theo cấu trúc phân tầng (Layered Architecture) sau:

```text
[Tên-Dự-Án]/
├── .viepilot/                 # [AI ONLY] Bối cảnh, tasks, trạng thái hệ thống ViePilot.
├── docs/                      # [CRITICAL] Tài liệu hệ thống (Documentation-First).
│   ├── 00-context/            # Mục tiêu dự án, đối tượng người dùng.
│   ├── brainstorm/            # Log các buổi thảo luận ý tưởng (vp-brainstorm).
│   ├── architecture/          # Quyết định kiến trúc (C4 Model, Sequence Diagram, ERD).
│   └── api/                   # Giao thức giao tiếp (REST, GraphQL, Serial, WebSocket).
├── backend/                   # (Hoặc `firmware/` / `core/`) Logic nghiệp vụ chính.
│   ├── src/
│   │   ├── controllers/       # Xử lý request đầu vào, gọi tới services.
│   │   ├── services/          # Chứa logic nghiệp vụ lõi (Core Business Logic).
│   │   ├── models/            # Schema, Data models (Database, ORM).
│   │   ├── config/            # Cấu hình môi trường (DB, Ports, Keys).
│   │   └── utils/             # Các hàm tiện ích dùng chung.
├── frontend/                  # (Hoặc `ui/` / `gui/`) Giao diện người dùng.
│   ├── src/
│   │   ├── components/        # Các UI component độc lập, có thể tái sử dụng.
│   │   ├── pages/             # Các trang (views) cấu thành từ nhiều components.
│   │   ├── hooks/             # Custom hooks (React) hoặc State management.
│   │   ├── services/          # Gọi API từ backend (Fetch/Axios).
│   │   └── assets/            # CSS, Hình ảnh, Fonts (sử dụng DESIGN.md).
├── tests/                     # Mã kiểm thử độc lập (Unit, Integration, E2E).
├── infrastructure/            # (Tùy chọn) Docker, CI/CD, Terraform scripts.
├── brain.md                   # Nguồn chân lý cốt lõi, giới thiệu tổng quan hệ thống cho AI.
└── README.md                  # Hướng dẫn setup và khởi động dự án cho người mới.
```

---

## 2. Nguyên Tắc Thiết Kế Hệ Thống (System Design Principles)

### 2.1. Separation of Concerns (Phân tách ranh giới)
- **Frontend và Backend là hai thực thể tách biệt:** Phải giao tiếp hoàn toàn thông qua API (REST, WebSocket) hoặc chuẩn giao tiếp vật lý (Serial COM). Không được trộn lẫn UI logic và Core logic.
- **Dumb Components & Smart Containers:** Các UI Components chỉ nhận `props` và render (Dumb), logic lấy dữ liệu nằm ở Pages hoặc State Managers (Smart).

### 2.2. Nguồn Chân Lý Duy Nhất (Single Source of Truth)
- **Kiến trúc:** Phải được vẽ và mô tả trong thư mục `docs/architecture/` (sử dụng Mermaid).
- **Thiết kế giao diện:** Phải được định nghĩa thông qua tệp `DESIGN.md` theo chuẩn token.
- **Trạng thái dự án:** Theo dõi bằng `.viepilot/TRACKER.md` và `ROADMAP.md`.

### 2.3. Tránh Ảo Giác Bằng Định Tuyến Dữ Liệu Rõ Ràng (Anti-Hallucination Data Flow)
AI thường bị ảo giác khi luồng dữ liệu không rõ ràng. Cần áp dụng:
1. **Strong Typing (Kiểu dữ liệu mạnh):** Bắt buộc sử dụng TypeScript (cho TS/JS), Pydantic (cho Python), hoặc Struct rõ ràng (cho C++). Không dùng `any`.
2. **Dependency Injection:** Truyền cấu hình và instances vào modules thay vì hardcode bên trong, giúp AI dễ mock dữ liệu khi viết test.
3. **Centralized Error Handling:** Xử lý lỗi tại một nơi duy nhất. Các hàm service luôn ném lỗi (throw/raise), controller bắt lỗi và trả về chuẩn API.

---

## 3. Luồng Giao Tiếp Chuẩn Giữa AI Và Hệ Thống

Để đảm bảo an toàn và tính chính xác, AI phải tuân theo luồng sau khi làm việc trên dự án:

1. **Hiểu Bối Cảnh (Context Discovery):**
   - Đọc `brain.md` để hiểu tổng quan.
   - Đọc `docs/architecture/system-overview.mermaid` để biết luồng chạy.
   - Kiểm tra `SKILL.md` (nếu dùng các tool đặc thù như GitNexus).
2. **Phân Tích Tác Động (Impact Analysis - BẮT BUỘC):**
   - Trước khi sửa bất kỳ logic lõi nào (hàm, class, file), AI **phải** chạy phân tích tác động (vd: `gitnexus_impact`).
   - Nếu phát hiện rủi ro CAO (HIGH) hoặc NGHIÊM TRỌNG (CRITICAL), AI **phải** dừng lại và báo cáo cho người dùng.
3. **Thực Thi Code (Documentation-First Execution):**
   - Phải xác nhận tài liệu thiết kế (`docs/`) đã phản ánh đúng tính năng chuẩn bị làm.
   - Áp dụng các thay đổi ở code base. Mỗi thay đổi cần thực hiện nhỏ (atomic) và test ngay.
4. **Xác Minh (Verification):**
   - Không được thay đổi tên file hoặc tên hàm bằng cách "Tìm và Thay thế" (Find & Replace) một cách mù quáng. Cần nắm rõ Call Graph.
   - Chạy lệnh lint, test hoặc kiểm tra Git (`gitnexus_detect_changes()`) trước khi báo cáo hoàn tất.

---

## 4. Quy Chuẩn Đặt Tên & Viết Code (Coding Standards)

- **Tên File/Thư Mục:**
  - Kebab-case cho thư mục và tên file (ví dụ: `user-controller.ts`, `data-processing.py`).
  - PascalCase cho các file chứa Class hoặc React Components (ví dụ: `UserProfile.tsx`).
- **Comments & Documentation:**
  - Không comment lại code cũ. Xóa thẳng tay, Git sẽ lưu trữ (Zero Dead Code).
  - Comment phải giải thích **TẠI SAO (Why)** thay vì **CÁI GÌ (What)** (vì AI tự hiểu được code đang làm gì).
  - Mọi hàm public/export cần có Docstring/JSDoc chuẩn mực bao gồm Parameters, Returns, và Exceptions.

---

## 5. Áp Dụng Tái Cấu Trúc Dự Án (Refactoring Existing Projects)

Khi áp dụng tài liệu này để cấu trúc lại dự án cũ, tuân thủ các bước:
1. **Step 1:** Tạo thư mục `docs/`, chuyển tất cả ghi chú hiện có vào.
2. **Step 2:** Phân tách rõ mã nguồn thành `backend/` và `frontend/` (hoặc `firmware/` và `ui/`).
3. **Step 3:** Dọn dẹp mã rác (Dead code) và xóa các thư mục dư thừa không theo chuẩn.
4. **Step 4:** Cập nhật lại các liên kết (imports), thiết lập lại biến môi trường, và viết `brain.md` mô tả cấu trúc mới.
5. **Step 5:** Cập nhật cấu hình `.viepilot` để AI theo dõi đúng tiến độ.
