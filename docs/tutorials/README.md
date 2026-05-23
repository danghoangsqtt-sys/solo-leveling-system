# docs/tutorials — Hướng Dẫn Kỹ Thuật

> Thư mục này chứa các hướng dẫn kỹ thuật dành cho AI đọc và thực thi tự động.
> Mỗi file là một quy trình độc lập, có thể tham chiếu lẫn nhau.

---

## Danh Sách Tài Liệu

| File | Mục đích | Khi nào dùng |
|---|---|---|
| [release-guide.md](release-guide.md) | Tạo bản release đầu tiên | Lần đầu đóng gói APK release, setup keystore |
| [ota-update-guide.md](ota-update-guide.md) | Đẩy bản cập nhật OTA | Khi muốn push update đến người dùng đã cài |
| [ota-release.sh](ota-release.sh) | Script tự động hóa | AI hoặc devops dùng để automate quy trình |

---

## Thông Tin Phiên Bản Hiện Tại

> AI cập nhật section này sau mỗi lần release thành công.

| Thuộc tính | Giá trị |
|---|---|
| `versionCode` | `1` |
| `versionName` | `"1.0"` |
| GitHub Release tag | Chưa có (cần tạo `v1` sau khi setup keystore) |
| Ngày cập nhật | 2026-05-23 |

---

## Hướng Dẫn Nhanh Cho AI

### Lần đầu release:
```
1. Đọc release-guide.md từ đầu đến cuối
2. Thực hiện BƯỚC 1-6 theo thứ tự
3. Cập nhật bảng "Thông Tin Phiên Bản Hiện Tại" trong file này
```

### Release tiếp theo (cập nhật OTA):
```
1. Đọc ota-update-guide.md
2. Chạy: bash docs/tutorials/ota-release.sh <NEW_CODE> "<NEW_NAME>" "<NOTES>"
3. Cập nhật bảng "Thông Tin Phiên Bản Hiện Tại" trong file này
```

### Ví dụ cụ thể cho bản cập nhật đầu tiên:
```bash
bash docs/tutorials/ota-release.sh 2 "1.1" "Fix debounce cloud sync, improve quest generation"
```
