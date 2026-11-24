# Hướng dẫn cài đặt và sử dụng

Để tìm hiểu dự án, bắt đầu từ [lộ trình đọc](README.md), [sơ đồ thư mục](project-map.md), [hướng dẫn MongoDB](mongodb-data-guide.md) và [kiến trúc AI/Java](ai-architecture.md).

## 1. Phần mềm cần có

- JDK 21 trở lên và Maven 3.x.
- MongoDB Community Server đang chạy. Cài bằng [hướng dẫn chính thức MongoDB 8.0 trên Windows](https://www.mongodb.com/docs/v8.0/tutorial/install-mongodb-on-windows/). Nếu cài dưới dạng Windows Service, trình cài đặt có thể khởi động dịch vụ sau khi cài.
- MongoDB Compass để xem dữ liệu. Compass là ứng dụng client; nếu chỉ cài Compass, cần cài thêm MongoDB Server.
- Python 3 để phục vụ các file website tĩnh qua HTTP.

Trong PowerShell, kiểm tra Server đang nghe cổng mặc định:

~~~powershell
Test-NetConnection 127.0.0.1 -Port 27017
~~~

Kết nối Compass tới `mongodb://127.0.0.1:27017`. Backend mặc định dùng database `motelona_portfolio`.

Nếu đã giải nén MongoDB Server dạng ZIP trong `%LOCALAPPDATA%\Programs\MongoDB\Server`, có thể khởi động server không cần quyền quản trị bằng script ở workspace:

~~~powershell
Set-Location D:\Portfolio_Web
.\tools\start-mongodb-local.ps1
~~~

Script lưu dữ liệu tại `%LOCALAPPDATA%\MongoDB\data\db`, log tại `%LOCALAPPDATA%\MongoDB\log\mongod.log` và chỉ mở kết nối loopback. Khi cài Server thành Windows Service, để dịch vụ đó chạy và không cần gọi script.

## 2. Chạy backend

Cách nhanh để khởi động MongoDB, backend và website cùng lúc:

~~~powershell
Set-Location D:\Portfolio_Web
.\tools\start-motelona-local.ps1
~~~

Launcher đọc các biến cấu hình backend từ `backend/.env`, không in API key và ghi log vào `%LOCALAPPDATA%\MoteLona\log`. Khi khởi động thành công, mở `http://127.0.0.1:5500/`.

Mở PowerShell tại thư mục gốc `D:\Portfolio_Web`:

~~~powershell
Set-Location .\Project\1\motelona-ai\backend
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/motelona_portfolio"
$env:MOTELONA_PROJECT_ROOT = (Resolve-Path ..\..\..\..).Path
~~~

Các key tạo sinh là tùy chọn. Nhập chúng ở backend khi muốn dùng provider; không đặt key trong HTML hoặc JavaScript:

~~~powershell
$secureKey = Read-Host "Gemini API key (để trống nếu chưa dùng)" -AsSecureString
$env:GEMINI_API_KEY = [System.Net.NetworkCredential]::new("", $secureKey).Password
$groqKey = Read-Host "Groq API key dự phòng (tùy chọn)" -AsSecureString
$env:GROQ_API_KEY = [System.Net.NetworkCredential]::new("", $groqKey).Password
$openRouterKey = Read-Host "OpenRouter API key dự phòng (tùy chọn)" -AsSecureString
$env:OPENROUTER_API_KEY = [System.Net.NetworkCredential]::new("", $openRouterKey).Password
mvn spring-boot:run
~~~

Nếu không có key, Portfolio và câu trả lời dự phòng cục bộ vẫn chạy được. Khi khởi động, backend tạo index và chỉ nạp seed vào collection trống.

## 3. Chạy website riêng

Launcher ở mục 2 đã chạy website. Nếu chỉ cần chạy website tĩnh riêng, mở PowerShell tại thư mục gốc dự án:

~~~powershell
Set-Location D:\Portfolio_Web
python -m http.server 5500 --bind 127.0.0.1
~~~

Truy cập `http://127.0.0.1:5500/`. Trang MoteLona ở `http://127.0.0.1:5500/Project/1/motelona.html`. Không mở `index.html` bằng cách nhấp đúp vì trình duyệt cần HTTP để gọi API backend.

## 4. Kiểm tra nhanh

- `http://127.0.0.1:8080/api/ai/health` — trạng thái backend và model provider.
- `http://127.0.0.1:8080/api/portfolio/status` — database đang kết nối; provider cần là `mongodb`.
- `http://127.0.0.1:8080/api/portfolio/public` — hồ sơ, học vấn, kỹ năng và dự án đã seed.
- Compass → `motelona_portfolio` — collection `portfolio_profile`, `portfolio_education`, `portfolio_experience`, `portfolio_skill_categories`, `portfolio_projects` và các collection hoạt động.

Nếu nhận lỗi “Không kết nối được với backend”, kiểm tra backend có đang chạy ở cổng 8080 và MongoDB Server có đang nghe cổng 27017 không. Nếu log báo `MongoSocketOpenException` hoặc `Connection refused`, Compass đang mở không có nghĩa Server đã chạy. URI có tên database sai thì ứng dụng sẽ tạo/sử dụng database ở URI đó; hãy kiểm tra `MONGODB_URI` trong terminal backend.

## 5. Chạy dự án ở máy chủ

Backend và website phải được triển khai ở nơi người dùng truy cập được. Bật HTTPS, cấu hình `MOTELONA_CORS_ORIGINS` với origin website chính xác và đặt `window.MOTELONA_API_BASE` thành URL HTTPS của backend. Dùng MongoDB Atlas hoặc MongoDB Server được bảo vệ; không mở MongoDB trực tiếp ra Internet. Tạo database user giới hạn quyền trong database ứng dụng và lưu thông tin xác thực bằng secret/environment của nền tảng triển khai.

Các biến chính:

| Biến | Công dụng |
|---|---|
| `MONGODB_URI` | URI kết nối, bao gồm host, port và database đích. Mặc định: `mongodb://127.0.0.1:27017/motelona_portfolio`. |
| `MOTELONA_PROJECT_ROOT` | Gốc workspace cho tìm kiếm file dự án trong allow-list. |
| `GEMINI_API_KEY`, `GROQ_API_KEY`, `OPENROUTER_API_KEY` | API key riêng của từng model provider; để trống sẽ bỏ qua provider. |
| `MOTELONA_CORS_ORIGINS` | Danh sách origin website được phép gọi backend, phân cách bằng dấu phẩy. |
| `MOTELONA_SERVER_ADDRESS`, `MOTELONA_PORT` | Địa chỉ bind và cổng backend. Mặc định `127.0.0.1:8080`. |

## 6. Quyền riêng tư và vận hành

- Lịch sử chỉ lưu khi khách truy cập bật lựa chọn lưu lịch sử. Backend giữ tối đa 200 message cho một session; xóa history sẽ xóa tài liệu tin nhắn và session metadata.
- Memory chỉ lưu sau khi bật và yêu cầu ghi nhớ rõ ràng. API key, password và secret bị chặn trước khi ghi.
- Session ID không phải tài khoản đăng nhập. Không đưa thông tin cá nhân nhạy cảm vào chat.
- Các API key có thể được chuyển đến provider kế tiếp khi provider trước lỗi; chỉ cấu hình dịch vụ mà bạn đồng ý gửi câu hỏi/evidence tới.
- MongoDB seed chỉ thêm bản ghi khi collection tương ứng trống. Sửa dữ liệu trong Compass không bị ghi đè ở lần chạy sau.
- Backend chỉ dùng MongoDB. Portfolio nằm trong các collection `portfolio_*`; lịch sử và memory tùy chọn nằm trong `conversation_*` và `user_memories`.

## 7. Chẩn đoán

- **Backend không khởi động:** xem lỗi đầu tiên trong terminal; xác minh JDK 21 và chạy Maven từ thư mục `backend`.
- **Không kết nối database:** kiểm tra MongoDB Community Server và URI. Mặc định phải chạy MongoDB trên `127.0.0.1:27017`.
- **Portfolio API rỗng:** kiểm tra `portfolio-seed.json`, collection đích và database ở URI. Seed chỉ tự nạp nếu collection trống.
- **Frontend báo lỗi CORS:** dùng origin trùng chính xác với cấu hình `MOTELONA_CORS_ORIGINS`.
- **AI không tạo sinh:** mở `/api/ai/health`, kiểm tra key/model/quota; fallback cục bộ không phải LLM tạo văn bản tổng quát.
