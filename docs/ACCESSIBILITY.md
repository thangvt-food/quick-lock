# Cấp quyền Accessibility cho QuickLock

QuickLock dùng **AccessibilityService** đúng một mục đích: gọi
`performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)` để tắt + khóa màn hình.
Đây là API công khai, không root, không Device Administrator.

Android **không cho phép** app tự bật quyền này. Người dùng phải bật thủ công một lần,
và có thể tắt bất cứ lúc nào.

---

## 1. Đường dẫn trên HyperOS 3 (Redmi K50 Ultra, Android 15)

**Settings → Additional settings → Accessibility → Downloaded apps → QuickLock → bật công tắc**

Một số bản HyperOS đặt mục này ở:

* **Settings → Additional settings → Accessibility → Installed apps**
* hoặc **Settings → Apps → Accessibility** (tuỳ bản ROM)

Bấm nút **Enable accessibility** trong QuickLock sẽ mở thẳng trang Accessibility
(tiện hơn tìm tay).

Khi bật, HyperOS hiện hộp thoại cảnh báo "ứng dụng sẽ có thể xem và điều khiển
màn hình" kèm nút **Allow/OK** → xác nhận.

---

## 2. Lỗi "Restricted setting" / công tắc bị mờ

Từ **Android 13**, hệ thống chặn bật Accessibility cho ứng dụng **cài ngoài Play Store**
(APK sideload) cho tới khi người dùng tự cho phép:

1. **Settings → Apps → Manage apps → QuickLock** (hoặc nhấn giữ icon app → *App info*).
2. Bấm **⋮** ở góc trên phải.
3. Chọn **Allow restricted settings** → xác nhận.
4. Quay lại Accessibility → bật QuickLock.

Đây là cơ chế bảo mật của Android, **không phải** lỗi của app. App không có cách
và không cố vượt qua cơ chế này.

---

## 3. Kiểm tra đã bật hay chưa

* Mở **QuickLock Settings**: dòng *Accessibility service* phải hiện **Enabled** màu xanh.
* Hoặc mở Accessibility settings: mục QuickLock phải ở trạng thái **On**.

Ứng dụng kiểm tra trạng thái bằng `AccessibilityManager.getEnabledAccessibilityServiceList()`
(API chuẩn), không đọc `Settings.Secure` — nên kết quả phản ánh đúng service đang được
hệ thống bind.

---

## 4. Thu hồi quyền

Tắt công tắc QuickLock trong Accessibility settings. Ứng dụng mất khả năng khóa;
khi chạm icon ổ khóa, app hiện Toast "QuickLock needs its accessibility service"
và mở màn hình setup thay vì thất bại im lặng.

Không cần xoá dữ liệu gì — QuickLock không lưu dữ liệu nào.

---

## 5. Xử lý sự cố

| Hiện tượng | Nguyên nhân / cách xử lý |
| --- | --- |
| Chạm icon ổ khóa mà không khóa | Quyền trợ năng chưa bật, hoặc service bị hệ thống kill. Kiểm tra trạng thái trong QuickLock Settings, tắt/bật lại công tắc. |
| Service tự tắt sau reboot | Một số ROM bật lại accessibility service chậm. Mở QuickLock Settings để xem trạng thái; bật **Autostart** cho QuickLock trong HyperOS. |
| Công tắc bị mờ | Xem mục 2 (*Allow restricted settings*). |
| Có cảnh báo "QuickLock đã bị dừng" | Service bị ROM tiết kiệm pin chặn. Vào **App info → Battery saver → No restrictions** và bật **Autostart**. |
| Icon ổ khóa mở màn hình setup | Quyền trợ năng đang tắt — đây là hành vi có chủ đích (thay vì im lặng). |
| Không thấy icon ổ khóa thứ hai | Icon này là `activity-alias`; nó xuất hiện trong ngăn ứng dụng cùng tên **QuickLock** với icon ổ khóa xanh, khác với icon "QuickLock Settings" (nền xám). |

---

## 6. Service này *không* làm gì

* Không nhận accessibility event (`eventTypes = 0`, cả trong XML lẫn runtime).
* Không đọc nội dung cửa sổ (`canRetrieveWindowContent = false`).
* Không thực hiện gesture, không mô phỏng chạm (`canPerformGestures = false`).
* Không theo dõi app đang mở, không đọc text, không ghi log.
* Chỉ gọi đúng một hàm: `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`.

Toàn bộ điều này có thể kiểm chứng trong 2 file:

* `app/src/main/res/xml/accessibility_service_config.xml`
* `app/src/main/java/com/quicklock/app/accessibility/LockAccessibilityService.kt`
