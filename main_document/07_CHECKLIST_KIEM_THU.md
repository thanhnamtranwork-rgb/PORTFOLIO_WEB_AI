# 07 — CHECKLIST KIỂM THỬ VÀ NGHIỆM THU

Dùng checklist này sau mỗi lần AI hoặc lập trình viên chỉnh sửa. Đánh dấu `PASS`, `FAIL` hoặc `SKIP`; không để mục chưa chạy bị hiểu nhầm là đã đạt.

## 1. Kiểm tra cấu trúc và tài nguyên

- [ ] Giải nén ZIP thành công.
- [ ] Có thư mục gốc `Portfolio_Web/`.
- [ ] `index.html` còn ở đúng vị trí.
- [ ] `Project/1/motelona.html` còn ở đúng vị trí.
- [ ] Các `href` CSS và `src` JavaScript nội bộ đều trỏ tới file tồn tại.
- [ ] Không đổi tên hoặc di chuyển thư mục ngoài yêu cầu.
- [ ] `main_document/` phản ánh cấu trúc sau chỉnh sửa.

## 2. Kiểm tra HTML / JavaScript / CSS

- [ ] Không có ID HTML trùng nhau trên từng trang.
- [ ] `node --check` chạy với mọi file JavaScript được sửa.
- [ ] Các selector `querySelector`/`querySelectorAll` còn khớp phần tử HTML.
- [ ] Không có listener hoặc script bị nạp hai lần ngoài chủ ý.
- [ ] Không đưa dữ liệu người dùng vào `innerHTML` không an toàn.
- [ ] Không có CSS override rộng làm ảnh hưởng trang khác.
- [ ] Không bỏ sót media query, reduced-motion hoặc focus-visible.
- [ ] Đổi EN/VI khi form lỗi/thành công, lightbox đang mở, nội dung MySQL vừa nạp và trợ lý đang mở; kiểm tra cả aria-label, alt, title và meta description.
- [ ] Cột dịch MySQL có giá trị sẽ hiển thị đúng theo ngôn ngữ; cột trống dùng nội dung gốc, không sinh bản dịch giả.
- [ ] Database cũ đã sao lưu và chạy `V002_BilingualContent_MySQL.sql` trước khi khởi động backend mới.

## 3. Portfolio `index.html`

- [ ] Home/Hero hiển thị đúng và không bị scale ngoài yêu cầu.
- [ ] Thanh điều hướng sticky hoạt động.
- [ ] Anchor About, Education, Skills, Projects, Contact đến đúng vị trí.
- [ ] Trạng thái section đang xem cập nhật đúng.
- [ ] EN/VI chuyển toàn bộ label, nút, placeholder và nội dung có hỗ trợ.
- [ ] Sáng/tối hoạt động.
- [ ] Các bảng màu vẫn hoạt động, kể cả Rainbow Spectrum nếu đang có.
- [ ] Các section Portfolio ngoài Home vẫn giữ tỷ lệ 75% desktop.
- [ ] Trang chi tiết MoteLona hiển thị ở tỷ lệ tự nhiên khi browser zoom 100%, không có dải trống/tràn ngang.
- [ ] Không có dải trắng hoặc tràn ngang do `zoom`/canvas.
- [ ] Contact form validation và thông báo hoạt động.
- [ ] Dữ liệu tĩnh vẫn hiển thị khi backend offline.

## 4. MoteLona chat

- [ ] Launcher mở chat.
- [ ] Nút Close đóng panel.
- [ ] Nút Settings mở giao diện consent.
- [ ] Header kéo được bằng chuột.
- [ ] Header kéo được bằng cảm ứng/pen nếu thiết bị hỗ trợ.
- [ ] Click Settings/Close không bị hiểu nhầm là kéo.
- [ ] Panel không bị kéo ra ngoài viewport.
- [ ] Grip góc dưới bên phải hiển thị rõ và có thể kéo để đổi chiều rộng/chiều cao.
- [ ] Resize bằng chuột, pen và touch hoạt động; không kéo nhầm nội dung chat.
- [ ] Phím mũi tên trên grip thay đổi kích thước; Shift tăng bước; double-click khôi phục kích thước gần mặc định.
- [ ] Kích thước được giới hạn trong viewport, kể cả sau khi panel đã được di chuyển.
- [ ] Resize cửa sổ trình duyệt không làm panel mất khỏi màn hình hoặc tràn ra ngoài viewport.
- [ ] Ô nhập, gửi câu hỏi, Enter/Shift+Enter hoạt động.
- [ ] Trạng thái backend online/offline hiển thị đúng.
- [ ] Chat EN/VI hoạt động.
- [ ] Tìm nội dung website trả đoạn trích phù hợp.
- [ ] Giải thích đoạn được chọn hoạt động.
- [ ] Điều hướng chỉ chạy tới section trong allow-list.
- [ ] Từ Portfolio mở được trang chi tiết MoteLona; từ trang chi tiết quay về được section Portfolio allow-list.
- [ ] Yêu cầu điều hướng theo chủ đề chọn đúng section, cuộn tới đoạn liên quan và tô vàng cụm có thật trong DOM.
- [ ] Không tìm thấy exact phrase thì không đánh dấu giả; phản hồi nêu rõ nội dung cần tô vàng không tồn tại.
- [ ] History chỉ lưu khi được bật.
- [ ] Memory yêu cầu explicit consent; bật/tắt/xóa đúng.
- [ ] Export và delete history/memory không báo thành công giả.
- [ ] `Tìm trên Internet: ...`, `Search the web for ...` và nút Search web gọi provider DuckDuckGo HTML.
- [ ] Kết quả title/snippet/link được dựng bằng text node; URL chỉ nhận HTTP(S), không đưa nội dung ngoài vào `innerHTML`.
- [ ] Query quá dài, rate limit, timeout, mất Internet và HTML không đọc được đều trả thông báo rõ, không giả lập kết quả.
- [ ] Search web mặc định tắt; giao diện giải thích query chỉ gửi DuckDuckGo khi người dùng chủ động yêu cầu/bật.
- [ ] Health cho biết provider tìm kiếm; `internetSearchEnabled=true` không bị hiểu là bảo đảm server đang có Internet.

## 5. Trang chi tiết `Project/1/motelona.html`

- [ ] Trang mở đúng từ project card.
- [ ] Link quay lại Home và Projects đúng đích.
- [ ] EN/VI hoạt động.
- [ ] Light/dark và bảng màu hoạt động.
- [ ] Tỷ lệ 75% chỉ áp dụng cho các section Portfolio ngoài Home; trang chi tiết MoteLona không bị thu nhỏ theo CSS zoom.
- [ ] Mobile/tablet sử dụng responsive bình thường ở 320px, 360px và 390px.
- [ ] Hero decoration không làm phát sinh scroll ngang toàn trang; section navigation vẫn cuộn ngang độc lập.
- [ ] Ở chiều rộng 561–760px, endpoint rows tự reflow trong card; mã endpoint, mô tả và nút copy không tràn khung.
- [ ] Các section Overview, Features, Architecture, Knowledge, Database, Quality, Scope, Roadmap hiển thị.
- [ ] Các section detail có khoảng cách, tương phản, chiều rộng đọc và thẻ nhất quán trên desktop/tablet/mobile.
- [ ] MoteLona chat điều hướng được tới các section detail trong allow-list.
- [ ] Glossary search có kết quả/no-result state chính xác.
- [ ] Checklist tick/reset và progress được cập nhật.
- [ ] Nút mở chat hoạt động.

## 6. Backend và database (nếu môi trường hỗ trợ)

- [ ] JDK 21 được nhận diện.
- [ ] MySQL Server 8 đang chạy.
- [ ] Database `PortfolioAI_DB` tồn tại.
- [ ] Tệp `.env` nằm trong thư mục backend, không bị đưa vào ZIP công khai.
- [ ] CORS chỉ cho phép các origin cần thiết.
- [ ] `/api/ai/health` trả trạng thái backend/engine đúng.
- [ ] `POST /api/ai/chat` trả response theo DTO hiện tại.
- [ ] `POST /api/ai/explain` trả kết quả theo DTO hiện tại.
- [ ] `POST /api/ai/search` tìm trong website context.
- [ ] Provider có fixture unit test cho tiêu đề, trích đoạn, URL redirect và link không an toàn; test parser không gọi Internet thật.
- [ ] `GET /api/ai/search/web?query=...` trả nguồn thật khi có mạng và 503 khi provider không khả dụng.
- [ ] History/memory được giới hạn theo session và consent.
- [ ] Rate limits còn hiệu lực.
- [ ] `mvnw.cmd clean test` chạy thành công.
- [ ] `VERIFY_MOTELONA.ps1` được chạy khi có cấu hình DB thật.

## 7. Responsive, khả năng truy cập và hiển thị

- [ ] Desktop chiều rộng từ 1440px.
- [ ] Desktop/laptop chiều rộng từ 1025px.
- [ ] Tablet khoảng 768px.
- [ ] Mobile khoảng 390px.
- [ ] Không tràn ngang ngoài chủ ý.
- [ ] Text không bị cắt hoặc đè lên control.
- [ ] Focus bàn phím hiển thị rõ.
- [ ] Escape đóng popover/overlay phù hợp.
- [ ] `prefers-reduced-motion` được tôn trọng.
- [ ] Browser zoom 100% được kiểm tra.
- [ ] Ở browser zoom 100% và CSS viewport height khoảng 620px, `@media (max-height: 760px)` bật compact-height layout.
- [ ] Ở browser zoom 100% và CSS viewport height khoảng 850px, bố cục nhiều hàng thông thường vẫn hoạt động.
- [ ] Thử sát breakpoint: viewport 760 CSS px bật compact mode; 761 CSS px trở về bố cục thường.
- [ ] Trong compact mode, toolsbar và suggestions là hàng ngang có thể cuộn đến nút cuối cùng; không có nút nào bị xóa/ẩn.
- [ ] Mở tính năng tìm kiếm trang; search input và nút Search/Close vẫn truy cập được bằng cuộn ngang; vùng kết quả tìm kiếm cuộn riêng.
- [ ] Messages cuộn dọc độc lập; lời chào và tin nhắn dài không bị chip che/cắt hoặc tràn ngang.
- [ ] Composer, nút gửi và footer vẫn nhìn thấy/hoạt động; không bị suggestions đẩy khỏi panel.
- [ ] Ở viewport ≤500px, lớp compact-height bổ sung không làm biến mất các control quan trọng.
- [ ] Kéo thả MoteLona vẫn hoạt động sau khi áp dụng compact-height CSS; Settings/Close không vô tình kích hoạt kéo.
- [ ] Resize MoteLona vẫn hoạt động ở chiều cao viewport khoảng 620px và 850px, khi search mở và khi panel ở vị trí đã kéo.

## 8. Kiểm tra bàn giao

- [ ] Đã cập nhật `04_UPGRADE_NOTES.md`.
- [ ] Đã cập nhật file tree nếu có thêm/xóa/đổi file.
- [ ] Đã cập nhật bản đồ phụ thuộc nếu thay đổi chức năng/kiến trúc.
- [ ] Đã liệt kê file sửa/thêm/xóa.
- [ ] Đã ghi các test PASS/FAIL/SKIP một cách trung thực.
- [ ] ZIP chứa toàn bộ dự án, không chỉ chứa file thay đổi.
- [ ] ZIP có thể kiểm tra bằng `unzip -t` hoặc công cụ tương đương.
