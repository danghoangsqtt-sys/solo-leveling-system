# Hướng Dẫn Phát Hành Bản Cập Nhật OTA — Solo Leveling System

> **Dành cho AI:** Tài liệu này mô tả quy trình đẩy bản cập nhật cho người dùng đã cài app.
> OTA hoạt động qua GitHub Releases. App tự động phát hiện bản mới khi mở HomeScreen.

---

## Cơ Chế OTA Hoạt Động Như Thế Nào

```
[App khởi động HomeScreen]
        ↓
[OtaUpdateManager.checkForUpdate(currentVersionCode)]
        ↓
[GET https://api.github.com/repos/danghoangsqtt-sys/solo-leveling-system/releases/latest]
        ↓
[Parse tag_name → "v2" → versionCode = 2]
        ↓
[So sánh: 2 > currentVersionCode(1) → Có bản mới]
        ↓
[Hiện OtaUpdateDialog với release notes]
        ↓
[User nhấn "Cập nhật ngay"]
        ↓
[Download app-release.apk → FileProvider URI → Install Intent]
        ↓
[Android hỏi "Install unknown apps" (lần đầu) → Cài đặt]
```

**File liên quan:**
- `core/src/main/java/com/systemleveling/core/ota/OtaUpdateManager.kt` — logic check + download
- `feature/home/src/main/java/com/systemleveling/feature/home/ui/OtaUpdateDialog.kt` — UI dialog
- `feature/home/src/main/java/com/systemleveling/feature/home/ui/HomeViewModel.kt` — trigger check

---

## Trạng Thái Phiên Bản Hiện Tại

| Thuộc tính | Giá trị |
|---|---|
| `versionCode` | `1` ← **Bắt đầu từ đây** |
| `versionName` | `"1.0"` |
| GitHub Release tag hiện tại | `v1` (sau khi release đầu tiên) |
| File cần sửa | `app/build.gradle.kts` → `defaultConfig` block |

---

## Quy Tắc Đánh Số Phiên Bản

| versionCode | versionName | GitHub Tag | Ghi chú |
|---|---|---|---|
| `1` | `"1.0"` | `v1` | Initial release |
| `2` | `"1.1"` | `v2` | Bản vá lỗi / tính năng nhỏ |
| `3` | `"1.2"` | `v3` | |
| `10` | `"2.0"` | `v10` | Major update |

**Nguyên tắc:**
- `versionCode` **chỉ tăng**, không bao giờ giảm
- `versionName` là chuỗi tự do, chỉ để hiển thị cho người dùng
- OTA so sánh dựa vào `versionCode`, không phải `versionName`
- Tag GitHub = `v` + `versionCode` (ví dụ: `versionCode=5` → tag `v5`)

---

## Quy Trình Đẩy Bản Cập Nhật

### BƯỚC 1 — Xác Định versionCode Tiếp Theo

```bash
# Xem versionCode hiện tại trong build.gradle.kts
grep "versionCode\|versionName" app/build.gradle.kts
```

**Output mẫu:**
```
versionCode = 1
versionName = "1.0"
```

→ versionCode tiếp theo = **2**, versionName = **"1.1"** (hoặc tùy nội dung thay đổi)

---

### BƯỚC 2 — Cập Nhật Version Trong Build File

Sửa `app/build.gradle.kts`, trong block `defaultConfig`:

```kotlin
defaultConfig {
    applicationId = "com.systemleveling.app"
    minSdk = 26
    targetSdk = 34
    versionCode = 2       // ← TĂNG LÊN 1 (ví dụ: 1 → 2)
    versionName = "1.1"   // ← Cập nhật tên version
    // ... giữ nguyên phần còn lại
}
```

**Lệnh kiểm tra sau khi sửa:**
```bash
grep "versionCode\|versionName" app/build.gradle.kts
# Expected: versionCode = 2 / versionName = "1.1"
```

---

### BƯỚC 3 — Build APK Release

> **Điều kiện:** Keystore và signing config đã cấu hình (xem `docs/tutorials/release-guide.md` BƯỚC 1-2).

```bash
# Clean và build
./gradlew clean :app:assembleRelease

# Xác nhận file output
ls -lh app/build/outputs/apk/release/app-release.apk
```

---

### BƯỚC 4 — Commit Version Bump

```bash
# Stage build file
git add app/build.gradle.kts

# Commit (thay thế 1.1 và versionCode=2 theo version thực tế)
git commit -m "chore(release): bump version to 1.1 (versionCode=2)"

# Push
git push origin master
```

---

### BƯỚC 5 — Tạo GitHub Release

> **QUAN TRỌNG:** Tag phải đúng định dạng `v{versionCode}`.
> OtaUpdateManager parse tag này để so sánh version.
> APK trong release **bắt buộc** đặt tên `app-release.apk`.

```bash
# Định nghĩa biến (thay đổi theo version đang release)
VERSION_CODE=2
VERSION_NAME="1.1"
APK_PATH="app/build/outputs/apk/release/app-release.apk"
TAG="v${VERSION_CODE}"

# Viết release notes (mô tả thay đổi trong bản này)
RELEASE_NOTES="## Phiên bản ${VERSION_NAME}

### Thay Đổi
- [Mô tả thay đổi 1]
- [Mô tả thay đổi 2]

### Sửa Lỗi
- [Mô tả bug đã fix]

### Yêu Cầu
- Android 8.0+ (API 26)"

# Tạo GitHub Release
gh release create "${TAG}" \
  --title "System Leveling v${VERSION_NAME}" \
  --notes "${RELEASE_NOTES}" \
  --latest \
  "${APK_PATH}#app-release.apk"
```

---

### BƯỚC 6 — Xác Nhận OTA Hoạt Động

```bash
# Kiểm tra release đã tạo đúng
gh release view "v${VERSION_CODE}"

# Kiểm tra tag_name đúng định dạng
gh release view "v${VERSION_CODE}" --json tagName,assets --jq '.tagName, .assets[].name'
# Expected output:
# v2
# app-release.apk

# Test GitHub API endpoint (giống OtaUpdateManager gọi)
curl -s "https://api.github.com/repos/danghoangsqtt-sys/solo-leveling-system/releases/latest" \
  | python -m json.tool | grep -E '"tag_name"|"browser_download_url"'
```

---

## Checklist Đẩy Bản Cập Nhật

```
[ ] versionCode đã tăng trong app/build.gradle.kts (so với version trước)
[ ] versionName đã cập nhật (ví dụ: "1.0" → "1.1")
[ ] ./gradlew :app:assembleRelease thành công (exit code 0)
[ ] app-release.apk tồn tại tại đường dẫn output
[ ] Git commit đã push
[ ] GitHub Release tạo với tag v{versionCode} (ví dụ: v2)
[ ] APK đính kèm tên CHÍNH XÁC là "app-release.apk"
[ ] curl test API trả về tag_name và download URL đúng
```

---

## Script Tự Động Hoàn Chỉnh

AI có thể dùng script dưới đây để thực hiện toàn bộ quy trình (thay thế các biến trước khi chạy):

```bash
#!/bin/bash
# ota-release.sh — Tự động bump version và tạo GitHub Release
# 
# Cách dùng: bash docs/tutorials/ota-release.sh <NEW_VERSION_CODE> <NEW_VERSION_NAME> "<RELEASE_NOTES>"
# Ví dụ:     bash docs/tutorials/ota-release.sh 2 "1.1" "Fix sync bug, add OTA updates"
#
# Điều kiện: signing config đã cấu hình, gh đã auth

set -e  # Dừng nếu có lỗi

NEW_VERSION_CODE=${1:?Thiếu versionCode}
NEW_VERSION_NAME=${2:?Thiếu versionName}
RELEASE_NOTES=${3:-"Cập nhật hệ thống"}

APK_PATH="app/build/outputs/apk/release/app-release.apk"
TAG="v${NEW_VERSION_CODE}"

echo "==> Bước 1: Cập nhật versionCode = ${NEW_VERSION_CODE}, versionName = ${NEW_VERSION_NAME}"
# Thay versionCode
sed -i "s/versionCode = [0-9]*/versionCode = ${NEW_VERSION_CODE}/" app/build.gradle.kts
# Thay versionName
sed -i "s/versionName = \"[^\"]*\"/versionName = \"${NEW_VERSION_NAME}\"/" app/build.gradle.kts

echo "==> Bước 2: Build APK release"
./gradlew clean :app:assembleRelease

echo "==> Bước 3: Kiểm tra APK"
if [ ! -f "${APK_PATH}" ]; then
  echo "LỖI: Không tìm thấy ${APK_PATH}"
  exit 1
fi

echo "==> Bước 4: Commit version bump"
git add app/build.gradle.kts
git commit -m "chore(release): bump version to ${NEW_VERSION_NAME} (versionCode=${NEW_VERSION_CODE})"
git push origin master

echo "==> Bước 5: Tạo GitHub Release ${TAG}"
gh release create "${TAG}" \
  --title "System Leveling v${NEW_VERSION_NAME}" \
  --notes "## Phiên bản ${NEW_VERSION_NAME}

${RELEASE_NOTES}" \
  --latest \
  "${APK_PATH}#app-release.apk"

echo ""
echo "==> HOÀN THÀNH!"
echo "    Release: ${TAG}"
echo "    OTA sẽ hiện dialog trên thiết bị đang chạy versionCode < ${NEW_VERSION_CODE}"
```

**Cách AI gọi script:**
```bash
# Cấp quyền thực thi (lần đầu)
chmod +x docs/tutorials/ota-release.sh

# Chạy với parameters
bash docs/tutorials/ota-release.sh 2 "1.1" "Fix sync bug, debounce cloud push"
```

---

## Xử Lý Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| OTA dialog không hiện | `tag_name` sai format | Tag phải là `v{số nguyên}`, ví dụ `v2` không phải `v1.1` |
| OTA dialog hiện nhưng không download | `app-release.apk` không có trong assets | Upload lại APK với đúng tên file |
| API rate limit | Nhiều check trong 1 giờ | GitHub cho phép 60 req/giờ không auth — thường không bị |
| `sed` không thay được trên Windows | `sed` Windows khác Linux | Dùng PowerShell: `(Get-Content file) -replace 'old','new' \| Set-Content file` |
| Build failed sau khi tăng versionCode | Code mới có lỗi | Chạy `./gradlew :app:compileDebugKotlin` để xem lỗi trước |

---

## Lịch Sử Phiên Bản

| versionCode | versionName | Ngày | Thay đổi |
|---|---|---|---|
| `1` | `"1.0"` | 2026-05-23 | Initial release — OTA support, Gemini AI, Supabase sync |
| `2` | `"1.1"` | 2026-05-24 | RPG Splash Screen, Motivational Quotes, Aura NPC greeting, Library drag-to-folder, lesson editing |
| `10401` | `"1.4.1"` | 2026-05-24 | Fix black screen (QuestListScreen), fix Calendar sort, fix OTA semantic version parsing, perf improvements |
| `10402` | `"1.4.2"` | 2026-05-24 | Tap notification → open quest list; onNewIntent deep-link; back-stack fix |

> AI cập nhật bảng này sau mỗi lần release thành công.

---

*Tài liệu này được tạo bởi vp-brainstorm — 2026-05-23*
*Phiên bản tài liệu: 1.0 | Áp dụng cho Solo Leveling System v1.0+*
