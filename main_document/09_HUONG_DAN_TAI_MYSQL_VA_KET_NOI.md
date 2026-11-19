# 09 — HƯỚNG DẪN TẢI MYSQL VÀ KẾT NỐI VỚI MOTELONA

Hướng dẫn dành cho Windows và project Portfolio + MoteLona trong thư mục ZIP. Project hiện hỗ trợ MySQL 8.0.36 trở lên hoặc MySQL 8.4 LTS; hướng dẫn này chọn MySQL 8.4 LTS.

Nếu máy đã có MySQL Server tương thích, hãy kiểm tra version/port và dùng server hiện có thay vì cài thêm một instance.

## 1. Tải MySQL từ trang chính thức

1. Mở [MySQL Community Server 8.4 LTS cho Windows](https://dev.mysql.com/downloads/mysql/8.4.html).
2. Chọn **Windows (x86, 64-bit), MSI Installer**. Không chọn ZIP Archive nếu bạn muốn cài theo từng bước bằng giao diện.
3. Tải và mở file `.msi` vừa tải. Cài Server trước; ở bước sau sẽ cài Workbench riêng.

MySQL hiện cung cấp MSI cho Windows 64-bit. MSI cài MySQL Server và MySQL Configurator; Configurator dùng để cấu hình server sau khi cài. Xem [hướng dẫn cài MySQL 8.4 trên Windows](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html).

### Cài MySQL Workbench

Workbench là giao diện để kết nối server, chạy SQL và xem database. Tải bản Windows từ [MySQL Workbench Downloads](https://dev.mysql.com/downloads/workbench/), mở file `.msi` và làm theo trình cài đặt. Workbench là chương trình riêng với MySQL Server.

> Nếu Windows báo thiếu thành phần hệ thống, xem phần prerequisites trên trang tải Workbench hoặc hướng dẫn cài MySQL Windows ở trên. Không tải bộ cài từ trang không chính thức.

## 2. Cấu hình MySQL Server lần đầu

Trong **MySQL Configurator**:

1. Chọn loại cấu hình **Development** vì MySQL chạy trên máy cá nhân cùng VS Code.
2. Bật kết nối **TCP/IP** và giữ port **3306** nếu port này chưa được chương trình khác sử dụng. Đây cũng là port mặc định trong cấu hình MoteLona.
3. Đặt mật khẩu mạnh cho tài khoản quản trị `root` và lưu ở nơi riêng tư. Không dùng mật khẩu root làm mật khẩu public hoặc đưa vào GitHub.
4. Chọn cài MySQL như một **Windows Service** và tự khởi động cùng Windows để server sẵn sàng khi mở project.
5. Áp dụng cấu hình. Giữ server chỉ dùng cho kết nối cục bộ nếu đây là máy phát triển cá nhân; không mở port MySQL ra Internet.

MySQL Configurator hỗ trợ Development profile, TCP/IP/3306, root password và Windows Service; xem [các tùy chọn Configurator](https://dev.mysql.com/doc/refman/8.4/en/mysql-configurator-workflow-server.html).

## 3. Kết nối Workbench với server

1. Mở **MySQL Workbench**.
2. Chọn dấu **+** cạnh **MySQL Connections** để tạo kết nối mới.
3. Điền:

   | Trường | Giá trị |
   |---|---|
   | Connection Name | `MoteLona Local` |
   | Hostname | `127.0.0.1` |
   | Port | `3306` |
   | Username | `root` |

4. Bấm **Test Connection**, nhập mật khẩu root đã tạo ở bước 2. Nếu báo thành công, bấm **OK** rồi mở kết nối.

Kiểm tra server bằng cách mở tab SQL mới và chạy:

```sql
SELECT VERSION();
SELECT @@hostname, @@port;
```

Nếu bạn chọn port khác 3306, ghi lại port đó; cần dùng cùng port trong `.env` của backend.

## 4. Tạo database PortfolioAI_DB

Trong Workbench:

1. Mở **File → Open SQL Script...**.
2. Chọn file `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql` trong thư mục project đã giải nén.
3. Bấm nút tia sét **Execute** để chạy script.
4. Trong bảng **SCHEMAS**, bấm refresh. Cần thấy database `PortfolioAI_DB`.
5. Chạy xác nhận:

```sql
USE PortfolioAI_DB;
SHOW TABLES;
```

File này tạo database, các bảng và dữ liệu khởi đầu. **Không chạy** `Project/1/motelona-ai/database/PortfolioAI_DB.sql`; file đó là ghi chú của phương án SQL Server cũ, không phải script cài MySQL.

### Nếu đã có database MoteLona cũ

Trước hết sao lưu bằng **Server → Data Export**. Sau đó chạy `Project/1/motelona-ai/database/V002_BilingualContent_MySQL.sql` bằng tài khoản quản trị trước khi khởi động backend mới. Migration này thêm các cột song ngữ còn thiếu; không dùng script khởi tạo mới để thay database đang có dữ liệu.

## 5. Tạo tài khoản cho backend

Không dùng `root` để chạy ứng dụng. Trong Workbench đang kết nối bằng root, mở tab SQL và chạy đoạn dưới đây. Thay `YOUR_STRONG_PASSWORD` bằng mật khẩu bạn tự chọn; dùng cùng một mật khẩu cho hai host.

```sql
CREATE USER IF NOT EXISTS 'portfolio_app'@'localhost'
IDENTIFIED BY 'YOUR_STRONG_PASSWORD';

CREATE USER IF NOT EXISTS 'portfolio_app'@'127.0.0.1'
IDENTIFIED BY 'YOUR_STRONG_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES
ON PortfolioAI_DB.* TO 'portfolio_app'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES
ON PortfolioAI_DB.* TO 'portfolio_app'@'127.0.0.1';

FLUSH PRIVILEGES;
```

Các quyền chỉ áp dụng cho `PortfolioAI_DB`. `CREATE` cần để Spring SQL init tạo bảng còn thiếu; `REFERENCES` cần khi tạo bảng có foreign key. Nếu tài khoản `portfolio_app` đã tồn tại và mật khẩu không khớp, hãy đổi mật khẩu tài khoản bằng Workbench/SQL của quản trị viên, rồi cập nhật cùng giá trị trong `.env`.

Chọn mật khẩu không chứa dấu nháy đơn (`'`) để có thể dán trực tiếp vào SQL mẫu. Trong `.env`, ghi mật khẩu không bọc trong dấu nháy.

## 6. Cấu hình kết nối trong VS Code

1. Mở thư mục `Project/1/motelona-ai/backend/` trong VS Code.
2. Sao chép `.env.example` thành `.env` ngay trong thư mục `backend`.
3. Mở `.env`, điền các giá trị tương ứng:

```dotenv
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=PortfolioAI_DB
MYSQL_USERNAME=portfolio_app
MYSQL_PASSWORD=YOUR_STRONG_PASSWORD

MOTELONA_CORS_ORIGINS=http://localhost:5500,http://127.0.0.1:5500,http://localhost:8000,http://127.0.0.1:8000
MOTELONA_SERVER_ADDRESS=127.0.0.1
MOTELONA_PORT=8080
```

`MYSQL_PASSWORD` phải khớp với mật khẩu tạo ở bước trước. Nếu server dùng port khác 3306, thay `MYSQL_PORT`. Không gửi `.env` cho người khác hoặc đưa file này lên GitHub; `.gitignore` của project đã loại `.env` khỏi Git.

## 7. Khởi động và xác nhận kết nối

Trong Terminal PowerShell ở thư mục `Project/1/motelona-ai/backend/`, chạy:

```powershell
.\run-local.ps1
```

Script này đọc `.env`, chạy Maven kiểm tra/build rồi khởi động Spring Boot. Giữ cửa sổ Terminal mở. Lần chạy đầu tiên cần Internet để tải Maven và thư viện Java. Nếu chỉ chạy `mvnw.cmd spring-boot:run` trực tiếp, `.env` sẽ không được script nạp tự động; dùng `run-local.ps1` cho cấu hình này.

Mở URL kiểm tra kết nối database:

```text
http://127.0.0.1:8080/api/portfolio/status
```

Kết quả thành công có dạng tương tự:

```json
{"enabled":true,"connected":true,"database":"PortfolioAI_DB","provider":"mysql"}
```

Endpoint `/api/ai/health` chỉ kiểm tra trạng thái assistant/backend; dùng `/api/portfolio/status` để xác nhận riêng kết nối MySQL.

Sau đó mở thư mục gốc `Portfolio_Web/` bằng VS Code, chạy `index.html` bằng Live Server và thử mở chat. Không mở riêng thư mục `assets/` hoặc `Project/` làm thư mục website gốc.

## 8. Lỗi thường gặp

| Thông báo/vấn đề | Kiểm tra |
|---|---|
| `Communications link failure` | MySQL Windows Service đã chạy chưa; đúng `MYSQL_HOST` và port chưa; port 3306 có bị đổi không. |
| `Access denied for user` | Mật khẩu trong `.env` có khớp tài khoản; đã tạo host account `127.0.0.1`; quyền đã cấp đúng database chưa. |
| `Unknown database 'PortfolioAI_DB'` | Chạy script `PortfolioAI_MySQL.sql` bằng root trong Workbench. |
| Lỗi quyền tạo bảng hoặc foreign key | Chạy lại các lệnh `GRANT` bằng root; xác nhận đã cấp `CREATE` và `REFERENCES` cho database. |
| Port 3306 đang được dùng | Chọn port trống trong MySQL Configurator rồi cập nhật `MYSQL_PORT` trong `.env`. |
| Bảng đã cũ/thiếu cột sau khi cập nhật | Sao lưu, chạy migration `V002_BilingualContent_MySQL.sql` bằng tài khoản quản trị, rồi khởi động lại backend. |
| `/api/ai/health` trả backend up nhưng dữ liệu DB không lên | Mở `/api/portfolio/status`; health endpoint AI không thay cho kiểm tra DB. |

## 9. Link tải và tài liệu chính thức

- [MySQL Community Server 8.4 LTS — Windows MSI](https://dev.mysql.com/downloads/mysql/8.4.html)
- [MySQL Workbench — Windows downloads](https://dev.mysql.com/downloads/workbench/)
- [Cài MySQL trên Windows](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html)
- [Cấu hình MySQL Server và port 3306](https://dev.mysql.com/doc/refman/8.4/en/mysql-configurator-workflow-server.html)
- [Quyền REFERENCES khi tạo khóa ngoại](https://dev.mysql.com/doc/refman/8.4/en/create-table-foreign-keys.html)
- Hướng dẫn chạy project: `README.md` và `main_document/02_HUONG_DAN_CAI_DAT_SU_DUNG.md`.
