# 00 — AI MASTER GUIDE: HƯỚNG DẪN ĐỌC VÀ CHỈNH SỬA PORTFOLIO + MOTELONA

> **Vai trò:** Tài liệu điều phối trung tâm dành cho AI/công cụ lập trình khi tiếp nhận dự án.  
> **Phiên bản tài liệu:** 2026-11-18
> **Đường dẫn gốc dự án:** `Portfolio_Web/`  
> **Ngôn ngữ giải thích:** Tiếng Việt; tên class, method, endpoint và thuật ngữ kỹ thuật giữ nguyên theo mã nguồn.

---

## 1. AI phải đọc tài liệu này như thế nào?

Khi được giao chỉnh sửa dự án, hãy đọc theo thứ tự:

1. `main_document/00_AI_MASTER_GUIDE.md` — hiểu mục tiêu, phạm vi, quy tắc và các giới hạn hiện tại.
2. `main_document/01_SO_DO_FILE.md` — tìm đúng file và đường dẫn.
3. `main_document/05_BAN_DO_PHU_THUOC.md` — xác định những file liên quan có thể bị ảnh hưởng khi sửa một chức năng.
4. `main_document/03_GIAI_THICH_SO_DO_FILE.md` — hiểu kiến trúc, luồng dữ liệu, Java, NLP và database.
5. `main_document/02_HUONG_DAN_CAI_DAT_SU_DUNG.md` — hiểu cách chạy và kiểm thử dự án.
6. `main_document/04_UPGRADE_NOTES.md` — biết lịch sử thay đổi; đây là nhật ký, không phải mô tả thay thế mã nguồn hiện tại.
7. `main_document/06_PROMPT_CHINH_SUA_MAU.md` và `main_document/07_CHECKLIST_KIEM_THU.md` — dùng khi thực hiện và nghiệm thu thay đổi.
8. `main_document/08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md` — dùng khi cần học, lần theo và thuyết trình kiến trúc Java/Spring Boot/MySQL của MoteLona.
9. `main_document/09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md` — cài MySQL trên Windows từ trang tải chính thức, tạo database/user và kết nối vào backend.

Sau đó phải kiểm tra trực tiếp HTML/CSS/JavaScript/Java/SQL có liên quan. **Không được chỉ dựa vào tài liệu Markdown để sửa mã.** Tài liệu giúp định hướng; mã nguồn hiện hành mới là căn cứ cuối cùng.

## 2. Thứ tự ưu tiên khi có thông tin mâu thuẫn

Nếu có khác biệt giữa các nguồn, áp dụng thứ tự:

1. Mã nguồn đang có trong ZIP được giao ở lượt hiện tại.
2. Các file cấu hình và schema thực tế (`pom.xml`, `application.yml`, `.env.example`, SQL, HTML/CSS/JS).
3. Tài liệu trong `main_document/`.
4. Nội dung trao đổi cũ hoặc mô tả của các phiên bản trước.

Không tự lấy thông tin từ một ZIP cũ, bản triển khai khác hoặc câu trả lời trước để ghi đè mã nguồn mới.

## 3. Bản đồ tổng quan dự án

Đây là dự án Portfolio cá nhân một trang, tích hợp trợ lý website MoteLona. Dự án có ba phần chính:

| Phần | Công nghệ | Điểm vào chính | Vai trò |
|---|---|---|---|
| Portfolio | HTML, CSS, JavaScript | `index.html` | Trang hồ sơ cá nhân một trang, điều hướng section, song ngữ, giao diện chủ đề và form liên hệ. |
| Trang chi tiết MoteLona | HTML, CSS, JavaScript | `Project/1/motelona.html` | Giải thích dự án AI, kiến trúc, chức năng, từ điển thuật ngữ, database, checklist và roadmap. |
| MoteLona runtime | Java 21, Spring Boot, MySQL 8, JavaScript | `Project/1/motelona-ai/` | Chatbot, hỏi đáp theo dữ liệu Portfolio, tìm kiếm văn bản trên trang, giải thích thuật ngữ, điều hướng được cho phép, lịch sử và bộ nhớ có đồng ý. |

### Những điều không được hiểu nhầm

- Backend hiện tại là **Java NLP tự xây**, không phải ứng dụng gọi OpenAI/Gemini/ChatGPT.
- Không có API key AI bên thứ ba cần cấu hình trong bản này.
- Hệ thống đang dùng nhận diện ý định, xử lý văn bản, TF-IDF, cosine similarity, truy xuất tri thức, bộ từ điển và luật phản hồi.
- Tìm trong website là tìm trên ngữ cảnh được frontend gửi lên; đây là chức năng riêng với tìm Internet.
- `SearchProvider` dùng `DuckDuckGoHtmlSearchProvider`, đọc kết quả từ trang HTML công khai, không dùng AI API/search API key. Server phải có outbound HTTPS; định dạng HTML có thể thay đổi hoặc bị giới hạn.
- Tìm Internet chỉ chạy khi người xem bật nút Search web hoặc nêu yêu cầu tìm web rõ ràng. Query được gửi tới DuckDuckGo; UI trả tiêu đề, trích đoạn và link nguồn để người xem tự kiểm chứng.
- Khi không đủ căn cứ, Java engine phải nói rõ giới hạn, không đoán. Tìm kiếm web hiện chỉ truy xuất kết quả và trích đoạn; không có LLM để tổng hợp hoặc xác minh toàn bộ trang ngoài.
- Điều hướng nhận các section/page ID cố định. Có thể chuyển giữa portfolio và trang chi tiết MoteLona; cụm phù hợp được cuộn tới và tô vàng tạm thời khi tìm thấy chính xác trong DOM.
- Chức năng điều hướng chỉ được thực hiện với các section có trong allow-list; không cho AI thực thi mã JavaScript hoặc URL tùy ý.
- Portfolio có thể hiển thị nội dung tĩnh khi backend/MySQL không chạy. Những tính năng cần server (chat, dữ liệu DB, lưu lịch sử/bộ nhớ, gửi form qua backend) cần backend tương ứng.

## 4. Các trang và cấu trúc nội dung

### 4.1 `index.html` — Portfolio chính

Đây là trang một trang (one-page). Các section chính hiện tại:

| ID section | Nội dung |
|---|---|
| `#hero` | Trang chủ/giới thiệu ngắn và điểm nhấn đầu trang. |
| `#about` | Giới thiệu bản thân. |
| `#education` | Học vấn. |
| `#skills` | Kỹ năng. |
| `#projects` | Dự án tiêu biểu, trong đó có MoteLona. |
| `#contact` | Form liên hệ và email. |
| `#stats` | Được một số logic AI cho phép nhận diện nếu có trên trang; hãy kiểm tra HTML trước khi thêm hoặc sửa nội dung. |

Navigation dùng các anchor như `#hero`, `#about`, `#education`, `#skills`, `#projects`, `#contact`. Nếu đổi ID section, phải đồng bộ navigation, JS điều hướng, bản dịch, scroll logic và các liên kết nội bộ.

Trang chủ hiện mặc định nội dung tiếng Anh (`<html lang="en">`) và có công tắc EN/VI. Khi người dùng yêu cầu song ngữ, dịch đầy đủ nội dung giao diện và các trường dữ liệu được duyệt sang cả hai ngôn ngữ; giữ nguyên sự thật, tên riêng, email, tên công nghệ và trạng thái dự án. Không tự bịa hoặc sửa dữ liệu cá nhân. Nếu trường dịch tiếng Việt tùy chọn trong MySQL còn trống, giao diện giữ nội dung gốc cho tới khi có bản dịch được cung cấp/duyệt.

### 4.2 `Project/1/motelona.html` — Trang giải thích dự án

Đây là trang tài liệu/giới thiệu kỹ thuật, khác với cửa sổ chat. Các section chính hiện có:

- `#overview`
- `#features`
- `#architecture`
- `#knowledge`
- `#database`
- `#quality`
- `#scope`
- `#roadmap`

Trang này có EN/VI, nút sáng/tối, bảng màu, glossary tìm kiếm, checklist có lưu trạng thái trong trình duyệt, liên kết về Portfolio và nút mở trợ lý. Không được nhầm các section này với section ID của `index.html`.

### 4.3 Khung chat MoteLona dùng chung

Giao diện chat được dựng từ `Project/1/motelona-ai/frontend/motelona.js`, được gắn vào phần tử mount có thuộc tính `data-motelona-ai-root`. Hai trang chính và trang chi tiết có điểm mount tương ứng.

Các phần giao diện gồm launcher, panel, header, trạng thái backend, thanh công cụ, tin nhắn, gợi ý, ô nhập, popover giải thích và khu vực cài đặt quyền riêng tư. Không xóa hoặc đổi tên các thuộc tính `data-*` mà JavaScript đang truy vấn nếu chưa cập nhật toàn bộ phần phụ thuộc.

## 5. Các hành vi giao diện cần bảo toàn

### 5.1 Ngôn ngữ và giao diện

- Portfolio hỗ trợ tiếng Anh và tiếng Việt.
- Trang chi tiết MoteLona có công tắc EN/VI riêng.
- Có giao diện sáng/tối và các bảng màu đang được triển khai trên hai trang.
- Bảng màu hiện có gồm các lựa chọn như Green Canopy, Peach Blossom, Golden Apricot và Rainbow Spectrum; kiểm tra `data-theme-choice`/`data-portfolio-theme` trong HTML trước khi thay đổi tên hoặc key.
- Tôn trọng `prefers-reduced-motion`, focus bằng bàn phím và các thuộc tính accessibility hiện hữu.
- Nội dung văn bản được chèn từ dữ liệu phải dùng `textContent` hoặc cơ chế an toàn; không đưa dữ liệu không tin cậy vào `innerHTML`.

### 5.2 Quy tắc tỷ lệ hiển thị Portfolio và trang chi tiết MoteLona

- Các section Portfolio ngoài Home dùng `zoom: 0.75` (75%) trên desktop thông qua `assets/css/portfolio-visual-scale.css`. Đây là quy tắc riêng của `index.html`; không tự ý thay đổi khi yêu cầu chỉ nói về MoteLona.
- Trang chi tiết `Project/1/motelona.html` không bị ép CSS zoom. Trang dùng tỷ lệ tự nhiên theo browser zoom 100%, chiều rộng tối đa theo nội dung và responsive reflow trên tablet/mobile. Mục tiêu là chữ, thẻ nội dung, thanh điều hướng và khoảng cách dễ đọc ở kích thước bình thường.
- Bố cục và giao diện trang chi tiết được quản lý chính bởi `assets/css/motelona-premium.css`; `assets/css/typography-refinement.css` là lớp typography dùng chung được nạp cuối trên cả `index.html` và `motelona.html`. Các file `motelona-detail.css`, `motelona-responsive.css`, `motelona-ultimate.css`, `motelona-refined.css` và `motelona-detail-zoom.css` còn trong cây file cũ nhưng hiện không được `motelona.html` nạp; không thêm chúng trở lại nếu không có lý do tương thích được kiểm thử.
- `assets/js/homepage-zoom.js` là cơ chế riêng cho nhóm điều hướng/Home/launcher trên `index.html`, dựa trên `devicePixelRatio`; không chỉnh file này khi làm giao diện trang chi tiết MoteLona.

Không dùng `transform: scale()` hoặc CSS `zoom` để thu nhỏ toàn trang chi tiết. Hãy điều chỉnh typography, max-width, grid và breakpoint responsive để đạt mật độ nội dung mong muốn mà không làm sai sticky navigation, anchor scroll, vùng click hoặc vị trí khung chat.

### 5.3 Kéo thả khung chat MoteLona

- Logic kéo thả nằm trong `Project/1/motelona-ai/frontend/motelona.js`, hàm `enablePanelDragging()`.
- Vùng kéo là `.motelona-ai-header`.
- Dùng Pointer Events (`pointerdown`, `pointermove`, `pointerup`, `pointercancel`, `lostpointercapture`) để hỗ trợ chuột, bút và cảm ứng.
- Nhấn vào `button`, `a`, `input`, `textarea`, `select` hoặc phần tử `contenteditable` trong header không bắt đầu kéo; các nút Settings/Close phải hoạt động bình thường.
- Tọa độ được điều chỉnh theo tỷ lệ hiển thị của panel và bị giới hạn trong viewport.
- CSS liên quan nằm trong `Project/1/motelona-ai/frontend/motelona.css`, đặc biệt `.motelona-ai-header`, `.motelona-ai-panel.is-dragging` và các media query responsive.

### 5.3.1 Thay đổi kích thước khung chat

- Góc dưới bên phải của khung có nút kéo `[data-motelona-resize]` với selector `.motelona-ai-resize-handle`. Kéo góc này để thay đổi đồng thời chiều rộng và chiều cao như cửa sổ desktop.
- Logic nằm trong `enablePanelResizing()` ở `frontend/motelona.js`; dùng Pointer Events cho chuột, pen và touch, giới hạn kích thước theo vùng viewport còn nhìn thấy.
- Có thể focus nút resize và dùng phím mũi tên; giữ `Shift` để thay đổi theo bước lớn hơn. Double-click grip khôi phục kích thước gần mặc định.
- Không kéo nhầm từ composer hoặc header; phải giữ kéo di chuyển panel bằng header độc lập với resize. Settings/Close và nhập/gửi tin nhắn luôn hoạt động.
- CSS grip, trạng thái `is-resizing` và khoảng trống footer nằm trong `frontend/motelona.css`. Khi chỉnh panel/media query/CSS zoom, cần kiểm tra cả vị trí đã kéo và kích thước đã resize khi viewport thay đổi.
### 5.4 Bố cục compact-height của khung chat (bản hiện hành)

Mục tiêu của thay đổi mới nhất là giúp khung chat dễ đọc ở Chrome zoom 100% khi cửa sổ trình duyệt/viewport có chiều cao thấp: giảm phần không gian bị các hàng nút chiếm dụng và dành thêm chiều cao cho hội thoại. Đây là **responsive reflow**, không phải thu nhỏ toàn bộ panel bằng `zoom` hoặc `transform: scale()`.

- Điểm kích hoạt chính xác: `@media screen and (max-height: 760px)` trong `Project/1/motelona-ai/frontend/motelona.css`. `760px` là **chiều cao CSS viewport**, không phải phần trăm browser zoom và không phải chiều cao vật lý của màn hình.
- Vì vậy, Chrome ở 100% chỉ bật compact mode khi viewport CSS thực tế cao không quá 760px. Ở cùng một cửa sổ vật lý, đổi Chrome sang 75% có thể làm CSS viewport cao hơn và khiến media query không còn khớp. Không được viết tài liệu hoặc logic theo kiểu “100% luôn bật compact mode”.
- Trong compact mode, `.motelona-ai-toolsbar` chuyển sang một hàng ngang (`flex-wrap: nowrap`) và cuộn ngang (`overflow-x: auto`); các chip không bị xóa hoặc ẩn.
- `.motelona-ai-suggestions` cũng thành một hàng ngang cuộn được. Người dùng có thể vuốt/scroll ngang để truy cập toàn bộ câu hỏi gợi ý.
- Khi bật tìm kiếm trong trang, `.motelona-ai-search` cũng dùng hàng ngang cuộn được để input và các nút Search/Close không tạo thêm nhiều hàng dọc. Khu vực kết quả tìm kiếm vẫn có vùng cuộn riêng và giới hạn chiều cao.
- `.motelona-ai-messages` là vùng nội dung hội thoại chính, nhận phần chiều cao còn lại và cuộn dọc độc lập. Tin nhắn dài phải wrap, không tràn khỏi panel.
- Header, trạng thái backend, composer (ô nhập/nút gửi) và footer được thu gọn khoảng cách/kích thước ở mức vừa phải nhưng vẫn giữ khả năng thao tác. Composer/footer không được bị suggestions đè lên.
- Với viewport rất thấp (`max-height: 500px`), có thêm một lớp thu gọn khoảng cách; không xóa tool chips, suggestions, composer hoặc footer.
- Khi viewport CSS cao hơn 760px, bố cục nhiều hàng thông thường được giữ lại. Đây là trạng thái bình thường, không phải lỗi nếu các chip không cuộn ngang ở chiều cao này.
- Các thay đổi compact-height nằm trong CSS; không sửa logic chat/API/Java chỉ để đạt hiệu ứng này. Chúng cũng không thay đổi cơ chế kéo thả.

Khi sửa kích thước panel, vị trí fixed, CSS zoom, header hoặc compact-height layout, phải kiểm thử browser zoom 100% với ít nhất hai chiều cao CSS viewport: khoảng 620px (compact) và 850px (normal). Nên kiểm tra thêm sát breakpoint 760px/761px. Thử cả khi thanh tìm kiếm trang được mở. Kiểm tra lại kéo thả bằng chuột/cảm ứng, Settings/Close, tin nhắn dài, nút gửi và footer. Không xóa phần hiệu chỉnh scale trong `getVisualScale()` nếu chưa có phương án thay thế được kiểm thử.

## 6. Kiến trúc MoteLona hiện tại

### 6.1 Frontend

| File | Trách nhiệm chính |
|---|---|
| `frontend/motelona.js` | Tạo khung chat; bản dịch chatbot; gửi câu hỏi; lấy page context; gọi API; mở/đóng panel; kéo di chuyển và resize panel; điều hướng; giải thích đoạn được chọn; tìm nội dung trang; quyền lưu lịch sử/bộ nhớ; export/delete dữ liệu. |
| `frontend/motelona.css` | Launcher, panel, header, messages, form, trạng thái, popover, consent UI, responsive, compact-height layout và giao diện chat MoteLona. |
| `frontend/motelona-config.js` | Chọn URL backend. Localhost/file dùng `http://127.0.0.1:8080`; website public không tự gọi loopback của khách truy cập. |

`motelona-config.js` chỉ chứa URL public của backend, không chứa mật khẩu database hay bí mật.

### 6.2 Backend Java

Package gốc:

`Project/1/motelona-ai/backend/src/main/java/com/motelona/assistant/`

| Package | Trách nhiệm |
|---|---|
| `ai/` | Nhận diện ý định, vector hóa văn bản, truy xuất nội dung và tạo phản hồi theo luật. |
| `config/` | CORS, HTTP security headers và xử lý lỗi API. |
| `controller/` | REST endpoints; nhận DTO, gọi service, trả JSON. |
| `dto/` | Cấu trúc request/response. Thay đổi DTO phải đồng bộ frontend JSON. |
| `memory/` | Logic lưu/đọc/xóa bộ nhớ dài hạn theo session. |
| `search/` | Interface, DuckDuckGo HTML provider và lỗi dịch vụ tìm kiếm. |
| `service/` | Nghiệp vụ chat, dữ liệu Portfolio, glossary, local search, action routing, rate limit. |

Backend chạy theo cấu hình hiện tại tại `127.0.0.1:8080`, dùng Java 21, Spring Boot theo phiên bản khai báo trong `pom.xml` và MySQL 8 qua JDBC.

### 6.3 Luồng xử lý câu hỏi

```text
Người dùng nhập câu hỏi / chọn đoạn văn bản
                    │
                    ▼
       frontend/motelona.js
       - lấy session và language
       - thu thập pageContext từ section hiện tại
       - chọn request chat hoặc explain
                    │ HTTP JSON
                    ▼
          AiController / DTO
                    │
                    ▼
              AiChatService
        ┌───────────┼──────────────┐
        ▼           ▼              ▼
 IntentClassifier  LocalSearch   GlossaryService
        │           │              │
        ▼           ▼              ▼
  LocalAiEngine / KnowledgeRetrievalService
                    │
            ActionRouter (nếu điều hướng)
                    │
           JdbcTemplate / services
                    │
                    ▼
              MySQL 8 (nếu cần)
                    │
                    ▼
              JSON response
                    │
                    ▼
     Hiển thị câu trả lời / action / trạng thái
```

Luồng thực tế phải xác minh ở từng service khi thay đổi. Sơ đồ trên mô tả các lớp chính, không có nghĩa mọi câu hỏi đều truy vấn tất cả các service hoặc database.

## 7. REST API hiện có

Đường dẫn controller gốc và mapping trong source hiện tại:

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/ai/health` | Kiểm tra backend/engine và trạng thái Internet search. |
| POST | `/api/ai/chat` | Hỏi đáp Portfolio. |
| POST | `/api/ai/explain` | Giải thích văn bản/thuật ngữ được chọn. |
| POST | `/api/ai/search` | Tìm trong ngữ cảnh nội dung website được gửi kèm. |
| GET | `/api/ai/search/web?query=...` | Tìm web qua DuckDuckGo HTML; tối đa 200 ký tự; giới hạn tần suất và cần Internet trên server. |
| GET | `/api/ai/memories?sessionId=...` | Đọc bộ nhớ theo session. |
| POST | `/api/ai/memories` | Lưu bộ nhớ; backend yêu cầu `consent=true`. |
| DELETE | `/api/ai/memories/{id}?sessionId=...` | Xóa một memory theo session. |
| DELETE | `/api/ai/memories?sessionId=...` | Xóa memory của session. |
| GET | `/api/ai/history/{sessionId}` | Đọc lịch sử hội thoại. |
| DELETE | `/api/ai/history/{sessionId}` | Xóa lịch sử hội thoại. |
| GET | `/api/ai/export/{sessionId}` | Xuất history và memory của session hiện tại. |
| GET | `/api/portfolio/status` | Kiểm tra kết nối dữ liệu Portfolio. |
| GET | `/api/portfolio/public` | Đọc dữ liệu Portfolio công khai. |
| POST | `/api/portfolio/contact` | Lưu tin nhắn liên hệ vào MySQL. |

Khi thay đổi endpoint, phải đồng bộ ít nhất controller, DTO, frontend request, cấu hình CORS nếu có, test và tài liệu. Không tự đổi tên trường JSON dựa trên suy đoán.

## 8. MySQL và dữ liệu

- Database: `PortfolioAI_DB`.
- Script cài đặt gốc: `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql`.
- Schema backend: `Project/1/motelona-ai/backend/src/main/resources/schema.sql`.
- Backend đang bật SQL init theo cấu hình trong `application.yml`; vì vậy hai schema phải được giữ nhất quán khi thêm/đổi bảng.
- `Project/1/motelona-ai/database/PortfolioAI_DB.sql` là file ghi chú về bản SQL Server cũ đã ngừng sử dụng, **không phải script tạo database**.
- Schema MySQL hiện có 17 bảng: `AdminAccount`, `Profile`, `Education`, `Experience`, `SkillCategory`, `Skill`, `Project`, `Technology`, `ProjectTechnology`, `ContactMessage`, `AIConversation`, `AIMessage`, `AIKnowledgeItem`, `SiteSetting`, `conversation_messages`, `ai_api_daily_usage`, `user_memories`.
- Hồ sơ học vấn, kỹ năng và dự án hỗ trợ trường tiếng Việt tùy chọn (`*Vi`). Khi cập nhật database cũ, sao lưu rồi chạy `Project/1/motelona-ai/database/V002_BilingualContent_MySQL.sql` trước khi chạy backend mới; `schema.sql` không tự nâng cấp bảng đã tồn tại.

Các quan hệ chính:

- `SkillCategory` 1–N `Skill`.
- `Project` N–N `Technology` qua `ProjectTechnology`.
- `AIConversation` 1–N `AIMessage`.
- `ContactMessage` có thể tham chiếu `AdminAccount` ở trường người phản hồi.
- `conversation_messages` và `user_memories` lưu dữ liệu theo session ở cấu trúc riêng phục vụ chức năng hiện tại.

Không tự xóa bảng trông có vẻ không dùng. Trước khi thay schema, cần tìm toàn bộ câu SQL/JDBC đang tham chiếu và thêm phương án tương thích dữ liệu cũ.

## 9. Quy tắc bắt buộc khi chỉnh sửa

### 9.1 Giữ nguyên trừ khi có yêu cầu cụ thể

- Không đổi tên, chuyển thư mục hoặc gộp file chỉ để làm code “gọn hơn”.
- Không viết lại toàn bộ `index.html`, `motelona.js`, CSS lớn hoặc backend nếu nhiệm vụ chỉ yêu cầu thay một tính năng nhỏ.
- Không xóa chức năng đang hoạt động, giao diện EN/VI, sáng/tối, bảng màu, responsive, điều hướng, contact form, lịch sử/bộ nhớ, thao tác giải thích thuật ngữ hoặc kéo thả khung chat.
- Không thay đổi trang chủ `#hero` khi yêu cầu chỉ nói tới các section khác.
- Giữ tỷ lệ 75% (`zoom: 0.75`) của các section Portfolio ngoài Home; trang chi tiết MoteLona dùng kích thước tự nhiên, không áp dụng CSS zoom.
- Không tự đổi kiến trúc Java NLP thành dịch vụ API/LLM.
- Không gọi Internet search ngầm. Chỉ truy vấn khi người xem chủ động yêu cầu hoặc bật Search web; phải hiện rõ nguồn và xử lý lỗi provider.

### 9.2 Quy trình sửa an toàn

1. Xác định yêu cầu thành các tiêu chí có thể kiểm tra được.
2. Đọc file gốc và toàn bộ file phụ thuộc liên quan.
3. Ghi rõ file dự kiến thay đổi và lý do.
4. Sửa tối thiểu; giữ class, ID, `data-*`, endpoint và schema đang được nơi khác sử dụng.
5. Tìm lại tên class/ID/method/endpoint liên quan trong toàn dự án để tránh tham chiếu bị đứt.
6. Chạy kiểm tra cú pháp phù hợp.
7. Kiểm tra đường dẫn file, HTML ID, request/response DTO, SQL schema và responsive.
8. Nếu không thể chạy browser/MySQL/backend thật, ghi rõ phần nào mới chỉ kiểm tra tĩnh; không tuyên bố đã kiểm thử end-to-end.
9. Cập nhật `04_UPGRADE_NOTES.md`, `01_SO_DO_FILE.md`, `05_BAN_DO_PHU_THUOC.md` nếu thay đổi có ảnh hưởng.
10. Đóng gói lại toàn bộ thư mục `Portfolio_Web/`, giữ nguyên cấu trúc và chỉ tạo một ZIP hoàn chỉnh.
11. Cập nhật `README.md` và `.github/copilot-instructions.md` nếu cách chạy, giới hạn AI hoặc quy tắc repo thay đổi.

### 9.3 An toàn và dữ liệu

- Không ghi mật khẩu MySQL hoặc secret vào HTML, CSS, JavaScript hay `.env.example`.
- Chỉ lưu secret trên server trong `.env` hoặc biến môi trường thật.
- Không ghi đè `.env` của người dùng bằng file mẫu.
- Giữ CORS theo allow-list origin cụ thể; không đổi thành `*` để “chạy cho được”.
- Giữ consent trước khi lưu memory dài hạn.
- Giữ rate limit và allow-list điều hướng; không biến text từ người dùng thành JavaScript/HTML thực thi.
- Không tuyên bố có tìm kiếm Internet/LLM/AI tự học khi code chưa có khả năng đó.

## 10. Những bài kiểm tra tối thiểu cần xem xét

- JavaScript: chạy `node --check` cho các file `.js` nếu Node.js có sẵn.
- Java: chạy `mvnw.cmd clean test` trong thư mục backend.
- Tích hợp: chỉ chạy `VERIFY_MOTELONA.ps1` khi đã có JDK, MySQL, `.env` và backend có thể khởi động.
- HTML: không có ID trùng; liên kết asset nội bộ còn tồn tại.
- CSS: selector mới không làm vỡ desktop/mobile, dark/light, màu chủ đề, reduced-motion.
- Chat: health, gửi câu hỏi, local search, explain, navigation, history/memory consent, delete/export và drag panel.
- Giao diện: desktop từ 1025px; tablet và mobile; browser zoom 100%; trang Home và các section sau Home; trang chi tiết MoteLona ở tỷ lệ tự nhiên và responsive.
- Không khẳng định test nào đã pass nếu chưa chạy test đó trong môi trường thực tế.

## 11. Đầu ra AI phải giao sau khi chỉnh sửa

Khi trả dự án đã chỉnh, AI phải cung cấp:

1. Một ZIP đầy đủ có cấu trúc thư mục nguyên vẹn.
2. Danh sách file đã sửa/thêm/xóa.
3. Mô tả ngắn thay đổi và lý do.
4. Những tính năng cũ đã được giữ lại và các kiểm tra đã chạy.
5. Các mục chưa thể kiểm tra (ví dụ cần MySQL thật, browser thật, domain/HTTPS hoặc thông tin deployment).
6. Cập nhật tài liệu trong `main_document/` nếu kiến trúc, file tree, endpoint, database hoặc hành vi người dùng thay đổi.

---

## 12. Bản đồ tài liệu trong `main_document/`

| File | Dùng khi |
|---|---|
| `00_AI_MASTER_GUIDE.md` | AI mới tiếp nhận dự án; cần biết toàn bộ quy tắc và trạng thái hiện tại. |
| `01_SO_DO_FILE.md` | Tìm file, thư mục hoặc kiểm tra cấu trúc ZIP. |
| `02_HUONG_DAN_CAI_DAT_SU_DUNG.md` | Cài đặt, chạy Portfolio, backend Java và MySQL. |
| `03_GIAI_THICH_SO_DO_FILE.md` | Cần hiểu sâu chức năng từng lớp và công nghệ. |
| `04_UPGRADE_NOTES.md` | Xem lịch sử thay đổi theo ngày. |
| `05_BAN_DO_PHU_THUOC.md` | Chỉnh sửa tính năng và đánh giá tác động lan truyền. |
| `06_PROMPT_CHINH_SUA_MAU.md` | Cần prompt mẫu để giao việc cho AI khác. |
| `07_CHECKLIST_KIEM_THU.md` | Kiểm tra sau khi sửa và trước khi đóng gói. |
| `08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md` | Học kiến thức Java/Spring Boot/MySQL qua code và chuẩn bị bảo vệ đồ án. |
| `09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md` | Tải, cài MySQL/Workbench, tạo database và cấu hình tài khoản kết nối cho MoteLona. |
