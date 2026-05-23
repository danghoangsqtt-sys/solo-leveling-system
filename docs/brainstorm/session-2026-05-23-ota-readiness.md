# Brainstorm Session — 2026-05-23 OTA & Production Readiness

## Chủ đề: Kiểm Tra Sẵn Sàng Đóng Gói + OTA Update

> **Trạng thái**: ✅ Scope Locked → Triển khai
> **Ngày**: 2026-05-23
> **Người yêu cầu**: @danghoangsqtt-sys

---

## Kết Quả Đánh Giá

### Sử dụng được chưa? ✅ CÓ (với 2 lưu ý nhỏ)

| Hạng mục | Trạng thái |
|---|---|
| Core features (Quests, Home, Inventory, Calendar) | ✅ |
| Database Room v8 + migrations | ✅ |
| AI Gemini integration | ✅ |
| Cloud sync Supabase | ✅ |
| Permissions manifest | ✅ |
| ProGuard/R8 | ⚠️ tắt — nên bật trước khi release |
| Signing keystore | ❌ chưa có — cần tạo trước release |

### Đóng gói được chưa?

- **APK debug**: Build được ngay
- **APK release**: Cần tạo keystore + signing config

---

## OTA Update — Quyết Định Thiết Kế

### Approach: Self-hosted trên GitHub Releases

- **Host**: GitHub Releases (`danghoangsqtt-sys/solo-leveling-system`)
- **Check endpoint**: `https://api.github.com/repos/danghoangsqtt-sys/solo-leveling-system/releases/latest`
- **Tag convention**: `v{versionCode}` (e.g., `v1`, `v2`, `v3`)
- **Flow**: Check on HomeScreen load → parse tag → compare versionCode → dialog → download → install

### Tag Convention

```
v1  → versionCode = 1 (initial release)
v2  → versionCode = 2 (first update)
v3  → versionCode = 3 (etc.)
```

APK artifact trong release phải đặt tên `app-release.apk`.

### Architecture

```
OtaUpdateManager (core)
  └── checkForUpdate(currentVersionCode: Int): OtaUpdateInfo?
  └── downloadAndInstall(downloadUrl: String): Result<Unit>

AppBuildInfo (core)
  └── data class AppBuildInfo(versionCode: Int, versionName: String)

AppModule (app/di)
  └── provides AppBuildInfo from BuildConfig

HomeViewModel (feature/home)
  └── injects OtaUpdateManager + AppBuildInfo
  └── otaUpdateInfo: StateFlow<OtaUpdateInfo?>
  └── init {} → checkForUpdate()

OtaUpdateDialog (feature/home)
  └── RPG-styled dialog, release notes, "Cập nhật" + "Để sau"
```

---

## Phases

### Phase 1 — OTA Core (triển khai ngay)
- [x] **P1.1** AppBuildInfo data class + DI
- [x] **P1.2** OtaUpdateManager (GitHub API check + download + install)
- [x] **P1.3** FileProvider + REQUEST_INSTALL_PACKAGES permission
- [x] **P1.4** HomeViewModel OTA state + check on init
- [x] **P1.5** OtaUpdateDialog composable (RPG-styled)
- [x] **P1.6** HomeScreen integrate dialog

### Phase 2 — Polish
- [ ] **P2.1** Download progress bar trong dialog
- [ ] **P2.2** Retry on download failure
- [ ] **P2.3** Check OTA chỉ 1 lần mỗi ngày (cache last check time)

### Phase 3 — Production Setup
- [ ] **P3.1** Tạo signing keystore + signing config trong build.gradle.kts
- [ ] **P3.2** Bật ProGuard/R8 (`isMinifyEnabled = true`)
- [ ] **P3.3** Build APK release + upload GitHub Release đầu tiên

---

## Hướng Dẫn Tạo Release

```bash
# 1. Tăng versionCode trong app/build.gradle.kts
#    versionCode = 2 (tăng 1 mỗi lần release)
#    versionName = "1.1"

# 2. Build APK release
./gradlew :app:assembleRelease

# 3. Tạo GitHub Release với tag v2
#    Upload file: app/build/outputs/apk/release/app-release.apk
#    Tag name: v2
#    Release title: Version 1.1 — <mô tả>
#    Body: mô tả thay đổi (hiển thị trong OTA dialog)
```

---

*Session completed — 2026-05-23 · vp-brainstorm v1.1.0*
