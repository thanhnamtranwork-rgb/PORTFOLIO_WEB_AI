# 05 — BẢN ĐỒ PHỤ THUỘC VÀ FILE CẦN SỬA THEO CHỨC NĂNG

Tài liệu này trả lời câu hỏi: **muốn thay đổi một tính năng thì phải kiểm tra những file nào, và các file đó phụ thuộc nhau ra sao?** Đọc cùng `00_AI_MASTER_GUIDE.md` trước khi chỉnh sửa.

## 1. Bản đồ chức năng → file

| Chức năng / yêu cầu | Điểm bắt đầu | File liên quan chính | Kiểm tra bắt buộc |
|---|---|---|---|
| Nội dung trang chủ / Hero | `index.html` → `#hero` | `assets/css/animfolio-base.css`, `format.css`, `homepage-polish.css`, `assets/js/homepage-zoom.js`, `assets/js/portfolio-experience.js` | Home không bị thay đổi ngoài yêu cầu; nav và mobile. |
| About / Giới thiệu | `index.html` → `#about` | `assets/css/portfolio-visual-scale.css`, `responsive-overrides.css`, `portfolio-refined.css`, `assets/js/language-toggle.js` | EN/VI, hiển thị 75% desktop, mobile responsive. |
| Education / Học vấn | `index.html` → `#education` | `assets/css/format.css`, `portfolio-visual-scale.css`, `assets/js/portfolio-data.js`, `assets/js/language-toggle.js` | Nội dung tĩnh và dữ liệu MySQL không trùng; anchor. |
| Skills / Kỹ năng | `index.html` → `#skills` | `assets/css/format.css`, `portfolio-visual-scale.css`, `assets/js/portfolio-data.js` | Nhóm kỹ năng, tỷ lệ tiến độ, DB fallback. |
| Projects / Dự án | `index.html` → `#projects` | `assets/css/animfolio-gallery.css`, `portfolio-ultimate.css`, `portfolio-refined.css`, `portfolio-visual-scale.css`, `motelona.css`, `assets/js/portfolio-data.js`, `motelona.js` | Card MoteLona, nút mở chat, liên kết trang chi tiết. |
| Contact form | `index.html` → `#contact` | `assets/css/animfolio-contact.css`, `assets/js/animfolio-contact-static.js`, `PortfolioController.java`, `PortfolioService.java`, `ContactMessageRequest.java`, `application.yml` | Validation, honeypot, DB save, FormSubmit email tùy cấu hình. |
| Nav sticky / hamburger | `index.html` nav | `assets/css/animfolio-nav.css`, `responsive-overrides.css`, `assets/js/modules/smooth-scroll.js`, `portfolio-experience.js` | Anchor đúng, header offset, bàn phím/mobile. |
| EN/VI Portfolio | HTML `data-*`/text | `assets/js/language-toggle.js`, `index.html` | Mọi nút/label/placeholders cập nhật, không làm mất text gốc. |
| Sáng/tối Portfolio | `index.html` theme controls | `assets/css/portfolio-theme.css`, inline CSS theme vars, `assets/js/modules/theme-toggle.js`, `assets/js/portfolio-theme.js` | Tương phản, lưu lựa chọn, trạng thái `aria-pressed`, reduced motion. |
| Bảng màu Portfolio | `index.html` color picker | `portfolio-theme.css`, inline theme style, `assets/js/portfolio-theme.js` | Các key theme trong HTML và JS khớp nhau. |
| Tỷ lệ 75% các section ngoài Home | `assets/css/portfolio-visual-scale.css` | `index.html` link CSS, `responsive-overrides.css`, `homepage-polish.css`, `portfolio-experience.js`, `smooth-scroll.js` | `#hero`/nav/chat không bị scale; desktop; mobile; anchor scroll. |
| Giao diện trang chi tiết MoteLona | `assets/css/motelona-premium.css` | `Project/1/motelona.html`, `assets/css/typography-refinement.css`, `motelona-detail.js`, `motelona-experience.js` | Layout/hero/cards/nav rail, light/dark, palette, responsive; typography chung được tinh chỉnh ở stylesheet nạp cuối; không zoom toàn trang. |
| Nút mở và cửa sổ chat | `data-motelona-ai-root` | `frontend/motelona.js`, `frontend/motelona.css`, `frontend/motelona-config.js` | Launcher, open/close, trạng thái, focus, responsive. |
| Kéo thả cửa sổ chat | `enablePanelDragging()` | `frontend/motelona.js`, `frontend/motelona.css` | Kéo bằng header; không chặn click nút; giữ trong viewport; chuột/cảm ứng. |
| Thay đổi kích thước cửa sổ chat | `enablePanelResizing()` / `[data-motelona-resize]` | `frontend/motelona.js`, `frontend/motelona.css` | Kéo grip góc dưới bên phải; giới hạn theo viewport; hỗ trợ chuột/pen/touch và bàn phím; không ảnh hưởng composer. |
| Bố cục chat compact-height | `@media screen and (max-height: 760px)` trong `frontend/motelona.css` | `frontend/motelona.css`; các class/DOM được tạo trong `frontend/motelona.js` | Điều kiện theo CSS viewport height (không phải browser zoom %); toolsbar, search row khi mở và suggestions cuộn ngang; messages cuộn dọc; composer/footer giữ chỗ; giữ tất cả nút. Kiểm tra thêm `max-height: 500px` và breakpoint 760/761px. |
| Cấu hình URL backend | `frontend/motelona-config.js` | `index.html`, `motelona.html`, `.env.example`, `application.yml`, `CorsConfig.java` | Localhost và domain public; không đặt secret frontend. |
| Chat hỏi đáp | `motelona.js` → `POST /api/ai/chat` | `AiController`, `ChatRequest`, `ChatResponse`, `AiChatService`, `IntentClassifier`, `LocalAiEngine`, `PortfolioService`, DB | Request DTO, câu trả lời, giới hạn tốc độ, không bịa dữ liệu. |
| Giải thích đoạn được chọn | `motelona.js` → `POST /api/ai/explain` | `AiController`, `ExplainRequest`, `AiChatService`, `GlossaryService`, `LocalAiEngine` | Đúng selectedText/context; text an toàn; phản hồi EN/VI. |
| Tìm kiếm nội dung trên trang | `motelona.js` → `POST /api/ai/search` | `AiController`, `WebsiteSearchRequest`, `AiChatService`, `LocalSearchService`, `TextVectorizer` | Chỉ tìm trong pageContext gửi từ frontend; không gọi web ngoài. |
| Tìm kiếm Internet | Chat mode hoặc GET `/api/ai/search/web` | `motelona.js`, `ChatRequest`, `AiChatService`, `AiController`, `DuckDuckGoHtmlSearchProvider`, `ApiExceptionHandler` | Chỉ chạy khi người xem bật Search web hoặc yêu cầu tường minh; không cần API key; cần outbound HTTPS; hiển thị title/snippet/link nguồn; lỗi provider trả thông báo/503. |
| Điều hướng và tô vàng AI | `performNavigation()` | `motelona.js`, `ActionRouter.java`, `UiAction.java`, IDs trong `index.html` và `motelona.html` | Chỉ route tới page/section ID allow-list; có thể chuyển giữa hai trang; topic chọn section theo context; cụm trùng DOM được tô vàng, không có match thì báo rõ. |
| Từ điển thuật ngữ chat | `motelona.js` popover/highlight | `GlossaryService.java`, `motelona.js`, `motelona.css` | Lookup, highlight, EN/VI, popover, keyboard. |
| Lịch sử hội thoại | chat settings | `motelona.js`, `MemoryController.java`, `MemoryService.java`, `conversation_messages`, `AIConversation` | Chỉ lưu khi history được bật; bật/tắt, tải, xóa, export. |
| Bộ nhớ dài hạn | chat settings/consent | `motelona.js`, `MemoryController.java`, `MemoryService.java`, `MemoryRequest.java`, `user_memories` | POST phải có `consent=true`; session validation; delete one/all. |
| API health | trạng thái chat | `AiController.java`, `LocalAiEngine.java`, `SearchProvider` | `provider=java-local`, `apiKeyRequired=false`, `internetSearchEnabled=true`, provider `DuckDuckGo HTML (no API key)`; adapter enabled không bảo đảm server có network. |
| Dữ liệu Portfolio DB | `portfolio-data.js` | `PortfolioController`, `PortfolioService`, `application.yml`, `schema.sql`, MySQL script | DB offline vẫn có HTML fallback; tránh duplicate khi hydrate. |
| Glossary/trang chi tiết | `motelona.html` → `#knowledge` | `motelona-detail.js`, `motelona-experience.js`, `motelona-premium.css`, `typography-refinement.css` | Search EN/VI, số kết quả, no-result state. |
| Checklist trang chi tiết | `motelona.html` → `#quality` | `motelona-experience.js`, `motelona-premium.css`, `typography-refinement.css` | Tick/reset, state local browser, progress count. |
| Theme trang chi tiết | `motelona.html` theme controls | `motelona-detail.js`, `motelona-premium.css`, `typography-refinement.css` | EN/VI, light/dark, palettes, reduced motion. |

## 2. Thứ tự CSS — Portfolio `index.html`

Các stylesheet được nạp theo thứ tự trong HTML. CSS xuất hiện sau có thể ghi đè CSS trước; đừng di chuyển hoặc xóa link chỉ để sắp xếp lại nếu chưa kiểm tra cascade.

1. `assets/css/animfolio-base.css`
2. `assets/css/animfolio-animations.css`
3. `assets/css/animfolio-nav.css`
4. `assets/css/animfolio-gallery.css`
5. `assets/css/animfolio-print.css` (media print)
6. `assets/css/format.css`
7. `assets/css/animfolio-contact.css`
8. `assets/css/portfolio-theme.css`
9. Các `<style>` nội tuyến trong `index.html` (theme variables, nature enhancements, language switch style)
10. `assets/css/responsive-overrides.css`
11. `assets/css/homepage-polish.css`
12. `assets/css/portfolio-ultimate.css`
13. `assets/css/portfolio-refined.css`
14. `assets/css/portfolio-visual-scale.css` — lớp scale 75% các section ngoài Hero.
15. `assets/css/typography-refinement.css` — lớp typography chung được nạp cuối.

Nếu thêm CSS override, nên ghi rõ vì sao cần đặt ở vị trí đó. Tránh tạo selector quá rộng như `section`, `button`, `body *` gây ảnh hưởng toàn website.

## 3. Thứ tự JavaScript — Portfolio `index.html`

Script thực tế được nạp từ HTML. `homepage-zoom.js` được nạp ở phần head; `motelona-config.js` và `motelona.js` xuất hiện ở khu vực dự án; các script dưới đây ở cuối trang:

1. `assets/js/homepage-zoom.js` (đầu trang; điều chỉnh riêng Home/launcher theo DPR).
2. `Project/1/motelona-ai/frontend/motelona-config.js`.
3. `Project/1/motelona-ai/frontend/motelona.js` (`defer`; runtime chatbot).
4. `assets/js/wp-shim.js`.
5. `assets/js/animfolio-engine.js`.
6. `assets/js/modules/scroll-trigger.js`.
7. `assets/js/modules/typed-text.js`.
8. `assets/js/modules/counter.js`.
9. `assets/js/modules/timeline-draw.js`.
10. `assets/js/modules/stagger.js`.
11. `assets/js/modules/parallax.js`.
12. `assets/js/modules/progress-bar.js`.
13. `assets/js/modules/smooth-scroll.js`.
14. `assets/js/modules/theme-toggle.js`.
15. `assets/js/animfolio-lightbox.js`.
16. `assets/js/animfolio-casestudy.js`.
17. `assets/js/format.js`.
18. `assets/js/portfolio-data.js`.
19. `assets/js/animfolio-contact-static.js`.
20. `assets/js/language-toggle.js`.
21. `assets/js/portfolio-theme.js`.
22. `assets/js/portfolio-experience.js` (`defer`).

Danh sách trên thể hiện thứ tự tham chiếu trong HTML. Lưu ý `motelona.js` và `portfolio-experience.js` có `defer`, vì vậy thời điểm thực thi của chúng tuân theo quy tắc deferred script sau khi parser hoàn tất; khi sửa cần giữ đúng thuộc tính `defer` và thứ tự tương đối giữa các script deferred. Không chuyển toàn bộ script sang `async` hoặc đảo thứ tự một cách tùy tiện; một số script truy vấn DOM, biến cài đặt hoặc hành vi do script trước tạo.

## 4. Trang chi tiết — thứ tự CSS và JavaScript

CSS theo thứ tự:

1. `assets/css/motelona-premium.css` — stylesheet chính của trang chi tiết, layout tự nhiên/responsive.
2. `assets/css/typography-refinement.css` — lớp typography chung được nạp sau cùng.

`motelona-detail.css`, `motelona-responsive.css`, `motelona-ultimate.css`, `motelona-refined.css`, `motelona-detail-zoom.css` là các lớp legacy không còn được `motelona.html` nạp.

JavaScript:

1. `Project/1/motelona-ai/frontend/motelona-config.js`
2. `Project/1/motelona-ai/frontend/motelona.js`
3. `assets/js/motelona-detail.js`
4. `assets/js/motelona-experience.js`

## 5. Backend Java — bản đồ phụ thuộc

```text
MotelonaApplication
  └── Spring Boot auto-configuration
      ├── config/CorsConfig
      ├── config/ApiSecurityHeadersFilter
      ├── config/ApiExceptionHandler
      ├── controller/AiController
      │   ├── dto/ChatRequest, ExplainRequest, WebsiteSearchRequest
      │   ├── service/AiChatService
      │   │   ├── ai/IntentClassifier
      │   │   ├── ai/LocalAiEngine
      │   │   ├── ai/KnowledgeRetrievalService
      │   │   ├── ai/TextVectorizer
      │   │   ├── service/PortfolioService
      │   │   ├── service/GlossaryService
      │   │   ├── service/LocalSearchService
      │   │   └── service/ActionRouter
      │   └── service/RateLimitService
      ├── controller/MemoryController
      │   ├── memory/MemoryService
      │   ├── jdbc/JdbcTemplate
      │   └── service/RateLimitService
      └── controller/PortfolioController
          ├── service/PortfolioService
          └── service/RateLimitService
```

Đây là sơ đồ khái niệm dựa trên các dependency được inject trong source; không xem đây là sơ đồ bean đầy đủ của Spring.

## 6. Những cặp file phải được cập nhật cùng nhau

| Khi thay đổi | Đồng bộ tối thiểu |
|---|---|
| ID section | `index.html`, `ActionRouter.java`, `motelona.js` allow-list, nav anchors, ngôn ngữ và tài liệu. |
| Request JSON | Frontend `motelona.js`, DTO Java, controller/service, unit/integration tests. |
| Response JSON | DTO/service/controller, cách frontend đọc trường response, test. |
| API endpoint | Controller mapping, frontend URL, CORS/host config nếu ảnh hưởng, tài liệu. |
| CSS class do JS tạo/truy vấn | CSS và mọi `querySelector`/`classList` có liên quan. |
| `data-*` attribute | HTML, JS selector và test liên quan. |
| Nội dung/song ngữ EN–VI | `language-toggle.js`, `motelona-detail.js`, `motelona.js`, `portfolio-data.js`, form liên hệ, lightbox, metadata HTML | Kiểm tra cả văn bản, trạng thái động, thông báo lỗi/thành công, aria-label/alt/title, dữ liệu MySQL và hành vi đổi ngôn ngữ khi đang mở thành phần tương tác. |
| Theme key | Giá trị `data-theme-choice`/`data-portfolio-theme`, JS map màu, CSS variables, trạng thái ARIA và storage. |
| Language dictionary | `motelona.js` dictionary hoặc `language-toggle.js` map, mọi text attribute trong HTML. |
| MySQL column/table | `PortfolioAI_MySQL.sql`, `V002_BilingualContent_MySQL.sql`, `backend/src/main/resources/schema.sql`, query JDBC/row mapper, `portfolio-data.js`, DTO/service và tests. |
| CSS order | `<link>` trong HTML; xem cả override phía sau và media query. |
| File được nạp qua relative path | HTML `src`/`href`, đường dẫn khi chạy từ root và trang con `Project/1/`. |

## 7. Trình tự phân tích tác động trước khi sửa

Trước khi sửa, AI nên thực hiện tìm kiếm toàn dự án cho:

- ID/class/data attribute đang đổi.
- tên method Java hoặc JavaScript.
- endpoint `/api/...`.
- tên trường request/response.
- bảng/cột SQL.
- đường dẫn CSS/JS trong `href`/`src`.

Sau khi sửa, lặp lại tìm kiếm để xác nhận không còn tham chiếu cũ ngoài những nơi được chủ động giữ tương thích. Không được tự xóa fallback hoặc biến thể tương thích chỉ vì tìm thấy chúng xuất hiện nhiều hơn một lần.
