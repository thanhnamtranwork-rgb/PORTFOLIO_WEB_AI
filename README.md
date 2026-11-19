# Portfolio Web + MoteLona

Portfolio cá nhân tĩnh, trang giới thiệu MoteLona và backend Java/Spring Boot dùng MySQL. Trợ lý chạy NLP cục bộ, không cần khóa API AI; tìm web được thực hiện khi khách truy cập chủ động yêu cầu và dùng trang HTML công khai của DuckDuckGo.

Để học và chuẩn bị thuyết trình đồ án theo mã nguồn, xem [`main_document/08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md`](main_document/08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md). Tài liệu giải thích luồng Java/Spring Boot/MySQL, thuật toán NLP hiện có, vị trí file và câu hỏi bảo vệ thường gặp.

## Chạy trên Windows bằng VS Code

### 1. Cài phần mềm cần thiết

- JDK 21
- MySQL Server 8.4 LTS (khuyến nghị; project cũng hỗ trợ 8.0.36+) và MySQL Workbench
- Visual Studio Code và extension Live Server

Không cần cài Maven riêng vì dự án có Maven Wrapper. Lần build đầu cần Internet để tải Maven và thư viện Java.

### 2. Tạo database

Với database mới, mở `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql` trong MySQL Workbench và chạy toàn bộ script. Script tạo database `PortfolioAI_DB`, các bảng, trường nội dung song ngữ và dữ liệu khởi đầu. Nếu đang dùng database MoteLona cũ, hãy sao lưu rồi chạy một lần `Project/1/motelona-ai/database/V002_BilingualContent_MySQL.sql` trước khi khởi động backend mới.

Nếu chưa cài MySQL, xem hướng dẫn từng bước kèm link tải chính thức trong [`main_document/09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md`](main_document/09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md).

### 3. Cấu hình và chạy backend

1. Mở `Project/1/motelona-ai/backend/` trong VS Code.
2. Sao chép `.env.example` thành `.env` trong cùng thư mục và điền thông tin MySQL của bạn. Không đưa `.env` lên GitHub.
3. Mở Terminal tại thư mục backend và chạy:

   ```powershell
   .\run-local.ps1
   ```

4. Giữ terminal này mở. Khi backend sẵn sàng, kiểm tra `http://127.0.0.1:8080/api/ai/health`.

### 4. Chạy website

1. Mở thư mục gốc `Portfolio_Web/` bằng VS Code.
2. Mở `index.html` bằng **Live Server**. Không mở `assets/` hoặc `Project/` làm thư mục gốc.
3. Mở chat MoteLona trong portfolio hoặc trang `Project/1/motelona.html`.

Nếu Live Server dùng cổng khác 5500, thêm đúng địa chỉ website vào `MOTELONA_CORS_ORIGINS` trong backend `.env`, sau đó khởi động lại backend.

## Ba biểu mẫu trong đồ án

1. **Biểu mẫu liên hệ** trên `index.html` nhận họ tên, email và nội dung liên hệ.
2. **Biểu mẫu trò chuyện AI** trong `Project/1/motelona-ai/frontend/motelona.js` gửi câu hỏi đến backend Java; hội thoại được lưu trong MySQL khi backend kết nối thành công.
3. **Biểu mẫu tìm kiếm trên trang** trong cùng giao diện MoteLona giúp tìm nội dung trong trang hiện tại.

Khi trình bày, có thể lần theo luồng từ biểu mẫu trò chuyện, qua `AiController` và `AiChatService`, đến `JdbcTemplate` và bảng `conversation_messages` trong MySQL.

## Thử MoteLona

- `Bạn có những kỹ năng nào?`
- `Đưa tôi đến đoạn nói về MySQL schema.`
- `Giải thích TF-IDF.`
- `Tìm trên Internet: cách cấu hình MySQL Connector/J trên Java 21.`
- Hoặc bật **Tìm web** trước khi gửi câu hỏi.

Khi tìm web, câu hỏi được gửi từ backend Java đến DuckDuckGo để lấy tiêu đề, trích đoạn và liên kết nguồn. Không có AI API hoặc search API key. Máy chủ cần kết nối HTTPS ra Internet; nguồn trả về cần được mở để kiểm tra trước khi sử dụng. Chat và dữ liệu Portfolio không gửi đến mô hình AI bên ngoài.

## Kiểm tra mã nguồn

Tại thư mục backend:

```powershell
.\mvnw.cmd clean test
```

Kiểm tra tích hợp với MySQL đang chạy:

```powershell
.\VERIFY_MOTELONA.ps1
```

Tại thư mục gốc, có thể kiểm tra cú pháp JavaScript đã cài bằng Node.js:

```powershell
Get-ChildItem -Recurse -Filter *.js | ForEach-Object { node --check $_.FullName }
```

## Đưa lên GitHub

Mở thư mục gốc `Portfolio_Web/` trong VS Code, khởi tạo hoặc chọn Git repository, kiểm tra mục `.env` không xuất hiện trong danh sách thay đổi rồi commit và push. File `.github/copilot-instructions.md` cung cấp cho Copilot quy tắc làm việc trong repository.

GitHub Pages chỉ phục vụ frontend tĩnh. Chat, MySQL và tìm web cần backend Java được host riêng, database MySQL và URL HTTPS công khai được cấu hình trong `motelona-config.js` hoặc trước khi nạp file đó.

Khi thêm học vấn, kỹ năng hoặc dự án vào MySQL, điền thêm trường tiếng Việt tương ứng như `MajorVi`, `SkillNameVi`, `TitleVi` và `DescriptionVi`. Nếu trường dịch để trống, website giữ nguyên nội dung gốc thay vì tự tạo bản dịch.

## Tài liệu

Xem `main_document/00_AI_MASTER_GUIDE.md` để biết kiến trúc và giới hạn, `main_document/02_HUONG_DAN_CAI_DAT_SU_DUNG.md` để cài/chạy, và `main_document/07_CHECKLIST_KIEM_THU.md` để nghiệm thu.
