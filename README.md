# Hide My Space

Module Xposed để ẩn ứng dụng và tạo không gian riêng tư trên thiết bị Android.

## Tính năng

- Ẩn ứng dụng khỏi các ứng dụng khác
- Hỗ trợ danh sách trắng (whitelist) và danh sách đen (blacklist)
- Tùy chọn loại trừ ứng dụng hệ thống
- Tương thích với các phiên bản Android từ 8.0 trở lên
- Giao diện người dùng thân thiện và dễ sử dụng

## Yêu cầu

- Android 8.0 trở lên
- LSPosed, EdXposed hoặc các framework Xposed tương tự
- Quyền root (tùy thuộc vào framework Xposed được sử dụng)

## Cài đặt

1. Cài đặt LSPosed hoặc framework Xposed tương tự
2. Cài đặt ứng dụng Hide My Space
3. Kích hoạt module trong LSPosed Manager
4. Khởi động lại thiết bị
5. Mở ứng dụng Hide My Space và cấu hình các ứng dụng cần ẩn

## Cách sử dụng

### Ẩn ứng dụng

1. Mở ứng dụng Hide My Space
2. Chọn ứng dụng bạn muốn cấu hình (ứng dụng sẽ không thể thấy các ứng dụng khác)
3. Chọn chế độ danh sách trắng hoặc danh sách đen
4. Chọn các ứng dụng bạn muốn ẩn (hoặc hiển thị nếu sử dụng danh sách trắng)
5. Lưu cấu hình

### Cài đặt

- **Chế độ tối**: Bật/tắt chế độ giao diện tối
- **Ghi log chi tiết**: Bật/tắt ghi log chi tiết (hữu ích cho việc gỡ lỗi)
- **Tự động khởi động lại**: Tự động khởi động lại thiết bị sau khi thay đổi cấu hình

## Giấy phép

Dự án này được phân phối dưới giấy phép GPL-3.0.

## Lưu ý

- Module này chỉ ẩn ứng dụng khỏi các ứng dụng khác, không xóa hoặc vô hiệu hóa ứng dụng
- Một số ứng dụng hệ thống có thể vẫn nhìn thấy tất cả các ứng dụng đã cài đặt
- Hiệu suất có thể bị ảnh hưởng trên các thiết bị cũ hoặc có cấu hình thấp 