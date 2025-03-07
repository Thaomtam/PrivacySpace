# Hướng dẫn Build Hide My Space

## Chuẩn bị

1. Cài đặt Android Studio: https://developer.android.com/studio
2. Cài đặt JDK 17 hoặc cao hơn
3. Tải tệp gradle-wrapper.jar từ: https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar
4. Đặt tệp gradle-wrapper.jar vào thư mục `gradle/wrapper/`

## Build ứng dụng

### Sử dụng Android Studio

1. Mở Android Studio
2. Chọn "Open an existing Android Studio project"
3. Chọn thư mục `HideMySpace`
4. Đợi Android Studio đồng bộ dự án
5. Chọn "Build > Build Bundle(s) / APK(s) > Build APK(s)"
6. Tệp APK sẽ được tạo tại `app/build/outputs/apk/debug/app-debug.apk`

### Sử dụng Command Line

1. Mở terminal/command prompt
2. Di chuyển đến thư mục `HideMySpace`
3. Chạy lệnh:
   - Windows: `.\gradlew.bat assembleDebug`
   - Linux/Mac: `./gradlew assembleDebug`
4. Tệp APK sẽ được tạo tại `app/build/outputs/apk/debug/app-debug.apk`

## Cài đặt

1. Cài đặt LSPosed Framework trên thiết bị Android
2. Cài đặt tệp APK đã build
3. Kích hoạt module trong LSPosed Manager
4. Khởi động lại thiết bị
5. Mở ứng dụng Hide My Space và cấu hình các ứng dụng cần ẩn

## Gỡ lỗi

Nếu gặp lỗi khi build:

1. Đảm bảo đã cài đặt JDK 17 hoặc cao hơn
2. Đảm bảo đã đặt tệp gradle-wrapper.jar vào đúng thư mục
3. Kiểm tra biến môi trường JAVA_HOME đã được thiết lập đúng
4. Xóa thư mục `.gradle` và thử build lại

## Yêu cầu hệ thống

- Android 8.0 (API 26) trở lên
- LSPosed, EdXposed hoặc các framework Xposed tương tự
- Quyền root (tùy thuộc vào framework Xposed được sử dụng) 