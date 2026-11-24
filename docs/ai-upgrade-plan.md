# Kế hoạch nâng cấp AI MoteLona

## Mục tiêu

Cải thiện trải nghiệm hỏi đáp và tăng số câu hỏi được giải quyết nhanh mà không gọi API miễn phí khi không cần. Khi phải gọi model, giới hạn thời gian chờ, số lượt thử và độ dài câu trả lời để một câu hỏi không giữ người dùng chờ lâu.

## Căn cứ

- Trước khi sửa, `docs/ai-architecture.md` mô tả luồng `ScopeGuard` → tra cứu cục bộ → model router và fallback cục bộ. Mã lúc đó cho phép mỗi provider chờ tối đa 20 giây, đầu ra mặc định 512 token và giao diện chờ tối đa 70 giây cho chat hoặc 130 giây khi bật tìm web.
- `docs/Motelona_AI_video_review.docx` tổng hợp clip về trả lời ngắn trước, cache câu hỏi lặp lại phù hợp, giới hạn quyền, dự phòng provider và theo dõi chất lượng.
- Nhu cầu người dùng: bổ sung câu trả lời và cải thiện trải nghiệm trong khi tránh để API free chạy lâu.

Đây là căn cứ để rà soát code; chi tiết thực tế phải được xác nhận trong mã trước khi sửa.

## Hướng thay đổi

### 1. Trả lời cục bộ cho câu hỏi có đáp án ổn định

- Mở rộng FAQ/intent cho các câu hỏi người dùng thường hỏi về MoteLona, portfolio, dự án và cách sử dụng trợ lý.
- Ưu tiên trả lời từ dữ liệu cục bộ đã có bằng chứng; hỏi lại khi câu hỏi thiếu thông tin; không đoán khi không tìm thấy dữ liệu.
- Chỉ gọi model khi cần tổng hợp, diễn giải hoặc câu hỏi không nằm trong bộ trả lời cục bộ.

### 2. Giới hạn ngân sách của một lượt chat

- Đặt một deadline tổng cho cả request, không cộng dồn timeout tối đa cho từng provider. Mục tiêu triển khai: tối đa 12 giây cho chuỗi model, từng provider tối đa 7 giây hoặc phần ngân sách còn lại.
- Mỗi provider chỉ thử một lần trong request; chuyển tiếp khi provider lỗi và còn đủ thời gian, không retry một provider vô hạn.
- Giữ đầu ra ngắn mặc định; chỉ cấp giới hạn cao hơn cho yêu cầu thật sự cần liệt kê/giải thích.
- Giữ tìm web như hành động do người dùng chủ động bật; áp dụng ngân sách riêng có thể nhìn thấy được cho lượt tìm web.

### 3. Phản hồi dễ hiểu khi chậm hoặc hết quota

- Giao diện có trạng thái chờ rõ ràng, hiển thị lỗi/quota bằng lời dễ hiểu và gợi ý cách hỏi tiếp.
- Nếu model không phản hồi kịp, trả về câu trả lời cục bộ hữu ích hoặc thông báo ngắn kèm lựa chọn thử lại sau.
- Giữ các câu trả lời và điều hướng trong phạm vi MoteLona; không để model tạo đích điều hướng tùy ý.

### 4. Câu trả lời lặp lại và giám sát có giới hạn

- Ưu tiên FAQ cục bộ cho câu hỏi lặp lại để không gọi API; chỉ cân nhắc cache khi nội dung công khai, ổn định và không phụ thuộc hồ sơ/ngữ cảnh riêng của người dùng.
- Theo dõi thời gian phản hồi, provider lỗi/timeout và tỷ lệ giải quyết cục bộ; không lưu văn bản chat nhạy cảm vào log.
- Dùng các số đo để điều chỉnh câu trả lời mẫu, giới hạn và trải nghiệm chờ.

## Tiêu chí hoàn thành

- Câu hỏi thường gặp có bằng chứng được trả lời cục bộ, không gọi provider.
- Câu hỏi cần model dừng trong ngân sách tổng đã chọn; số lần thử có giới hạn và kết thúc bằng fallback rõ ràng.
- Câu trả lời mặc định ngắn, có ích và có cách hỏi tiếp; câu hỏi thiếu căn cứ không bị bịa.
- Tìm web chỉ chạy khi người dùng yêu cầu; nguồn được giữ lại nếu luồng hiện tại hỗ trợ trích dẫn.
- Không đưa API key ra frontend và không thêm lưu trữ nội dung chat không cần thiết.

## Thứ tự triển khai

1. Xác nhận cấu trúc hiện tại trong `AiChatService`, `ModelProviderRouter`, `LocalAiEngine`, `ModelPromptBuilder` và widget frontend.
2. Cải thiện bộ trả lời cục bộ và nội dung phản hồi ngắn.
3. Áp dụng deadline tổng, giới hạn provider/retry và fallback gọn.
4. Cải thiện trạng thái chờ/lỗi trong giao diện.
5. Đọc lại thay đổi để bảo đảm phù hợp kiến trúc và yêu cầu.

## Giới hạn

Kế hoạch này không đổi nhà cung cấp, không thêm database/vector store hay local LLM, không đổi quy tắc bảo mật và không xác nhận quota hiện thời của các dịch vụ. Quota/model free thay đổi theo nhà cung cấp và cần kiểm tra nguồn chính thức trước khi dựa vào con số cụ thể.

## Cập nhật sau triển khai

- Đã thêm câu trả lời cục bộ về cách dùng MoteLona, quyền riêng tư của chat và xử lý khi API free chậm/hết quota; thêm ba câu hỏi gợi ý song ngữ ở trạng thái chào mừng.
- Đã đặt ngân sách tổng cho chuỗi model là 12 giây, tối đa 7 giây cho mỗi provider và một lần thử/provider; nếu hết thời gian, chat quay về trả lời cục bộ khi có.
- Đã giảm giới hạn đầu ra xuống 384 token mặc định, 768 cho câu dài và 1.024 cho câu hỏi cần liệt kê/giải thích/tìm web; giao diện báo chờ sớm hơn và dừng chat thường sau 25 giây.
- Đã ghi lại kiến trúc và giới hạn API đang công bố trong `docs/ai-architecture.md`. Tìm web vẫn chỉ chạy khi người dùng chủ động yêu cầu và có thời gian chờ riêng.
- Chưa thêm cache câu trả lời model vì prompt có thể chứa dữ liệu riêng theo phiên; FAQ cục bộ xử lý câu lặp lại trước. Log chỉ ghi trạng thái/provider và thời gian, chưa có bảng theo dõi tổng hợp tỷ lệ giải quyết cục bộ.
- Đã đồng bộ nội dung song ngữ trên `Project/1/motelona.html` với luồng thực tế: FAQ cục bộ, giới hạn provider 12/7 giây, privacy/fallback và lộ trình tiếp theo.
- Phần dữ liệu Portfolio trên trang chi tiết đọc số lượng bản ghi từ API; endpoint công khai thêm `knowledgeCount` thay vì công khai nội dung tri thức.
- FAQ trong widget có thêm câu trả lời cục bộ về nội dung/số lượng dữ liệu MongoDB, lấy số đếm trực tiếp từ AI context; có thêm nút gợi ý câu hỏi tương ứng.
- Đã cập nhật nội dung dự phòng song ngữ trên trang chủ; khi API có dữ liệu, thẻ MoteLona được đồng bộ mô tả và công nghệ từ database.
- Đã đồng bộ mô tả song ngữ và danh sách công nghệ trong `portfolio-seed.json` cùng record MoteLona hiện có trong MongoDB.
