# Báo cáo kiểm thử — QuickLock 1.0.0

## 1. Môi trường

| Hạng mục | Giá trị |
| --- | --- |
| Máy build | GitHub Actions, `ubuntu-latest` (24.04) |
| JDK | Temurin 21.0.13 |
| Gradle | 8.11.1 (wrapper) |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| SDK components | `platforms;android-35`, `build-tools;35.0.0` |
| Thiết bị thực tế | **không có** (không có Redmi K50 Ultra trong quá trình phát triển) |
| Emulator | **không sử dụng** (máy build không có KVM) |

Vì không có thiết bị/emulator, phần kiểm thử chia làm hai nhóm:

* **Đã kiểm chứng tự động** — build, manifest, quyền, component, chữ ký, lint (mục 2–4).
* **Chưa kiểm chứng** — mọi hành vi runtime trên HyperOS 3 (mục 5, 6). Danh sách kiểm thử
  tay kèm lệnh ADB nằm ở mục 5 để chạy trên máy thật.

---

## 2. Build

| Hạng mục | Kết quả |
| --- | --- |
| CI run | [run 43200776057](https://github.com/thangvt-food/quick-lock/actions/runs/43200776057) — **success** |
| Commit | `11e8a84a5971ef1d66bcd2055218b55df200b418` |
| Task | `:app:assembleDebug` — BUILD SUCCESSFUL, không có warning biên dịch Kotlin |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Kích thước | 8.340.869 bytes (8,0 MB), 121 entry |
| SHA-256 APK | `920cbbc6b2d1b08a1c0d986d092b37d754c74a5f3e58b2b25877d2a0b716b3ea` |
| Chữ ký | APK Signature Scheme v2, `CN=Android Debug, O=Android, C=US` |
| Cert SHA-256 | `0EF068FFFE16220E71084E897F5AAEC958EB0B018239D9ED49257DCECA27D74B` |

APK được ký bằng key debug công khai nằm trong repo (`keystore/quicklock-debug.jks`).
Nhờ vậy mọi bản build CI có cùng chữ ký → cài đè/cập nhật được, không phải gỡ app.

---

## 3. Audit APK tự động (`scripts/verify-apk.sh`)

Script chạy trong CI sau khi build và **fail build** nếu bất kỳ mục nào sai.

| Kiểm tra | Kết quả | Bằng chứng |
| --- | --- | --- |
| Package name | PASS | `package: name='com.quicklock.app' versionName='1.0.0'` |
| minSdk / targetSdk | PASS | `minSdkVersion:'26'`, `targetSdkVersion:'35'` |
| Quyền hệ thống | PASS | Không có `INTERNET`, `READ_*`, `QUERY_ALL_PACKAGES`… |
| Quyền tự định nghĩa duy nhất | PASS | `com.quicklock.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, `protectionLevel=0x00000002` (signature) |
| `MainActivity` | PASS | có trong manifest |
| `LockActivity` | PASS | có trong manifest |
| `LockShortcutAlias` = launcher entry | PASS | `activity-alias` + `category.LAUNCHER` |
| `LockAccessibilityService` | PASS | có `BIND_ACCESSIBILITY_SERVICE` + action `android.accessibilityservice.AccessibilityService` |
| Accessibility config không event | PASS | không có attribute `accessibilityEventTypes` |
| Không đọc nội dung cửa sổ | PASS | `canRetrieveWindowContent = false` |
| Không gesture | PASS | `canPerformGestures = false` |
| Strip `ProfileInstallReceiver` | PASS | không còn receiver AndroidX nào |
| Chữ ký | PASS | `apksigner verify --print-certs` OK |

Badging đầy đủ của APK:

```
package: name='com.quicklock.app' versionCode='1' versionName='1.0.0' compileSdkVersion='35'
minSdkVersion:'26'
targetSdkVersion:'35'
uses-permission: name='com.quicklock.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
application-label:'QuickLock'
application: label='QuickLock' icon='res/mipmap-anydpi/ic_launcher_settings.xml'
launchable-activity: name='com.quicklock.app.MainActivity' label='QuickLock Settings'
provides-component:'accessibility'
locales: '--_--' 'vi'
```

---

## 4. Android Lint

`./gradlew lintDebug` là bước **blocking** trong CI. Kết quả: **0 error, 5 warning**
(báo cáo: artifact `lint-report` của CI run ở mục 2).

| Warning | Đánh giá |
| --- | --- |
| `OldTargetApi` (targetSdk 35 < mới nhất) | Chấp nhận có chủ đích — yêu cầu dự án là targetSdk 35. |
| `GradleDependency` (có phiên bản thư viện mới hơn) | Chấp nhận — version được pin để build tất định. |
| `DataExtractionRules` | Không áp dụng: `allowBackup=false`, app không lưu dữ liệu. |
| `ReportShortcutUsage` | Khuyến nghị tối ưu ranking shortcut; app chỉ có 1 shortcut nên không ảnh hưởng. |

---

## 5. Kiểm thử trên thiết bị — **CHƯA THỰC HIỆN**

Không có Redmi K50 Ultra / HyperOS 3.0.6.0 và không có emulator trong môi trường phát triển.
Các mục dưới đây là checklist nghiệm thu, đánh dấu *chưa xác nhận*:

| # | Tiêu chí | Trạng thái |
| --- | --- | --- |
| 1 | Cài đặt được trên Android 15 | chưa xác nhận (APK hợp lệ, đúng targetSdk 35) |
| 2 | Không yêu cầu root / Device Admin | xác nhận qua code + manifest (không có `BIND_DEVICE_ADMIN`, không dùng `su`) |
| 3 | Không cần Internet | xác nhận qua audit (không có `INTERNET`) |
| 4 | Accessibility Service bật/tắt được | chưa xác nhận |
| 5 | Tạo được shortcut QuickLock | chưa xác nhận (alias đã có trong manifest) |
| 6 | Kéo shortcut vào Dock | chưa xác nhận trên HyperOS |
| 7 | Chạm icon một lần là khóa | chưa xác nhận |
| 8 | Không mở giao diện thừa khi khóa | chưa xác nhận (theme trong suốt, không `setContentView`) |
| 9 | Không có tracking/telemetry | xác nhận qua audit (không SDK mạng) |
| 10 | Quyền bị thu hồi → app báo lỗi rõ | chưa xác nhận (đã xử lý trong code, xem mục 6) |
| 11 | Cập nhật app giữ quyền + shortcut | chưa xác nhận |

### Lệnh kiểm thử tay (khi có máy thật)

```bash
# 1. Cài đặt
adb install -r app-debug.apk

# 2. Mở màn hình trạng thái
adb shell am start -n com.quicklock.app/.MainActivity

# 3. Kiểm tra service đã được bật chưa
adb shell dumpsys accessibility | grep -i -A3 quicklock

# 4. Gọi thẳng activity khóa (không cần chạm icon) — màn hình phải tắt
adb shell am start -n com.quicklock.app/.LockActivity

# 5. Kiểm tra không có quyền nào ngoài dự kiến
adb shell dumpsys package com.quicklock.app | grep -A20 "requested permissions"

# 6. Thu hồi quyền rồi chạm icon khóa → phải hiện Toast và mở màn hình setup
#    (tắt công tắc QuickLock trong Settings → Accessibility)
```

---

## 6. Phân tích các luồng đặc biệt (kiểm tra tĩnh)

| Luồng | Hành vi trong code |
| --- | --- |
| Chạm icon khi quyền đã bật | `LockActivity.onCreate` → `LockAccessibilityService.lockScreen()` → `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)` → `finishAndRemoveTask()`. Không gọi `setContentView`. |
| Chạm icon khi quyền bị thu hồi | `instance == null` → `lockScreen()` trả `false` → Toast + mở `MainActivity` (không thất bại im lặng). `app/src/main/java/com/quicklock/app/LockActivity.kt:26-33` |
| Process bị hệ thống kill | Service được hệ thống rebind khi bật accessibility; nếu chưa kịp rebind, hành vi rơi vào nhánh "quyền bị thu hồi". |
| Android 8.x (API 26–27) | `GLOBAL_ACTION_LOCK_SCREEN` không tồn tại → `lockScreen()` trả `false` → Toast hướng dẫn. |
| Thu hồi quyền khi service đang chạy | `onUnbind`/`onDestroy` đặt `instance = null`. |
| Cập nhật app (cài đè cùng chữ ký) | Quyền trợ năng và shortcut của launcher được giữ nguyên vì cùng package + chữ ký. |
| Gỡ rồi cài lại | Mất quyền trợ năng (Android reset), shortcut trên Home/Dock có thể trỏ tới package đã bị gỡ → phải tạo lại. |
| Đổi launcher / reset launcher | Icon `activity-alias` vẫn tồn tại trong ngăn ứng dụng; shortcut đã ghim có thể mất và phải ghim lại. |
| Ứng dụng khác gọi `LockActivity` | Activity exported (bắt buộc để launcher/shortcut khởi chạy). Hệ quả: một app bất kỳ cũng có thể yêu cầu khóa màn hình — thao tác này không lộ dữ liệu và không cấp quyền gì thêm. |

---

## 7. Những điểm **chưa** thể xác nhận

1. Chạm icon trên HyperOS 3.0.6.0 có khóa ngay hay xuất hiện một frame animation của launcher.
2. Icon `activity-alias` có kéo được vào Dock trên HyperOS 3 (về mặt Android chuẩn thì có).
3. Hành vi của HyperOS với accessibility service sau reboot / dưới chế độ tiết kiệm pin.
4. Hộp thoại "Restricted settings" của Android 13+ trên ROM này (đã có hướng dẫn xử lý).
5. `requestPinShortcut` có được HyperOS hỗ trợ đầy đủ hay trả `false`.

Các điểm trên chỉ kiểm chứng được trên thiết bị thật; phần code liên quan đều đã có
nhánh xử lý khi thất bại (Toast + mở màn hình setup / mở danh sách ứng dụng).
