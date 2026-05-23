# Hướng Dẫn Bảo Mật Keystore — Solo Leveling System

> **Dành cho AI:** Đọc file này trước khi thực hiện bất kỳ thao tác git, release, hoặc CI/CD nào.
> Đây là quy tắc bất biến — không có ngoại lệ.

---

## Quy Tắc Bảo Mật Tuyệt Đối

### ❌ TUYỆT ĐỐI KHÔNG ĐẨY LÊN GITHUB

| File | Lý do | Hậu quả nếu lộ |
|---|---|---|
| `solo-leveling.jks` | Chữ ký danh tính của app | Kẻ xấu giả mạo app, chiếm quyền update |
| `KEYSTORE-CREDENTIALS.txt` | Chứa mật khẩu plaintext | Mật khẩu bị lộ, keystore bị dùng trái phép |
| `keystore.properties` | Chứa đường dẫn + mật khẩu | Kết hợp với `.jks` để sign app giả mạo |

### Thông Tin Keystore Hiện Tại

```
File     : solo-leveling.jks  (trong thư mục gốc dự án, KHÔNG trong git)
Alias    : solo-leveling
Password : Xem KEYSTORE-CREDENTIALS.txt (KHÔNG trong git)
Tạo ngày : 2026-05-23
Hết hạn  : ~2053 (10000 ngày)
```

---

## Bảo Vệ Đã Được Thiết Lập

### Lớp 1 — .gitignore

File `.gitignore` đã chứa (dòng 28-30):
```
solo-leveling.jks
keystore.properties
KEYSTORE-CREDENTIALS.txt
```

### Lớp 2 — Pre-commit Hook

File `.git/hooks/pre-commit` tự động chặn commit nếu phát hiện keystore files.

**Kiểm tra hook đang hoạt động:**
```bash
cat .git/hooks/pre-commit
# Phải thấy script kiểm tra BLOCKED_FILES
```

**Test hook:**
```bash
# Tạo file test (đừng dùng tên thật)
echo "test" > test.jks
git add test.jks
git commit -m "test"
# Hook phải chặn và hiện thông báo đỏ
rm test.jks
git reset HEAD test.jks 2>/dev/null
```

---

## Quy Trình Backup Bắt Buộc

Sau khi tạo keystore, bạn PHẢI làm ít nhất 1 trong 2:

### Cách 1 — USB / Ổ cứng ngoài (Khuyến nghị)
```
1. Copy solo-leveling.jks → USB
2. Copy KEYSTORE-CREDENTIALS.txt → USB
3. Giữ USB ở nơi an toàn (không cùng chỗ với máy tính)
```

### Cách 2 — Password Manager
```
1. Mở Bitwarden / 1Password / KeePass
2. Tạo entry mới: "Solo Leveling Android Keystore"
3. Username: solo-leveling (alias)
4. Password: [mật khẩu từ KEYSTORE-CREDENTIALS.txt]
5. Attachment: upload file solo-leveling.jks
```

---

## Quy Trình Kiểm Tra Trước Khi Push (Cho AI)

AI PHẢI chạy đoạn này trước mỗi `git push`:

```bash
# Kiểm tra không có keystore nào bị staged
echo "=== Kiểm tra keystore files ==="
STAGED=$(git diff --cached --name-only 2>/dev/null)
UNTRACKED=$(git ls-files --others --exclude-standard 2>/dev/null)

for file in "solo-leveling.jks" "keystore.properties" "KEYSTORE-CREDENTIALS.txt"; do
  if echo "$STAGED" | grep -q "$file"; then
    echo "DANGER: $file đang trong staging area! git reset HEAD $file"
    exit 1
  fi
  if echo "$UNTRACKED" | grep -q "$file"; then
    echo "WARNING: $file đang untracked — đảm bảo .gitignore bao gồm nó"
  fi
done

echo "OK: Không có keystore files nào bị staged"
```

---

## Nếu Lỡ Đẩy Lên GitHub

> Tình huống khẩn cấp: ai đó vô tình push keystore lên public repo.

### Bước xử lý ngay lập tức:

```bash
# Bước 1: Đặt repo thành private ngay
gh repo edit danghoangsqtt-sys/solo-leveling-system --visibility private

# Bước 2: Xóa file khỏi lịch sử git (cần BFG hoặc git-filter-repo)
# Cài BFG:
# java -jar bfg.jar --delete-files solo-leveling.jks .
# git reflog expire --expire=now --all
# git gc --prune=now --aggressive
# git push --force

# Bước 3: Tạo keystore MỚI (vì keystore cũ đã bị compromise)
# Xem docs/tutorials/release-guide.md BƯỚC 1

# Bước 4: Build lại app với keystore mới và phát hành bản mới
```

> **Lưu ý:** Người dùng đã cài app với keystore cũ sẽ cần gỡ ra và cài lại vì keystore mới khác.

---

## Checklist Bảo Mật Hàng Ngày (Cho AI)

Khi được yêu cầu commit/push bất kỳ thứ gì, AI chạy checklist này:

```
[ ] git status không hiện solo-leveling.jks
[ ] git status không hiện keystore.properties
[ ] git status không hiện KEYSTORE-CREDENTIALS.txt
[ ] .gitignore vẫn còn 3 dòng trên (chạy: grep -c "jks\|keystore\|KEYSTORE" .gitignore)
[ ] Pre-commit hook vẫn tồn tại (chạy: ls -la .git/hooks/pre-commit)
```

---

*Tài liệu bảo mật này được tạo bởi vp-brainstorm — 2026-05-23*
*Cập nhật khi: thay đổi keystore, thêm CI/CD, thêm collaborator*
