#!/bin/bash
# ota-release.sh — Tự động bump version và tạo GitHub Release
#
# Cách dùng:
#   bash docs/tutorials/ota-release.sh <NEW_VERSION_CODE> <NEW_VERSION_NAME> "<RELEASE_NOTES>"
#
# Ví dụ:
#   bash docs/tutorials/ota-release.sh 2 "1.1" "Fix sync bug, debounce cloud push"
#
# Điều kiện:
#   - signing config đã cấu hình trong app/build.gradle.kts (xem release-guide.md)
#   - keystore.properties tồn tại ở thư mục gốc
#   - gh CLI đã auth: gh auth status
#   - Java và Gradle wrapper khả dụng

set -e  # Dừng ngay khi có lỗi

# ── Arguments ──────────────────────────────────────────────────────────────────
NEW_VERSION_CODE=${1:?"Thiếu argument 1: NEW_VERSION_CODE (ví dụ: 2)"}
NEW_VERSION_NAME=${2:?"Thiếu argument 2: NEW_VERSION_NAME (ví dụ: 1.1)"}
RELEASE_NOTES=${3:-"Cập nhật hệ thống"}

APK_PATH="app/build/outputs/apk/release/app-release.apk"
TAG="v${NEW_VERSION_CODE}"
BUILD_GRADLE="app/build.gradle.kts"

# ── Kiểm tra điều kiện ─────────────────────────────────────────────────────────
echo "==> Kiểm tra môi trường..."

if ! command -v gh &> /dev/null; then
  echo "LỖI: GitHub CLI (gh) chưa cài. Chạy: winget install GitHub.cli"
  exit 1
fi

if ! gh auth status &> /dev/null; then
  echo "LỖI: GitHub CLI chưa đăng nhập. Chạy: gh auth login"
  exit 1
fi

if [ ! -f "keystore.properties" ]; then
  echo "LỖI: Không tìm thấy keystore.properties. Xem docs/tutorials/release-guide.md BƯỚC 2."
  exit 1
fi

# ── Lấy version hiện tại ───────────────────────────────────────────────────────
CURRENT_VERSION_CODE=$(grep "versionCode = " "${BUILD_GRADLE}" | grep -o '[0-9]*')
echo "    versionCode hiện tại: ${CURRENT_VERSION_CODE}"
echo "    versionCode mới:      ${NEW_VERSION_CODE}"

if [ "${NEW_VERSION_CODE}" -le "${CURRENT_VERSION_CODE}" ]; then
  echo "LỖI: NEW_VERSION_CODE (${NEW_VERSION_CODE}) phải lớn hơn version hiện tại (${CURRENT_VERSION_CODE})"
  exit 1
fi

# ── Bước 1: Cập nhật version ───────────────────────────────────────────────────
echo ""
echo "==> Bước 1: Cập nhật version..."

# Dùng sed để thay versionCode
sed -i "s/versionCode = [0-9]*/versionCode = ${NEW_VERSION_CODE}/" "${BUILD_GRADLE}"

# Thay versionName
sed -i "s/versionName = \"[^\"]*\"/versionName = \"${NEW_VERSION_NAME}\"/" "${BUILD_GRADLE}"

# Xác nhận
echo "    $(grep 'versionCode\|versionName' ${BUILD_GRADLE} | tr -s ' ')"

# ── Bước 2: Build APK ──────────────────────────────────────────────────────────
echo ""
echo "==> Bước 2: Build APK release (có thể mất 2-5 phút)..."

./gradlew clean :app:assembleRelease --quiet

if [ ! -f "${APK_PATH}" ]; then
  echo "LỖI: Build thất bại — không tìm thấy ${APK_PATH}"
  exit 1
fi

APK_SIZE=$(du -h "${APK_PATH}" | cut -f1)
echo "    APK: ${APK_PATH} (${APK_SIZE})"

# ── Bước 3: Commit ─────────────────────────────────────────────────────────────
echo ""
echo "==> Bước 3: Commit version bump..."

git add "${BUILD_GRADLE}"
git commit -m "chore(release): bump version to ${NEW_VERSION_NAME} (versionCode=${NEW_VERSION_CODE})"
git push origin master

echo "    Commit đã push lên remote."

# ── Bước 4: Tạo GitHub Release ─────────────────────────────────────────────────
echo ""
echo "==> Bước 4: Tạo GitHub Release ${TAG}..."

gh release create "${TAG}" \
  --title "System Leveling v${NEW_VERSION_NAME}" \
  --notes "## Phiên bản ${NEW_VERSION_NAME}

${RELEASE_NOTES}

---
*Cập nhật tự động từ ota-release.sh — $(date '+%Y-%m-%d')*" \
  --latest \
  "${APK_PATH}#app-release.apk"

# ── Bước 5: Xác nhận ───────────────────────────────────────────────────────────
echo ""
echo "==> Bước 5: Xác nhận release..."
gh release view "${TAG}" --json tagName,name,assets --jq '"Tag: " + .tagName + " | Title: " + .name + " | Assets: " + (.assets | map(.name) | join(", "))'

# ── Hoàn thành ─────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " HOÀN THÀNH!"
echo " Release:  ${TAG} — System Leveling v${NEW_VERSION_NAME}"
echo " OTA:      Thiết bị chạy versionCode < ${NEW_VERSION_CODE} sẽ thấy dialog cập nhật"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
