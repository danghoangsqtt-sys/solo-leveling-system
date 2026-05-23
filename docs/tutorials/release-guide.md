# Hướng Dẫn Tạo Bản Release — Solo Leveling System

> **Dành cho AI:** Tài liệu này mô tả toàn bộ quy trình đóng gói APK release và phát hành lên GitHub Releases.
> Đọc toàn bộ trước khi thực thi. Mỗi bước có điều kiện tiên quyết cụ thể.

---

## Thông Tin Dự Án

| Thuộc tính | Giá trị |
|---|---|
| `applicationId` | `com.systemleveling.app` |
| `versionCode` hiện tại | `1` |
| `versionName` hiện tại | `"1.0"` |
| `minSdk` | `26` (Android 8.0) |
| `targetSdk` | `34` |
| Repo GitHub | `danghoangsqtt-sys/solo-leveling-system` |
| File build | `app/build.gradle.kts` |
| OTA tag format | `v{versionCode}` (ví dụ: `v1`, `v2`) |

---

## Điều Kiện Tiên Quyết

Trước khi bắt đầu, kiểm tra các công cụ sau đã cài đặt:

```bash
# Kiểm tra Java (cần cho keytool và Gradle)
java -version

# Kiểm tra Android SDK / Gradle wrapper
./gradlew --version

# Kiểm tra GitHub CLI (cần để tạo release)
gh --version
gh auth status
```

---

## BƯỚC 1 — Tạo Keystore (Chỉ Làm 1 Lần)

> **Kiểm tra trước:** Nếu file `solo-leveling.jks` đã tồn tại trong thư mục gốc dự án → **bỏ qua bước này**.

```bash
# Tạo keystore mới
keytool -genkey -v \
  -keystore solo-leveling.jks \
  -alias solo-leveling \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "YOUR_STORE_PASSWORD" \
  -keypass "YOUR_KEY_PASSWORD" \
  -dname "CN=Solo Leveling, OU=App, O=System Leveling, L=Hanoi, ST=HN, C=VN"
```

> **QUAN TRỌNG:** Lưu `solo-leveling.jks`, `storePassword`, `keyPassword` vào nơi an toàn.
> Mất keystore = không thể cập nhật app trên thiết bị người dùng đã cài.

Sau khi tạo keystore, thêm vào `.gitignore`:

```bash
echo "solo-leveling.jks" >> .gitignore
echo "keystore.properties" >> .gitignore
```

---

## BƯỚC 2 — Cấu Hình Signing Config

### 2a. Tạo file `keystore.properties`

Tạo file `keystore.properties` ở thư mục gốc dự án (KHÔNG commit file này):

```properties
storeFile=../solo-leveling.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=solo-leveling
keyPassword=YOUR_KEY_PASSWORD
```

### 2b. Cập nhật `app/build.gradle.kts`

Thêm signing config vào `app/build.gradle.kts`. **Kiểm tra xem đã có `signingConfigs` block chưa — nếu có thì bỏ qua.**

```kotlin
// Thêm vào đầu file, trước android { }
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    // ... (giữ nguyên cấu hình hiện có)

    // Thêm signingConfigs block:
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true    // Bật ProGuard/R8
            isShrinkResources = true  // Xóa resources không dùng
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## BƯỚC 3 — Đặt Số Phiên Bản

Cập nhật `versionCode` và `versionName` trong `app/build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.systemleveling.app"
    minSdk = 26
    targetSdk = 34
    versionCode = 1       // ← Số nguyên tăng dần. Đây là số OTA dùng để so sánh
    versionName = "1.0"   // ← Chuỗi hiển thị cho người dùng
    // ...
}
```

**Quy tắc versionCode:**
- `versionCode` là số nguyên, **chỉ tăng, không bao giờ giảm**
- GitHub Release tag = `v{versionCode}` (ví dụ: `versionCode=1` → tag `v1`)
- OTA update manager so sánh `versionCode` của release với `BuildConfig.VERSION_CODE` của app

---

## BƯỚC 4 — Build APK Release

```bash
# Clean build trước để tránh artifacts cũ
./gradlew clean

# Build APK release (có signing)
./gradlew :app:assembleRelease

# File APK output:
# app/build/outputs/apk/release/app-release.apk
```

**Kiểm tra build thành công:**

```bash
# Xác nhận file tồn tại và có kích thước hợp lý (thường > 5MB)
ls -lh app/build/outputs/apk/release/app-release.apk

# Kiểm tra signing (optional)
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

---

## BƯỚC 5 — Commit Version Bump

```bash
# Stage chỉ build file (không commit keystore)
git add app/build.gradle.kts

# Commit với message chuẩn
git commit -m "chore(release): bump version to 1.0 (versionCode=1)"

# Push lên remote
git push origin master
```

---

## BƯỚC 6 — Tạo GitHub Release

```bash
# Định nghĩa biến (thay đổi theo version đang release)
VERSION_CODE=1
VERSION_NAME="1.0"
APK_PATH="app/build/outputs/apk/release/app-release.apk"
TAG="v${VERSION_CODE}"

# Tạo release với GitHub CLI
gh release create "${TAG}" \
  --title "System Leveling v${VERSION_NAME}" \
  --notes "## Phiên bản ${VERSION_NAME}

### Tính năng
- Hệ thống leveling RPG hoàn chỉnh
- AI Quest Generation (Gemini)
- Cloud sync (Supabase)
- OTA update tự động

### Yêu cầu
- Android 8.0+ (API 26)
- Cần nhập Gemini API Key khi onboarding" \
  --latest \
  "${APK_PATH}#app-release.apk"
```

**Kiểm tra release đã tạo:**

```bash
gh release view "${TAG}"
```

---

## Checklist Hoàn Thành

```
[ ] keytool đã chạy thành công, file solo-leveling.jks tồn tại
[ ] keystore.properties tạo xong, không trong git
[ ] app/build.gradle.kts có signingConfigs block đúng
[ ] versionCode và versionName đã set đúng
[ ] ./gradlew :app:assembleRelease thành công (exit code 0)
[ ] app-release.apk tồn tại tại đường dẫn output
[ ] Git commit đã push lên remote
[ ] GitHub Release đã tạo với tag v{versionCode}
[ ] APK đính kèm trong release assets
```

---

## Xử Lý Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| `keystore not found` | Đường dẫn `storeFile` sai | Kiểm tra `keystore.properties` — dùng đường dẫn tương đối từ app/ |
| `signing failed` | Password sai | Kiểm tra lại `storePassword` và `keyPassword` trong `keystore.properties` |
| `BUILD FAILED: minification` | ProGuard lỗi với classes | Thêm `-keep` rules vào `proguard-rules.pro` |
| `gh: command not found` | GitHub CLI chưa cài | `winget install GitHub.cli` (Windows) |
| `not logged in` | GitHub CLI chưa auth | `gh auth login` |

---

*Tài liệu này được tạo bởi vp-brainstorm — 2026-05-23*
*Phiên bản tài liệu: 1.0 | Áp dụng cho Solo Leveling System v1.0+*
