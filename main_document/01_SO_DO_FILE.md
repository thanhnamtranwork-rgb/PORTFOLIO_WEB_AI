# 01 — SƠ ĐỒ CẤU TRÚC FILE PORTFOLIO + MOTELONA

Tài liệu này phản ánh cấu trúc thư mục trong gói ZIP hiện tại. Cây thư mục được tạo từ các file thực tế trong dự án tại thời điểm cập nhật. Các thư mục build phát sinh (`target/`), cache IDE, `node_modules/`, metadata Git và file bí mật cá nhân (`.env`) không có trong bản ZIP này nên không được liệt kê.

- Thư mục gốc: `Portfolio_Web/`
- Số file hiện có (không tính thư mục): **119**
- Điểm vào Portfolio: `index.html`
- Điểm vào trang chi tiết MoteLona: `Project/1/motelona.html`
- Backend Java: `Project/1/motelona-ai/backend/`
- Bộ tài liệu: `main_document/`
- Lần đồng bộ nội dung gần nhất: **2026-11-18** (bổ sung hướng dẫn tải/cài MySQL và kết nối database MoteLona trên Windows).
- Tổng số file trong ZIP: **119** (gồm 10 file Markdown trong `main_document/`).

## Cây thư mục đầy đủ

```text
Portfolio_Web/
├── .github/
│   └── copilot-instructions.md
├── .gitignore
├── README.md
├── assets/
│   ├── css/
│   │   ├── animfolio-animations.css
│   │   ├── animfolio-base.css
│   │   ├── animfolio-contact.css
│   │   ├── animfolio-gallery.css
│   │   ├── animfolio-nav.css
│   │   ├── animfolio-print.css
│   │   ├── format.css
│   │   ├── homepage-polish.css
│   │   ├── motelona-premium.css
│   │   ├── motelona-detail-zoom.css
│   │   ├── motelona-detail.css
│   │   ├── motelona-refined.css
│   │   ├── motelona-responsive.css
│   │   ├── motelona-ultimate.css
│   │   ├── portfolio-refined.css
│   │   ├── portfolio-theme.css
│   │   ├── portfolio-ultimate.css
│   │   ├── portfolio-visual-scale.css
│   │   ├── responsive-overrides.css
│   │   └── typography-refinement.css
│   ├── images/
│   │   └── narrow-ai.jpg
│   └── js/
│       ├── modules/
│       │   ├── counter.js
│       │   ├── parallax.js
│       │   ├── progress-bar.js
│       │   ├── scroll-trigger.js
│       │   ├── smooth-scroll.js
│       │   ├── stagger.js
│       │   ├── theme-toggle.js
│       │   ├── timeline-draw.js
│       │   └── typed-text.js
│       ├── animfolio-casestudy.js
│       ├── animfolio-contact-static.js
│       ├── animfolio-engine.js
│       ├── animfolio-lightbox.js
│       ├── format.js
│       ├── homepage-zoom.js
│       ├── language-toggle.js
│       ├── motelona-detail.js
│       ├── motelona-experience.js
│       ├── portfolio-data.js
│       ├── portfolio-experience.js
│       ├── portfolio-theme.js
│       └── wp-shim.js
├── main_document/
│   ├── 00_AI_MASTER_GUIDE.md
│   ├── 01_SO_DO_FILE.md
│   ├── 02_HUONG_DAN_CAI_DAT_SU_DUNG.md
│   ├── 03_GIAI_THICH_SO_DO_FILE.md
│   ├── 04_UPGRADE_NOTES.md
│   ├── 05_BAN_DO_PHU_THUOC.md
│   ├── 06_PROMPT_CHINH_SUA_MAU.md
│   ├── 07_CHECKLIST_KIEM_THU.md
│   ├── 08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md
│   └── 09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md
├── Project/
│   └── 1/
│       ├── motelona-ai/
│       │   ├── backend/
│       │   │   ├── .mvn/
│       │   │   │   └── wrapper/
│       │   │   │       └── maven-wrapper.properties
│       │   │   ├── src/
│       │   │   │   ├── main/
│       │   │   │   │   ├── java/
│       │   │   │   │   │   └── com/
│       │   │   │   │   │       └── motelona/
│       │   │   │   │   │           └── assistant/
│       │   │   │   │   │               ├── ai/
│       │   │   │   │   │               │   ├── IntentClassifier.java
│       │   │   │   │   │               │   ├── KnowledgeRetrievalService.java
│       │   │   │   │   │               │   ├── LocalAiEngine.java
│       │   │   │   │   │               │   └── TextVectorizer.java
│       │   │   │   │   │               ├── config/
│       │   │   │   │   │               │   ├── ApiExceptionHandler.java
│       │   │   │   │   │               │   ├── ApiSecurityHeadersFilter.java
│       │   │   │   │   │               │   └── CorsConfig.java
│       │   │   │   │   │               ├── controller/
│       │   │   │   │   │               │   ├── AiController.java
│       │   │   │   │   │               │   ├── MemoryController.java
│       │   │   │   │   │               │   └── PortfolioController.java
│       │   │   │   │   │               ├── dto/
│       │   │   │   │   │               │   ├── ApiError.java
│       │   │   │   │   │               │   ├── ChatRequest.java
│       │   │   │   │   │               │   ├── ChatResponse.java
│       │   │   │   │   │               │   ├── ContactMessageRequest.java
│       │   │   │   │   │               │   ├── ConversationItem.java
│       │   │   │   │   │               │   ├── ExplainRequest.java
│       │   │   │   │   │               │   ├── MemoryItem.java
│       │   │   │   │   │               │   ├── MemoryRequest.java
│       │   │   │   │   │               │   ├── MessageTurn.java
│       │   │   │   │   │               │   ├── UiAction.java
│       │   │   │   │   │               │   └── WebsiteSearchRequest.java
│       │   │   │   │   │               ├── memory/
│       │   │   │   │   │               │   └── MemoryService.java
│       │   │   │   │   │               ├── search/
│       │   │   │   │   │               │   ├── DuckDuckGoHtmlSearchProvider.java
│       │   │   │   │   │               │   ├── SearchProvider.java
│       │   │   │   │   │               │   └── SearchUnavailableException.java
│       │   │   │   │   │               ├── service/
│       │   │   │   │   │               │   ├── ActionRouter.java
│       │   │   │   │   │               │   ├── AiChatService.java
│       │   │   │   │   │               │   ├── AiUnavailableException.java
│       │   │   │   │   │               │   ├── GlossaryService.java
│       │   │   │   │   │               │   ├── LocalSearchService.java
│       │   │   │   │   │               │   ├── PortfolioService.java
│       │   │   │   │   │               │   ├── RateLimitException.java
│       │   │   │   │   │               │   └── RateLimitService.java
│       │   │   │   │   │               └── MotelonaApplication.java
│       │   │   │   │   └── resources/
│       │   │   │   │       ├── application.yml
│       │   │   │   │       └── schema.sql
│       │   │   │   └── test/
│       │   │   │       └── java/
│       │   │   │           └── com/
│       │   │   │               └── motelona/
│       │   │   │                   └── assistant/
│       │   │   │                       ├── ai/
│       │   │   │                       │   ├── IntentClassifierTest.java
│       │   │   │                       │   └── LocalAiEngineTest.java
│       │   │   │                       ├── controller/
│       │   │   │                       │   └── AiControllerHealthTest.java
│       │   │   │                       ├── database/
│       │   │   │                       │   └── MySqlSchemaContractTest.java
│       │   │   │                       ├── search/
│       │   │   │                       │   └── DuckDuckGoHtmlSearchProviderTest.java
│       │   │   │                       └── service/
│       │   │   │                           ├── ActionRouterTest.java
│       │   │   │                           ├── GlossaryServiceTest.java
│       │   │   │                           ├── LocalSearchServiceTest.java
│       │   │   │                           ├── MemoryServiceTest.java
│       │   │   │                           ├── PortfolioServiceTest.java
│       │   │   │                           └── RateLimitServiceTest.java
│       │   │   ├── .env.example
│       │   │   ├── .gitignore
│       │   │   ├── mvnw
│       │   │   ├── mvnw.cmd
│       │   │   ├── pom.xml
│       │   │   ├── run-local.ps1
│       │   │   └── VERIFY_MOTELONA.ps1
│       │   ├── database/
│       │   │   ├── PortfolioAI_DB.sql
│       │   │   ├── PortfolioAI_MySQL.sql
│       │   │   └── V002_BilingualContent_MySQL.sql
│       │   └── frontend/
│       │       ├── motelona-config.js
│       │       ├── motelona.css
│       │       └── motelona.js
│       └── motelona.html
└── index.html
```

## Điểm bắt đầu theo mục đích

| Muốn làm gì | Mở file/thư mục này trước |
|---|---|
| Xem Portfolio | `index.html` |
| Xem trang giải thích MoteLona | `Project/1/motelona.html` |
| Sửa cửa sổ chat, nút mở, kéo thả | `Project/1/motelona-ai/frontend/motelona.js` và `motelona.css` |
| Sửa URL backend frontend | `Project/1/motelona-ai/frontend/motelona-config.js` |
| Chạy Java backend | `Project/1/motelona-ai/backend/run-local.ps1` hoặc `mvnw.cmd` |
| Sửa thuật toán NLP | `Project/1/motelona-ai/backend/src/main/java/com/motelona/assistant/ai/` |
| Sửa API / logic nghiệp vụ | `.../controller/` và `.../service/` |
| Cài database | `Project/1/motelona-ai/database/PortfolioAI_MySQL.sql` |
| Đọc hướng dẫn AI | `main_document/00_AI_MASTER_GUIDE.md` |
| Cài đặt/chạy website | `main_document/02_HUONG_DAN_CAI_DAT_SU_DUNG.md` |
| Tìm file liên quan khi sửa chức năng | `main_document/05_BAN_DO_PHU_THUOC.md` |
| Học và thuyết trình Java/Spring Boot/MySQL | `main_document/08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md` |

## Lưu ý duy trì sơ đồ

Khi thêm, xóa hoặc đổi vị trí file, phải cập nhật cây thư mục này. Không chỉ sửa phần tên file; đồng thời cập nhật các đường dẫn được tham chiếu trong HTML, CSS, JavaScript, script chạy, prompt và bảng phụ thuộc.

Nếu chỉ sửa nội dung bên trong một file mà không đổi cấu trúc, không cần tạo lại cây thư mục nhưng vẫn cần cập nhật `04_UPGRADE_NOTES.md` và tài liệu liên quan khi hành vi/chức năng đã thay đổi.
