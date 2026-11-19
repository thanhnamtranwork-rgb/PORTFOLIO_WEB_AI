# 02 — HƯỚNG DẪN CÀI ĐẶT, CHẠY VÀ SỬ DỤNG PORTFOLIO + MOTELONA

Tài liệu áp dụng cho cấu trúc ZIP hiện tại. Hướng dẫn ưu tiên Windows và VS Code; các lệnh Maven/Java có thể điều chỉnh tương ứng trên macOS/Linux.

## 1. Tổng quan trước khi cài

| Thành phần | Công nghệ / vị trí | Có cần để xem website? |
|---|---|---|
| Portfolio chính | `index.html`, HTML/CSS/JavaScript | Có; không cần backend để xem nội dung tĩnh. |
| Trang chi tiết MoteLona | `Project/1/motelona.html` | Có; không cần backend để đọc nội dung. |
| Chatbot frontend | `Project/1/motelona-ai/frontend/` | Cần file HTML/JS/CSS; để chat trực tuyến cần backend. |
| Backend | Java 21 + Spring Boot; mặc định `127.0.0.1:8080` | Không cần để xem trang; cần để sử dụng API/chat. |
| Database | MySQL 8, database `PortfolioAI_DB` | Không cần để xem bản tĩnh; cần cho dữ liệu DB, contact, history/memory. |
| AI engine | Java NLP tự xây | Không cần API key AI bên thứ ba. |

**Cách hoạt động:** MoteLona dùng Java NLP cục bộ, TF-IDF, cosine similarity, truy xuất tri thức và từ điển; không gọi API AI, không cần AI API key và không tự học trực tuyến. Có hai loại tìm kiếm: nội dung website được gửi từ trang hiện tại và tìm web opt-in qua trang HTML công khai của DuckDuckGo. Tìm web cần Internet outbound trên máy chạy backend, trả trích đoạn/nguồn để người xem tự kiểm chứng; không có LLM tổng quát để tổng hợp toàn bộ trang ngoài.

`README.md` ở thư mục gốc có hướng dẫn chạy nhanh bằng VS Code, Live Server, Java 21 và MySQL.

## 2. Phần mềm cần cài

| Phần mềm | Mục đích | Cách kiểm tra |
|---|---|---|
| Chrome / Edge / Firefox hiện đại | Mở và kiểm thử giao diện | Mở được `index.html` qua HTTP local. |
| Visual Studio Code | Chỉnh sửa dự án | Mở thư mục gốc `Portfolio_Web`. |
| Live Server (VS Code extension) hoặc HTTP server tương đương | Chạy frontend qua HTTP local | Có địa chỉ như `http://127.0.0.1:5500`. |
| JDK 21 | Chạy backend | `java -version` hiển thị phiên bản 21. |
| MySQL Server 8.4 LTS (khuyến nghị) hoặc 8.0.36+ | Database | Kết nối được cổng 3306. |
| MySQL Workbench | Quản lý DB, chạy SQL | Kết nối được tới MySQL Server. |

Dự án có Maven Wrapper (`mvnw`, `mvnw.cmd`), không bắt buộc cài Maven riêng. Lần build đầu cần Internet để tải Maven và các dependency.

## 3. Mở giao diện Portfolio

1. Giải nén ZIP vào đường dẫn dễ tìm, ví dụ `D:\Projects\Portfolio_Web`.
2. Trong VS Code chọn **File → Open Folder** và mở thư mục gốc `Portfolio_Web`, nơi có `index.html`, `assets/`, `Project/`, `main_document/`.
3. Cài Live Server nếu chưa có.
4. Nhấn chuột phải `index.html` → **Open with Live Server**.
5. Trình duyệt sẽ mở website qua HTTP local.

Không mở riêng `assets/` hoặc `Project/` làm thư mục gốc vì relative path trong HTML đang được viết theo cấu trúc hiện tại.

Website vẫn có nội dung tĩnh khi Java/MySQL chưa chạy. Khi đó chatbot báo backend offline; phần dữ liệu động từ MySQL không hydrate và form không thể lưu vào database.

### 3.1 Điều khiển trình duyệt

- Zoom in: `Ctrl` + `+` hoặc `Ctrl` + lăn chuột lên.
- Zoom out: `Ctrl` + `-` hoặc `Ctrl` + lăn chuột xuống.
- Reset browser zoom: `Ctrl` + `0`.
- Tải lại bỏ cache: `Ctrl` + `Shift` + `R`.

### 3.2 Tỷ lệ hiển thị đang được cài trong mã nguồn

Một số section Portfolio trên `index.html` được mô phỏng tỷ lệ hiển thị 75% bằng CSS khi Chrome ở 100%; đây không phải thay đổi cài đặt zoom thực tế của Chrome. Trang chi tiết MoteLona dùng kích thước tự nhiên, không thu phóng toàn trang.

- Portfolio: `assets/css/portfolio-visual-scale.css` áp dụng `zoom: 0.75` cho section ngoài Hero khi `min-width: 1025px`, có hover và pointer chính xác.
- Các section bị scale gồm `#about`, `#education`, `#skills`, `#projects`, `#contact` (selector áp dụng mọi `.animfolio-section` trừ Hero).
- `#hero`, thanh điều hướng và launcher/chat nổi không nằm trong lớp scale 75% này.
- Trang chi tiết MoteLona: `assets/css/motelona-premium.css` quản lý layout, còn `assets/css/typography-refinement.css` là lớp typography chung được nạp sau cùng trên cả hai trang; không áp dụng `zoom` toàn trang.
- Màn hình nhỏ và thiết bị touch không áp dụng cơ chế zoom desktop; dùng responsive bình thường.
- `assets/js/homepage-zoom.js` là cơ chế riêng để giữ kích thước cảm nhận của Hero/launcher theo `devicePixelRatio`; không phải file điều khiển tỷ lệ 75% cho các section khác.

Khi chỉnh CSS/JS liên quan tới tỷ lệ, phải kiểm tra lại vùng nền full-width, canh giữa, vị trí sticky navigation, anchor scroll, fixed chat, trang chi tiết, desktop và mobile. Trang chi tiết MoteLona phải dễ đọc ở browser zoom 100% mà không cần CSS zoom. Đọc `00_AI_MASTER_GUIDE.md` và `05_BAN_DO_PHU_THUOC.md` trước khi sửa.

## 4. Tạo database MySQL

Nếu đây là lần đầu cài MySQL trên Windows, hãy làm theo hướng dẫn từ bước tải MSI và cài Server/Workbench trong `main_document/09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md`.

1. Mở MySQL Workbench và kết nối MySQL Server local.
2. Mở `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql`.
3. Chạy toàn bộ script.
4. Kiểm tra database `PortfolioAI_DB` đã được tạo.

Với database đã tạo từ bản cũ, hãy sao lưu trước rồi chạy một lần `Project/1/motelona-ai/database/V002_BilingualContent_MySQL.sql` để thêm các cột nội dung tiếng Việt. Script `schema.sql` của Spring Boot tạo bảng mới nhưng không tự sửa cấu trúc bảng đã tồn tại.

**Chỉ dùng `PortfolioAI_MySQL.sql` làm script cài đặt.** File `Project/1/motelona-ai/database/PortfolioAI_DB.sql` hiện là ghi chú/placeholder về phương án SQL Server cũ, không phải schema database và không dùng để cài phiên bản hiện tại.

Các bản ghi học vấn, kỹ năng và dự án có thể điền các cột `MajorVi`, `DegreeVi`, `DescriptionVi`, `CategoryNameVi`, `SkillNameVi`, `TitleVi`, `ShortDescriptionVi` và `FullDescriptionVi`. Nếu cột tiếng Việt chưa có nội dung, giao diện giữ nguyên giá trị gốc.

### 4.1 Tạo tài khoản ứng dụng (khuyến nghị)

Không nên dùng tài khoản `root` để chạy backend. Sau khi tạo database, chạy dưới tài khoản quản trị MySQL (thay mật khẩu mẫu bằng mật khẩu mạnh của bạn):

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

Nếu dùng username khác, cập nhật `.env` và chỉ cấp quyền trên database cần thiết.

## 5. Cấu hình backend Java

Đi tới:

`Project/1/motelona-ai/backend/`

1. Sao chép `.env.example` thành file `.env` trong cùng thư mục.
2. Mở `.env` và điền đúng mật khẩu/tài khoản MySQL.
3. Không gửi file `.env` chứa mật khẩu cho người khác và không đưa file này lên GitHub.

Ví dụ cấu hình local:

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

Lưu ý:

- `MYSQL_PASSWORD` phải khớp mật khẩu MySQL.
- Nếu Live Server dùng cổng khác, thêm origin chính xác, ví dụ `http://127.0.0.1:5501`, vào `MOTELONA_CORS_ORIGINS`.
- CORS phải là danh sách origin cụ thể; không dùng `*` để xử lý lỗi.
- `motelona-config.js` mặc định dùng `http://127.0.0.1:8080` khi chạy localhost hoặc `file:`. Khi truy cập website public, trình duyệt không tự gọi `127.0.0.1` của khách truy cập.
- Không đặt mật khẩu MySQL hoặc secret vào `motelona-config.js`, HTML hay JavaScript.

## 6. Khởi động backend trên Windows

1. Đảm bảo MySQL Server đang chạy.
2. Mở PowerShell tại thư mục `Project/1/motelona-ai/backend/`.
3. Chạy:

```powershell
.\run-local.ps1
```

Script sẽ đọc `.env` (nếu có), yêu cầu `MYSQL_PASSWORD` không rỗng, chạy `mvnw.cmd clean verify`, sau đó khởi động Spring Boot.

Nếu đã build và verify xong, muốn bỏ qua bước build:

```powershell
.\run-local.ps1 -SkipBuild
```

Giữ cửa sổ PowerShell mở trong lúc dùng chatbot. Để dừng backend, nhấn `Ctrl` + `C` trong cửa sổ đó.

### 6.1 Kiểm tra backend

Mở:

`http://127.0.0.1:8080/api/ai/health`

Health response nhận diện engine Java local (`provider=java-local`, `engine=self-built-nlp`, `apiKeyRequired=false`) và provider `DuckDuckGo HTML (no API key)`. `internetSearchEnabled=true` cho biết adapter được tích hợp; truy vấn thực tế vẫn cần kết nối Internet outbound và có thể bị giới hạn bởi nhà cung cấp.

Có thể kiểm tra database qua:

`http://127.0.0.1:8080/api/portfolio/status`

Chỉ coi DB sẵn sàng nếu response xác nhận kết nối MySQL và database `PortfolioAI_DB`.

## 7. Sử dụng các chức năng MoteLona

1. Mở `index.html` qua Live Server.
2. Tới **Projects / Dự án tiêu biểu** và mở MoteLona assistant.
3. Hoặc mở trang chi tiết `Project/1/motelona.html` rồi chọn nút mở MoteLona.
4. Kiểm tra status backend trên khung chat.
5. Thử các yêu cầu như:
   - `Bạn có những kỹ năng nào?`
   - `Trang này có những dự án nào?`
   - `Bạn học ở trường nào?`
   - `TF-IDF là gì?`
   - `Cuộn đến phần kỹ năng.`
   - Bôi đen thuật ngữ trong nội dung rồi yêu cầu giải thích.
6. Kéo khung chat bằng vùng header; tránh bắt đầu kéo trên nút Settings hoặc Close.
7. Kéo grip ở góc dưới bên phải để thay đổi đồng thời chiều rộng và chiều cao; dùng phím mũi tên khi grip được focus (giữ Shift để tăng bước), double-click để khôi phục kích thước gần mặc định.
7. Nếu khung chat đang ở chế độ compact-height, vuốt/scroll ngang trên thanh công cụ hoặc nhóm câu hỏi gợi ý để xem các nút nằm ngoài mép phải.
8. Cuộn dọc bên trong vùng tin nhắn để đọc hội thoại dài; các nhóm nút ngang không phải vùng cuộn hội thoại.

### 7.1 Lịch sử và bộ nhớ

- Mở Settings trong khung chat để xem tùy chọn quyền riêng tư.
- Lịch sử và memory là hai lựa chọn riêng.
- Memory dài hạn cần người dùng đồng ý rõ ràng; backend kiểm tra `consent=true`.
- Có chức năng xem/xóa history, xem/xóa memory và export dữ liệu theo session.
- Không bật lưu trữ mặc định hoặc âm thầm thu thập dữ liệu nếu đang chỉnh sửa phần consent.

### 7.2 Bố cục dễ đọc ở browser zoom 100%

Khung chat có hai chế độ bố cục theo chiều cao CSS viewport:

- `max-height: 760px`: compact-height. Thanh công cụ, thanh tìm kiếm khi mở và câu hỏi gợi ý hiển thị thành các hàng ngang có thể cuộn; vùng hội thoại được ưu tiên thêm chiều cao.
- `min-height: 761px`: bố cục thường, các nhóm chip được phép xuống nhiều hàng như trước.
- `max-height: 500px`: compact-height tăng cường, giảm thêm khoảng cách để giữ header, nội dung, ô nhập và footer trong panel.

Breakpoint dựa trên chiều cao CSS viewport, không dựa trực tiếp vào con số zoom của Chrome. Để xác nhận compact mode, đặt Chrome ở 100% rồi kiểm tra chiều cao viewport khoảng 620px; kiểm tra khoảng 850px để xác nhận bố cục thường. Thay đổi browser zoom có thể làm thay đổi kích thước CSS viewport và trạng thái breakpoint.

## 8. Cấu hình khi triển khai website public

Frontend tĩnh và backend Java cần được triển khai riêng hoặc qua một hạ tầng có thể phục vụ cả hai. Bản ZIP hiện tại không chứa URL production đã xác minh.

Trước khi public:

1. Triển khai backend Spring Boot lên máy chủ/container có JDK phù hợp và cho phép outbound HTTPS tới DuckDuckGo nếu muốn dùng tìm web.
2. Triển khai MySQL, tạo database và tài khoản ứng dụng quyền tối thiểu.
3. Thiết lập biến môi trường server; không upload `.env` chứa secret vào public web root.
4. Cấu hình `MOTELONA_SERVER_ADDRESS`, `MOTELONA_PORT`, thông tin MySQL và `MOTELONA_CORS_ORIGINS`.
5. Bật HTTPS cho website và API.
6. Trong trang HTML, đặt URL backend HTTPS của bạn vào `window.MOTELONA_API_BASE` **trước** khi nạp `motelona-config.js`, ví dụ:

```html
<script>
  window.MOTELONA_API_BASE = "https://api.example.com";
</script>
<script src="./Project/1/motelona-ai/frontend/motelona-config.js"></script>
```

Thay URL ví dụ bằng domain thật của bạn. Không dùng `api.example.com` trong production.

7. CORS backend chỉ cho phép domain frontend thực tế.
8. Kiểm thử health, chat, DB, history/memory consent, contact form, rate limit, backup/restore và log.

Trang contact hiện dùng `assets/js/animfolio-contact-static.js`: nếu backend kết nối được, nó gửi message tới `/api/portfolio/contact`; đồng thời có thể gửi thông báo qua FormSubmit nếu `window.ANIMFOLIO_CONTACT_EMAIL` được cấu hình hợp lệ trong `index.html`. Đây là tích hợp dịch vụ bên ngoài riêng cho email contact, không phải API AI.

## 9. Kiểm thử

### 9.1 Unit tests

Trong PowerShell tại backend:

```powershell
.\mvnw.cmd clean test
```

### 9.2 Kiểm tra tích hợp

Cần có JDK 21, MySQL chạy được, database đã tạo và `.env` hợp lệ:

```powershell
.\VERIFY_MOTELONA.ps1
```

Script sẽ build, khởi chạy backend tạm thời, kiểm tra health, kết nối MySQL, Portfolio public API, local search, memory create/list/delete/session isolation, chat và explain; sau cùng dừng process do script khởi chạy.

Không chạy/không vượt qua script tích hợp không có nghĩa website tĩnh không thể mở; nó có nghĩa chưa xác minh backend end-to-end.

## 10. Xử lý lỗi thường gặp

| Lỗi/hiện tượng | Nguyên nhân có thể | Hướng xử lý |
|---|---|---|
| `java` không được nhận diện | Thiếu JDK hoặc PATH chưa đúng | Cài JDK 21, mở PowerShell mới, chạy `java -version`. |
| `MYSQL_PASSWORD is empty` | Chưa tạo `.env` hoặc tên biến sai | Kiểm tra `.env` trong đúng thư mục backend. |
| `Communications link failure` | MySQL chưa chạy/sai host/port | Kiểm tra MySQL Server, host 127.0.0.1, port 3306. |
| `Access denied for user` | Sai tài khoản/mật khẩu/privileges | Kiểm tra username, password và grant trên `PortfolioAI_DB`. |
| Chat báo backend offline | Backend chưa chạy, API base sai hoặc CORS | Mở health URL; kiểm tra config và origin frontend. |
| CORS error | Origin hiện tại chưa nằm trong allow-list | Thêm origin scheme + hostname + port chính xác vào `.env`, restart backend. |
| Website không tải CSS/ảnh/JS | Mở sai thư mục gốc hoặc sai relative path | Mở `Portfolio_Web` bằng Live Server, không đổi vị trí thư mục. |
| Trang còn CSS cũ | Browser cache | `Ctrl` + `Shift` + `R`. |
| Chat kéo không đúng sau chỉnh zoom | CSS scale/position đã thay đổi | Kiểm tra `getVisualScale()` trong `motelona.js` và CSS panel; test lại mọi viewport. |
| Chat resize không đúng sau chỉnh zoom | Scale, min/max size hoặc vị trí panel thay đổi | Kiểm tra `enablePanelResizing()`, `getScale()`, `.motelona-ai-resize-handle`; thử kéo góc ở desktop/mobile và khi panel đã di chuyển. |
| Không thấy compact chat layout | Chiều cao CSS viewport đang lớn hơn 760px | Kiểm tra `@media (max-height: 760px)`; thử Chrome 100% với viewport khoảng 620px. |
| Không nhìn thấy hết tool chips/câu hỏi mẫu | Các nhóm ở compact mode được thiết kế cuộn ngang | Vuốt/scroll ngang trên chính hàng nút; không phải nút bị xóa. |
| Tìm Internet không có kết quả | Server mất Internet, trang HTML thay đổi hoặc nhà cung cấp giới hạn yêu cầu | Kiểm tra outbound HTTPS từ server, thử câu ngắn hơn, sau đó mở `DuckDuckGo` trực tiếp. Tìm web không dùng API key nhưng phụ thuộc trang công khai của nhà cung cấp. |

## 11. Khi muốn chỉnh sửa

- Bắt đầu từ `00_AI_MASTER_GUIDE.md`.
- Tra bảng chức năng trong `05_BAN_DO_PHU_THUOC.md` để tìm file cần sửa và các phụ thuộc.
- Tra `01_SO_DO_FILE.md` để kiểm tra đường dẫn đầy đủ.
- Sau khi sửa, chạy `07_CHECKLIST_KIEM_THU.md`.
- Nếu thêm/xóa/đổi file hoặc thay đổi API/database, cập nhật sơ đồ và bản đồ phụ thuộc.
