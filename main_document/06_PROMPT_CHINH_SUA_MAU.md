# 06 — PROMPT MẪU KHI GIAO ZIP CHO AI CHỈNH SỬA

Sao chép prompt này, đính kèm toàn bộ ZIP dự án hiện tại và thay phần `[YÊU CẦU CỤ THỂ]` bằng nhiệm vụ cần thực hiện.

---

## PROMPT

Bạn là Senior Full-Stack Engineer, Software Architect và QA Engineer chịu trách nhiệm chỉnh sửa dự án Portfolio + MoteLona được đính kèm.

### A. Bắt buộc đọc và hiểu dự án trước khi sửa

Đọc toàn bộ tài liệu trong `main_document/` theo thứ tự:

1. `00_AI_MASTER_GUIDE.md`
2. `01_SO_DO_FILE.md`
3. `05_BAN_DO_PHU_THUOC.md`
4. `03_GIAI_THICH_SO_DO_FILE.md`
5. `02_HUONG_DAN_CAI_DAT_SU_DUNG.md`
6. `04_UPGRADE_NOTES.md`
7. `07_CHECKLIST_KIEM_THU.md`
8. `08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md` nếu yêu cầu liên quan đến Java/Spring Boot hoặc chuẩn bị bảo vệ.
9. `09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md` nếu yêu cầu liên quan đến cài đặt/kết nối MySQL.

Sau đó kiểm tra trực tiếp các file mã nguồn liên quan. Không giả định tài liệu luôn chính xác hơn mã nguồn; nếu có mâu thuẫn, ưu tiên mã nguồn của ZIP này và cập nhật tài liệu sau khi sửa.

### B. Bối cảnh kỹ thuật bắt buộc phải hiểu

- Portfolio: HTML/CSS/JavaScript, trang chính là `index.html`.
- Trang tài liệu MoteLona: `Project/1/motelona.html`.
- Chat frontend: `Project/1/motelona-ai/frontend/motelona.js` và `motelona.css`.
- Layout trang chi tiết MoteLona: `assets/css/motelona-premium.css`; typography chung hai trang: `assets/css/typography-refinement.css` (được nạp cuối). Không nạp lại các stylesheet detail cũ hoặc CSS zoom cũ.
- Backend: Java 21 + Spring Boot.
- Database: MySQL 8, database `PortfolioAI_DB`.
- AI: NLP/truy xuất tri thức Java tự xây; không gọi OpenAI/Gemini/LLM bên ngoài trong phiên bản hiện tại.
- Tìm kiếm trong website khác với tìm Internet. Internet search đã tích hợp opt-in qua trang HTML công khai DuckDuckGo, không cần API key; query chỉ gửi khi khách truy cập bật nút Search web hoặc nêu yêu cầu rõ. Kết quả cần link nguồn; lỗi/thiếu dữ liệu không được biến thành câu trả lời giả.
- Điều hướng được phép chuyển giữa `index.html` và `Project/1/motelona.html`, nhưng chỉ tới page/section ID trong allow-list. Tô vàng cụm khớp bằng DOM range, không chèn query như HTML thực thi.
- MoteLona dùng MySQL. Khi thay đổi endpoint, DTO, schema hoặc tìm web, đồng bộ Java, frontend, SQL nếu cần, tests và tài liệu.
- MoteLona có chức năng kéo di chuyển cửa sổ chat bằng header và resize bằng grip ở góc dưới bên phải; phải giữ nút Settings/Close/input/send hoạt động. Không gộp hai vùng kéo thành một.
- Trên trang chi tiết, AI có thể điều hướng tới Overview, Capabilities, Architecture, NLP basics, Data & API, Quality, Scope và Roadmap; `pageContext()` gửi nội dung section tương ứng cho bộ truy xuất Java.
- Chat có compact-height CSS tại `frontend/motelona.css` với breakpoint `max-height: 760px` theo CSS viewport height (không phải phần trăm zoom của Chrome); tool row, search row khi mở và suggestions cuộn ngang, messages cuộn dọc. Viewport `max-height: 500px` có tinh chỉnh thêm.
- Trang chủ `#hero` được giữ nguyên khi yêu cầu không nhắc tới nó.
- Các section Portfolio ngoài Home dùng tỷ lệ hiển thị 75% (`zoom: 0.75`) trên desktop. Trang chi tiết MoteLona dùng tỷ lệ tự nhiên ở browser zoom 100%, giao diện responsive qua `assets/css/motelona-premium.css` và typography chung qua `assets/css/typography-refinement.css`; không thêm CSS zoom cho trang này.
- Hệ thống có EN/VI, sáng/tối, bảng màu, responsive, contact, search, explain, navigation, history/memory theo consent. Không tự xóa các chức năng này. Với nội dung nằm trong phạm vi yêu cầu, kiểm tra cả hai ngôn ngữ trên trang tĩnh, trạng thái phát sinh động, thông báo lỗi/thành công, metadata và nhãn trợ năng; giữ nguyên dữ kiện cá nhân và chỉ điền nội dung dịch vào dữ liệu có căn cứ.

### C. Yêu cầu lần này

`[YÊU CẦU CỤ THỂ — ghi rõ kết quả mong muốn, các khu vực được phép thay đổi và các phần tuyệt đối không thay đổi]`

### D. Quy tắc thực hiện

1. Phân tích yêu cầu thành các tiêu chí nghiệm thu cụ thể.
2. Xác định chính xác file phải sửa và các phụ thuộc theo `05_BAN_DO_PHU_THUOC.md`.
3. Chỉ sửa phần cần thiết; không tái cấu trúc hoặc đổi tên file ngoài phạm vi.
4. Giữ nguyên mọi chức năng cũ đang hoạt động và dữ kiện người dùng đã cung cấp; nếu yêu cầu có dịch, diễn đạt lại nội dung đó tự nhiên ở ngôn ngữ đích nhưng không đổi ý nghĩa.
5. Không tạo dữ liệu, thông tin cá nhân, link, endpoint, secret hoặc chức năng giả.
6. Không đưa mật khẩu hoặc secret vào frontend; không mở CORS wildcard.
7. Không thay API contract, schema, ID, class hay data attribute mà không cập nhật đồng bộ mọi nơi phụ thuộc.
8. Không tuyên bố đã test browser/MySQL/deployment nếu chưa thực sự chạy.
9. Cập nhật tài liệu trong `main_document/` để phản ánh mã nguồn mới.

### E. Kiểm thử và bàn giao

- Chạy `node --check` cho JavaScript nếu môi trường có Node.js.
- Chạy `mvnw.cmd clean test` trong backend nếu có JDK/Maven Wrapper hoạt động.
- Nếu có MySQL và `.env`, chạy thêm `VERIFY_MOTELONA.ps1`.
- Kiểm tra HTML IDs, đường dẫn CSS/JS, CSS cascade, độ đầy đủ và tự nhiên EN/VI (kể cả nội dung tạo động, aria-label/alt/title và metadata), light/dark, theme, desktop/mobile, zoom 100%, launcher/chat drag, các API bị ảnh hưởng.
- Với MoteLona, kiểm tra browser zoom 100% tại CSS viewport height khoảng 620px và 850px; thử sát 760px/761px. Compact-height chỉ bật khi CSS viewport height ≤760px, không phải cứ browser zoom 100% là bật. Phải giữ mọi nút, cho phép cuộn ngang ở tools/search/suggestions, cuộn dọc trong messages, không để welcome message bị che/cắt, và không làm hỏng kéo thả. Thử thêm khi form tìm kiếm đang mở và ở viewport ≤500px.
- Ghi rõ test nào pass, fail, skipped và lý do.
- Đóng gói thành một ZIP duy nhất có đủ thư mục `Portfolio_Web/` và giữ nguyên relative paths.

### F. Định dạng câu trả lời cuối

1. Link tải ZIP hoàn chỉnh.
2. Tóm tắt thay đổi.
3. Danh sách file đã sửa/thêm/xóa.
4. Những tính năng cũ đã được giữ lại.
5. Các bước kiểm tra đã thực hiện và kết quả.
6. Giới hạn hoặc phần chưa thể kiểm thử.

Không chỉ đưa ra hướng dẫn hoặc đoạn code mẫu. Hãy chỉnh sửa trực tiếp các file trong ZIP và bàn giao file ZIP thực tế.

---

## Mẹo dùng prompt

- Nên giao mỗi lần một nhóm yêu cầu có liên quan để AI kiểm soát tác động tốt hơn.
- Nói rõ những phần phải giữ nguyên, ví dụ: “không sửa Home”, “giữ 75% cho section Portfolio ngoài Home”, “không thêm CSS zoom cho trang chi tiết MoteLona”, “không thay đổi Java AI engine”, “không thay cấu trúc thư mục”.
- Mỗi lần chỉnh sửa nên dùng ZIP mới nhất làm đầu vào, không dùng ZIP cũ hơn.
