# 03 — GIẢI THÍCH KIẾN TRÚC VÀ VAI TRÒ TỪNG NHÓM FILE

Tài liệu này bổ sung cho `00_AI_MASTER_GUIDE.md`. Nó giải thích vì sao các nhóm file tồn tại, dữ liệu đi qua đâu và cần lưu ý gì khi sửa.

## 1. Bức tranh tổng thể

Dự án gồm một frontend Portfolio, một trang chi tiết giải thích MoteLona và một ứng dụng chatbot Java riêng:

```text
Portfolio_Web/
├── index.html                         ← Portfolio one-page
├── Project/1/motelona.html            ← Trang giải thích dự án MoteLona
├── assets/                            ← CSS, JavaScript và hình ảnh dùng chung
├── Project/1/motelona-ai/frontend/    ← UI chatbot chạy trên browser
├── Project/1/motelona-ai/backend/     ← Java 21 + Spring Boot REST API
├── Project/1/motelona-ai/database/    ← Script MySQL
└── main_document/                     ← Tài liệu hướng dẫn AI/người phát triển
```

Frontend được phục vụ như trang tĩnh; backend chạy như ứng dụng Java độc lập. Trình duyệt gọi backend bằng HTTP JSON. Backend truy xuất MySQL khi nghiệp vụ cần dữ liệu hoặc lưu trữ.

## 2. Hai trang HTML

### 2.1 `index.html`

Trang chính theo dạng one-page. Phần lớn nội dung được viết trực tiếp trong HTML làm bản dự phòng tĩnh. Các section quan trọng có ID `hero`, `about`, `education`, `skills`, `projects`, `contact`; một số logic cho phép cả `stats` nếu ID đó hiện hữu.

`assets/js/portfolio-data.js` thử gọi `/api/portfolio/status`. Nếu database sẵn sàng, script gọi `/api/portfolio/public` rồi bổ sung/cập nhật dữ liệu profile, education, skills và projects. Nếu API lỗi, trang giữ phần HTML tĩnh; đây là fallback có chủ ý.

Không được thay toàn bộ trang bằng nội dung lấy từ API theo cách làm mất fallback nếu người dùng không yêu cầu kiến trúc mới.

### 2.2 `Project/1/motelona.html`

Trang giới thiệu kỹ thuật về MoteLona, không phải frontend của chatbot. Trang có các phần Overview, Features, Architecture, Knowledge, Database, Quality, Scope và Roadmap; nội dung có EN/VI, từ điển tìm kiếm, checklist và theme control.

Các script `motelona-detail.js` và `motelona-experience.js` điều khiển chủ đề, ngôn ngữ, glossary, scroll indicator và checklist. Chat assistant được nạp chung qua `motelona-config.js` và `motelona.js`.

## 3. Cấu trúc `assets/`

### 3.1 CSS của Portfolio

| File | Vai trò |
|---|---|
| `animfolio-base.css` | Nền tảng layout, reset có phạm vi, typography, section, card, grid và thành phần dùng chung. |
| `animfolio-animations.css` | Animation và trạng thái xuất hiện. |
| `animfolio-nav.css` | Navigation desktop/mobile, menu và nút điều khiển header. |
| `animfolio-gallery.css` | Gallery/card media. |
| `animfolio-print.css` | Quy tắc khi in. |
| `format.css` | Định dạng bố cục Portfolio hiện tại. |
| `animfolio-contact.css` | Form và layout khu vực liên hệ. |
| `portfolio-theme.css` | CSS token và hệ thống theme/palette. |
| `responsive-overrides.css` | Override responsive cho nhiều viewport; cần đọc cùng CSS được nạp sau. |
| `homepage-polish.css` | Hoàn thiện bố cục/trang trí Hero, nền, quỹ đạo, nhãn và phần liên quan trang đầu. |
| `portfolio-ultimate.css` | Lớp nâng cấp style bổ sung của Portfolio. |
| `portfolio-refined.css` | Lớp tinh chỉnh giao diện Portfolio. |
| `portfolio-visual-scale.css` | Scale 75% cho section ngoài Hero trên desktop; không dùng để scale Home. |
| `typography-refinement.css` | Lớp tinh chỉnh typography dùng chung, được nạp cuối trên cả Portfolio và trang chi tiết MoteLona. |

### 3.2 CSS của trang chi tiết MoteLona

| File | Vai trò |
|---|---|
| `motelona-premium.css` | Stylesheet chính được `motelona.html` nạp cho layout, màu sắc, theme palettes, desktop/tablet/mobile, reduced motion và print. |
| `typography-refinement.css` | Lớp typography dùng chung được nạp sau `motelona-premium.css` để đồng bộ kiểu chữ giữa hai trang. |
| `motelona-detail.css` | CSS nền tảng cũ, hiện không được trang chi tiết nạp. Giữ lại để tham khảo/tương thích lịch sử. |
| `motelona-responsive.css` | CSS responsive cũ, hiện không được trang chi tiết nạp. |
| `motelona-ultimate.css` | Lớp override cũ, hiện không được trang chi tiết nạp. |
| `motelona-refined.css` | Lớp tinh chỉnh cũ, hiện không được trang chi tiết nạp. |
| `motelona-detail-zoom.css` | CSS zoom 67% cũ, không còn được trang chi tiết nạp. Không khôi phục cho trang MoteLona. |

### 3.3 JavaScript của Portfolio

| File | Vai trò |
|---|---|
| `wp-shim.js` | Lớp tương thích nhỏ cho một số biến/hook kiểu WordPress. |
| `animfolio-engine.js` | Engine khởi tạo animation/section, tôn trọng reduced-motion. |
| `animfolio-casestudy.js` | Overlay/case-study interaction và keyboard focus. |
| `animfolio-lightbox.js` | Lightbox ảnh. |
| `format.js` | Hỗ trợ khởi tạo/định dạng của format trang hiện tại. |
| `homepage-zoom.js` | Điều chỉnh kích thước cảm nhận của điều hướng, Hero và launcher theo `devicePixelRatio` thông qua class/attribute CSS tương ứng. |
| `language-toggle.js` | Dịch nội dung Portfolio EN/VI, thuộc tính và placeholder; phát event đổi ngôn ngữ. |
| `portfolio-theme.js` | Bảng màu và hiệu ứng Rainbow Spectrum cho Portfolio. |
| `portfolio-data.js` | Lấy dữ liệu DB public và hydrate vào HTML; giữ HTML fallback nếu API offline. |
| `animfolio-contact-static.js` | Validate form; gửi contact tới backend khi có DB và gửi email qua FormSubmit khi được cấu hình. |
| `portfolio-experience.js` | Các tương tác bổ sung như tiến độ cuộn, nhãn section và đồng bộ trải nghiệm. |
| `motelona-detail.js` | Theme/palette và hiệu ứng màu của trang chi tiết MoteLona. |
| `motelona-experience.js` | Glossary, checklist, scroll progress, đổi ngôn ngữ phần trải nghiệm. |

### 3.4 JavaScript modules

Trong `assets/js/modules/`:

- `scroll-trigger.js`: kích hoạt animation theo vùng nhìn thấy.
- `typed-text.js`: hiệu ứng chữ gõ.
- `counter.js`: đếm số có animation.
- `timeline-draw.js`: hiệu ứng timeline.
- `stagger.js`: xuất hiện theo thứ tự cho nhóm phần tử.
- `parallax.js`: hiệu ứng parallax.
- `progress-bar.js`: tiến độ đọc/scroll.
- `smooth-scroll.js`: smooth scroll, offset cho sticky nav và điều hướng.
- `theme-toggle.js`: chuyển chế độ sáng/tối và cập nhật trạng thái điều khiển.

Một số module có thể không tác động trên mọi section nếu trang không có selector tương ứng. Không xóa chỉ vì một module không thấy hiệu ứng ở viewport đầu trang.

### 3.5 Hình ảnh

`assets/images/narrow-ai.jpg` là ảnh minh họa dự án AI/MoteLona dùng trong Portfolio và metadata trang chi tiết. Khi thay ảnh, kiểm tra đường dẫn tương đối trên cả `index.html` và `Project/1/motelona.html`.

## 4. Frontend MoteLona

Thư mục `Project/1/motelona-ai/frontend/`:

- `motelona.js`: file điều phối chính. Dựng UI và gắn event listener, lấy trang context từ các section, tạo session ID, gọi API, hiển thị message/status, điều hướng, giải thích selection, local search, consent, history/memory, xóa và export dữ liệu. `enablePanelDragging()` di chuyển cửa sổ bằng header; `enablePanelResizing()` thay đổi chiều rộng/chiều cao bằng grip góc dưới bên phải.
- `motelona.css`: giao diện launcher và panel; header; messages; input; suggestion chips; status; popover; consent; responsive, compact-height và skin chat mới.
- `motelona-config.js`: cấu hình địa chỉ API. Local development dùng `127.0.0.1:8080`; nếu website public thì phải khai báo URL HTTPS của backend trước khi load file này.

### 4.1 Cơ chế kéo thả

`enablePanelDragging()` tìm `.motelona-ai-header` làm vùng kéo. Khi pointerdown, mã bỏ qua các phần tử tương tác như button/link/input/textarea/select. Trong pointermove, nó tính lại tọa độ dựa trên tỷ lệ render panel và chặn panel ở trong viewport. Khi pointerup/cancel/lost capture, trạng thái kéo kết thúc.

Khi thay đổi style của header hoặc panel, giữ:

- `touch-action: none` ở drag handle; các button vẫn có `touch-action: manipulation`.
- `.is-dragging` để tắt transition khi đang kéo.
- `getVisualScale()` để bù zoom.
- logic giới hạn min/max X/Y và xử lý resize.

### 4.2 Thay đổi kích thước cửa sổ chat

`[data-motelona-resize]` là nút grip ở góc dưới bên phải. `enablePanelResizing()` dùng Pointer Events; kéo sang phải/trái để đổi chiều rộng, kéo lên/xuống để đổi chiều cao. Tọa độ/kích thước được quy đổi qua `getScale()` để tương thích CSS zoom, sau đó clamp vào vùng viewport còn lại. Nút hỗ trợ phím mũi tên (giữ Shift để tăng bước) và double-click để khôi phục kích thước gần mặc định. `motelona.css` vẽ grip và trạng thái `is-resizing`. Cần kiểm tra riêng resize và drag: header chỉ di chuyển cửa sổ, grip chỉ đổi kích thước.

### 4.3 Bố cục chat khi viewport thấp

Trong `frontend/motelona.css`, media query `@media screen and (max-height: 760px)` bật compact-height layout cho cửa sổ chat. Điều kiện này dùng chiều cao **CSS viewport**; nó không kiểm tra trực tiếp browser zoom. Chrome ở 100% và viewport 620 CSS px sẽ khớp breakpoint; ở cùng kích thước cửa sổ nhưng browser zoom 75%, CSS viewport có thể lớn hơn 760px và trở về bố cục thường.

Các selector và hành vi trong compact mode:

- `.motelona-ai-header`: giảm padding/gap; logo và nút icon được thu nhỏ vừa phải. Header vẫn là drag handle.
- `.motelona-ai-toolsbar`: `flex-wrap: nowrap`, `overflow-x: auto`; các `.motelona-ai-chip` giữ kích thước nội dung, không co ép thành nhiều dòng.
- `.motelona-ai-search`: khi form tìm kiếm đang mở, input và nút Search/Close nằm trên cùng hàng cuộn ngang; `.motelona-ai-search-results` có vùng cuộn riêng và `max-height` giới hạn.
- `.motelona-ai-status`: giảm padding và font size, vẫn hiển thị trạng thái backend.
- `.motelona-ai-messages`: `flex: 1 1 auto`, có `min-height` để nhận phần chiều cao đã giải phóng; đây là vùng cuộn dọc cho hội thoại.
- `.motelona-ai-message`: giảm padding/font/line-height vừa phải, vẫn cho phép wrap từ dài (`overflow-wrap: anywhere`).
- `.motelona-ai-suggestions`: `flex-wrap: nowrap`, `overflow-x: auto`; toàn bộ prompt mẫu vẫn ở DOM và dùng được qua cuộn ngang.
- `.motelona-ai-compose`, `.motelona-ai-input`, `.motelona-ai-send` và `.motelona-ai-footer`: giảm khoảng cách/kích thước có kiểm soát để composer và footer vẫn ở cuối panel, không bị suggestions che.
- Với `@media screen and (max-height: 500px)`, tiếp tục giảm padding/kích thước một số thành phần; không ẩn các nhóm chức năng.

Đây là thay đổi bố cục flex/overflow, không dùng `zoom` hoặc `transform: scale()` lên panel và không thay đổi JavaScript/API/backend. Khi viewport CSS cao hơn 760px, bố cục nhiều hàng thông thường được giữ nguyên.

Khi thay đổi CSS compact-height, phải thử browser zoom 100% tại viewport cao khoảng 620px và 850px; nên thử cả 760px và 761px để kiểm tra ranh giới breakpoint. Ở viewport thấp, kiểm tra lời chào/meta, tin nhắn dài, scroll dọc, tool chips, prompt mẫu, form tìm kiếm đang mở, composer, nút gửi, footer và drag header. Ở viewport cao, kiểm tra bố cục nhiều hàng không bị thay đổi ngoài ý muốn.

### 4.3 Ngữ cảnh website gửi lên AI

`pageContext()` thu thập nội dung văn bản từ các ID section cho phép (ví dụ hero/about/education/skills/projects/stats/contact), rút gọn và giới hạn chiều dài trước khi gửi. Điều này giúp backend trả lời dựa trên nội dung trang mà người dùng đang xem.

Không thu thập hoặc gửi thêm dữ liệu cá nhân không liên quan. Khi thêm section mới, cần cập nhật danh sách context và ActionRouter nếu muốn AI tìm/điều hướng đến section đó.

## 5. Backend Java Spring Boot

Đường dẫn package:

`Project/1/motelona-ai/backend/src/main/java/com/motelona/assistant/`

### 5.1 Điểm khởi chạy và cấu hình build

- `MotelonaApplication.java`: entry point Spring Boot.
- `pom.xml`: khai báo Java 21, Spring Boot parent, Web MVC, JDBC, MySQL Connector, Validation và test starter.
- `.mvn/wrapper/`, `mvnw`, `mvnw.cmd`: Maven Wrapper.
- `run-local.ps1`: đọc `.env`, kiểm tra password rồi chạy `mvnw clean verify` và `spring-boot:run`.
- `VERIFY_MOTELONA.ps1`: build và chạy kiểm tra tích hợp bằng backend process tạm.

### 5.2 Package `controller/`

- `AiController.java`: health, chat, explain, local website search, endpoint web search bị tắt.
- `MemoryController.java`: đọc/lưu/xóa memories; đọc/xóa history; export dữ liệu theo session.
- `PortfolioController.java`: trạng thái DB, public Portfolio data, contact message.

Controller nhận HTTP request và gọi service. Không nhồi thuật toán NLP hoặc query phức tạp vào controller nếu có thể đặt trong service phù hợp.

### 5.3 Package `dto/`

DTO là hợp đồng JSON giữa Java và frontend. Các file gồm `ChatRequest`, `ChatResponse`, `ExplainRequest`, `WebsiteSearchRequest`, `MemoryRequest`, `MemoryItem`, `ConversationItem`, `MessageTurn`, `ContactMessageRequest`, `UiAction`, `ApiError`.

Khi đổi tên/thêm trường record, kiểm tra `motelona.js`, mọi controller/service dùng DTO và test. Frontend không được gửi thêm trường sai cấu trúc vào request khác loại.

### 5.4 Package `ai/`

- `IntentClassifier.java`: phân nhóm ý định/ngữ cảnh câu hỏi.
- `TextVectorizer.java`: chuẩn hóa/tách văn bản và tính vector/độ tương đồng theo cách triển khai trong code.
- `KnowledgeRetrievalService.java`: truy xuất/xếp hạng tri thức có sẵn.
- `LocalAiEngine.java`: tổng hợp phản hồi cục bộ theo ý định và thông tin truy xuất.

Đây là engine NLP theo phạm vi hẹp. Không gọi là neural LLM, không tự huấn luyện, không tự có kiến thức Internet mới.

### 5.5 Package `service/`

- `AiChatService.java`: điều phối chat/explain, opt-in web search, điều hướng và lưu lịch sử khi consent bật.
- `PortfolioService.java`: đọc dữ liệu Portfolio, trạng thái kết nối và contact message.
- `GlossaryService.java`: định nghĩa thuật ngữ kỹ thuật.
- `LocalSearchService.java`: xếp hạng các đoạn văn bản trong context được cung cấp.
- `ActionRouter.java`: biến yêu cầu điều hướng tường minh thành `UiAction`; map section cố định, có thể chọn section từ context và mang theo cụm cần tô vàng.
- `RateLimitService.java`: giới hạn request theo IP/nhóm hành động.
- `AiUnavailableException.java`, `RateLimitException.java`: exception nghiệp vụ.

### 5.6 Package còn lại

- `memory/MemoryService.java`: thao tác memory theo session.
- `search/SearchProvider.java`: abstraction tìm Internet và kiểu dữ liệu title/snippet/url.
- `search/DuckDuckGoHtmlSearchProvider.java`: dùng `HttpClient` lấy trang kết quả HTML công khai của DuckDuckGo, parse title/snippet, kiểm tra URL HTTP(S), giới hạn query/response/result và timeout; không cần API key.
- `search/SearchUnavailableException.java`: lỗi tạm thời/không đọc được kết quả web.
- `config/CorsConfig.java`: allow-list origin; wildcard CORS bị từ chối.
- `config/ApiSecurityHeadersFilter.java`: security headers.
- `config/ApiExceptionHandler.java`: chuẩn hóa lỗi API.

## 6. REST API và frontend contract

| Controller | Endpoint | Request/response trọng tâm |
|---|---|---|
| `AiController` | `GET /api/ai/health` | Health map, engine/provider/api key/search status. |
| `AiController` | `POST /api/ai/chat` | `ChatRequest` → `ChatResponse`; gồm session, message, language, context, history/memory consent, `searchWeb` và `webResults`. Web search chạy khi toggle bật hoặc lệnh tìm web được nêu tường minh. |
| `AiController` | `POST /api/ai/explain` | `ExplainRequest` → `ChatResponse`; nội dung selection và context. |
| `AiController` | `POST /api/ai/search` | `WebsiteSearchRequest` → results trong context website. |
| `AiController` | `GET /api/ai/search/web` | DuckDuckGo HTML search; 200 ký tự tối đa, rate limit 10 lần/phút, lỗi provider trả 503. |
| `MemoryController` | `/api/ai/memories` | List/create/delete memory; create yêu cầu consent. |
| `MemoryController` | `/api/ai/history/{sessionId}` | Read/delete history. |
| `MemoryController` | `/api/ai/export/{sessionId}` | Export history và memories. |
| `PortfolioController` | `/api/portfolio/status` | Trạng thái DB. |
| `PortfolioController` | `/api/portfolio/public` | Dữ liệu Portfolio. |
| `PortfolioController` | `/api/portfolio/contact` | Lưu contact message. |

Các endpoint memory có thêm `sessionId` qua path hoặc query tùy mapping. Xem trực tiếp annotation trong controller trước khi viết client mới. Web query được gửi đến DuckDuckGo khi có yêu cầu rõ ràng; kết quả snippet không được mô hình AI ngoài tổng hợp hoặc xác minh.

## 7. Cơ sở dữ liệu MySQL

Database `PortfolioAI_DB` có schema hiện tại trong hai file:

- Script cài đặt: `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql`.
- Schema khởi tạo backend: `Project/1/motelona-ai/backend/src/main/resources/schema.sql`.

Hai schema hiện liệt kê cùng 17 bảng:

1. `AdminAccount`
2. `Profile`
3. `Education`
4. `Experience`
5. `SkillCategory`
6. `Skill`
7. `Project`
8. `Technology`
9. `ProjectTechnology`
10. `ContactMessage`
11. `AIConversation`
12. `AIMessage`
13. `AIKnowledgeItem`
14. `SiteSetting`
15. `conversation_messages`
16. `ai_api_daily_usage`
17. `user_memories`

Nội dung portfolio trong MySQL có các trường dịch tiếng Việt tùy chọn: `MajorVi`, `DegreeVi`, `DescriptionVi`, `CategoryNameVi`, `SkillNameVi`, `TitleVi`, `ShortDescriptionVi` và `FullDescriptionVi`. Frontend chọn trường theo ngôn ngữ đang bật và dùng giá trị gốc nếu trường dịch còn trống. Cài mới dùng hai schema hiện tại; database cũ cần chạy `Project/1/motelona-ai/database/V002_BilingualContent_MySQL.sql` một lần trước khi khởi động backend mới.

Nhóm bảng và quan hệ nổi bật:

- Thông tin hồ sơ: `Profile`, `Education`, `Experience`.
- Kỹ năng: `SkillCategory` → `Skill`.
- Dự án/công nghệ: `Project` ↔ `Technology` qua `ProjectTechnology`.
- Liên hệ/quản trị: `ContactMessage`, `AdminAccount`.
- Hội thoại lưu trữ theo cấu trúc archive: `AIConversation` → `AIMessage`.
- Dữ liệu kiến thức: `AIKnowledgeItem`.
- Lịch sử/memory theo session của phiên bản hiện tại: `conversation_messages`, `user_memories`.
- `ai_api_daily_usage` vẫn tồn tại trong schema nhưng engine hiện tại không dùng API AI bên thứ ba.

`application.yml` đang cấu hình `spring.sql.init.mode: always`; schema đi cùng backend có thể được thực thi mỗi lần khởi động. Vì vậy schema phải tương thích với database đã tồn tại. Trước khi thay DDL, tìm query JDBC và đánh giá migration/backward compatibility.

## 8. Thứ tự nạp CSS/JS quan trọng

Không tùy tiện đảo thứ tự stylesheet hoặc script. Xem danh sách đầy đủ và bảng phụ thuộc trong `05_BAN_DO_PHU_THUOC.md`.

Các CSS cuối của Portfolio hiện có vai trò override rõ ràng: `responsive-overrides.css` → `homepage-polish.css` → `portfolio-ultimate.css` → `portfolio-refined.css` → `portfolio-visual-scale.css` → `typography-refinement.css`. Trang chi tiết MoteLona dùng `motelona-premium.css` làm lớp layout chính rồi nạp `typography-refinement.css` làm lớp typography chung; các stylesheet detail cũ không được nạp.

## 9. Tìm đúng file theo công việc

| Công việc | File nên kiểm tra đầu tiên |
|---|---|
| Sửa nội dung Home/section | `index.html`, `assets/js/language-toggle.js` |
| Sửa bố cục Portfolio | `assets/css/animfolio-base.css`, `format.css`, `responsive-overrides.css`, `portfolio-refined.css` |
| Sửa họa tiết Hero | `assets/css/homepage-polish.css` |
| Sửa tỷ lệ 75% section | `assets/css/portfolio-visual-scale.css` |
| Sửa layout/trang trí trang chi tiết MoteLona | `assets/css/motelona-premium.css` |
| Sửa typography dùng chung hai trang | `assets/css/typography-refinement.css` |
| Sửa ngôn ngữ/chat | `frontend/motelona.js`, `assets/js/language-toggle.js` tùy khu vực |
| Sửa launcher/panel/drag | `frontend/motelona.css`, `frontend/motelona.js` |
| Sửa URL backend | `frontend/motelona-config.js` và cấu hình public trước khi nạp file |
| Sửa intent/response Java | `ai/IntentClassifier.java`, `ai/LocalAiEngine.java`, `service/AiChatService.java` |
| Sửa local search | `service/LocalSearchService.java`, `ai/TextVectorizer.java`, frontend pageContext |
| Sửa điều hướng | `service/ActionRouter.java`, `dto/UiAction.java`, frontend `performNavigation()`, section IDs |
| Sửa glossary | `service/GlossaryService.java`, frontend selection/highlight |
| Sửa memory/history | `MemoryController.java`, `MemoryService.java`, DTO, JS và SQL |
| Sửa DB fields | cả hai schema SQL + query JDBC + DTO + tests |
| Sửa contact | `animfolio-contact-static.js`, `PortfolioController.java`, `PortfolioService.java`, DTO |

## 10. Lưu ý kỹ thuật khi bảo trì

- Không dùng `innerHTML` với nội dung do người dùng hoặc DB cung cấp nếu chưa sanitize an toàn.
- Giữ URL ngoài được xác thực protocol HTTP/HTTPS trước khi tạo link.
- Giữ CORS allow-list, security headers, request validation và rate limit.
- Không đổi session format nếu history/memory đang lưu theo session ID.
- Không thay đổi schema riêng lẻ; phải đồng bộ cả script cài đặt và schema backend.
- Không đưa `.env` vào ZIP chia sẻ công khai.
- Khi sửa các thuộc tính `data-*` hoặc CSS class mà JS truy vấn, kiểm tra toàn bộ references.
