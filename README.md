# Portfolio cá nhân và MoteLona

Website portfolio chạy bằng HTML, CSS và JavaScript. Trợ lý MoteLona dùng backend Java 21/Spring Boot và MongoDB.

## Chạy trên máy

Cần JDK 21+, Maven 3.x, MongoDB Server và Python 3. Mở PowerShell ở thư mục này rồi chạy:

~~~powershell
.\tools\start-motelona-local.ps1
~~~

Mở http://127.0.0.1:5500/. MongoDB Compass kết nối tới mongodb://127.0.0.1:27017.

## Học cấu trúc dự án

- [Bắt đầu đọc tài liệu](docs/README.md)
- [Dữ liệu được lưu và chạy qua hệ thống như thế nào](docs/mongodb-data-guide.md)
- [Sơ đồ thư mục và nơi sửa từng chức năng](docs/project-map.md)
- [Cài đặt và xử lý lỗi](docs/setup-and-run.md)
- [Luồng AI và backend Java](docs/ai-architecture.md)

MoteLona dùng MongoDB database motelona_portfolio. Tất cả cấu hình, mã nguồn và tài liệu của dự án đều hướng tới MongoDB.

