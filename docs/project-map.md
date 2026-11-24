# Sơ đồ thư mục dự án

Sơ đồ dưới đây tập trung vào file cần đọc và sửa. Thư mục target/ là kết quả build tự sinh nên được lược khỏi sơ đồ; hãy sửa mã trong src/, không sửa class đã biên dịch.

~~~text
D:\Portfolio_Web
├── README.md                         Hướng dẫn mở nhanh
├── index.html                        Trang Portfolio
├── docs/                             Tài liệu học dự án
├── assets/
│   ├── css/                          Giao diện Portfolio và MoteLona
│   ├── images/                       Hình ảnh
│   └── js/
│       ├── portfolio-data.js         Đọc Portfolio từ API MongoDB
│       ├── language-toggle.js        Chuyển EN/VI
│       ├── portfolio-theme.js        Màu sắc
│       └── modules/                  Animation và hành vi dùng chung
├── Project/1/
│   ├── motelona.html                 Trang giới thiệu MoteLona
│   └── motelona-ai/
│       ├── frontend/
│       │   ├── motelona.js           Widget chat và gọi API
│       │   ├── motelona-config.js    Địa chỉ backend
│       │   └── motelona.css          Giao diện widget
│       ├── backend/
│       │   ├── pom.xml               Thư viện và build Java
│       │   └── src/
│       │       ├── main/
│       │       │   ├── java/com/motelona/assistant/
│       │       │   │   ├── controller/   REST API
│       │       │   │   ├── service/      Nghiệp vụ và truy cập dữ liệu
│       │       │   │   ├── memory/       Memory người dùng
│       │       │   │   ├── ai/           Phân loại và tìm nội dung cục bộ
│       │       │   │   ├── search/       Tra cứu web
│       │       │   │   └── config/       MongoDB, CORS, bảo vệ API
│       │       │   └── resources/
│       │       │       ├── application.yml
│       │       │       └── portfolio-seed.json
│       │       └── test/             Test Java
│       └── database/
│           └── README.md             Ghi chú dữ liệu MongoDB
├── tools/
│   ├── start-motelona-local.ps1      Chạy website, backend và MongoDB cục bộ
│   └── start-mongodb-local.ps1       Chỉ khởi động MongoDB portable
~~~

## Mỗi phần làm gì?

- **index.html + assets/**: trang Portfolio và giao diện. portfolio-data.js gọi API để lấy dữ liệu động; nếu backend tắt, HTML vẫn có nội dung dự phòng.
- **frontend/**: widget hiển thị chat, gửi câu hỏi và xử lý thao tác cuộn/tô sáng. Không đặt API key ở đây.
- **backend/controller/**: nhận HTTP request và trả JSON.
- **backend/service/**: điều phối xử lý, đọc/ghi MongoDB, gọi model và tìm kiếm.
- **backend/config/MongoPortfolioInitializer.java**: tạo index, seed dữ liệu ban đầu và xử lý nhãn dữ liệu cũ.
- **backend/resources/portfolio-seed.json**: dữ liệu mẫu cho database hoặc collection trống.
- **backend/src/test/**: test; chạy từ backend bằng mvn test.
- **target/**: file build tự sinh; có thể tạo lại bằng Maven, không sửa trực tiếp.

## Tìm nhanh nơi cần sửa

| Việc cần làm | File/thư mục |
|---|---|
| Sửa nội dung hoặc cấu trúc HTML Portfolio | index.html |
| Sửa giao diện Portfolio | assets/css/ |
| Sửa ngôn ngữ, theme hoặc animation | assets/js/ |
| Sửa cách Portfolio lấy dữ liệu từ API | assets/js/portfolio-data.js |
| Sửa widget trợ lý | Project/1/motelona-ai/frontend/ |
| Thêm/sửa endpoint | backend/src/main/java/com/motelona/assistant/controller/ |
| Sửa logic hoặc lưu trữ MongoDB | backend/src/main/java/com/motelona/assistant/service/ và memory/ |
| Sửa Mongo index hoặc dữ liệu khởi tạo | MongoPortfolioInitializer.java và portfolio-seed.json |
| Sửa cấu hình host, database, model | backend/src/main/resources/application.yml hoặc backend/.env |
| Xem dữ liệu đang dùng | MongoDB Compass → motelona_portfolio |

