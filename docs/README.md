# Học dự án MoteLona

Nếu bạn mới mở dự án, hãy đọc theo thứ tự này. Không cần hiểu hết Java hay MongoDB trước khi bắt đầu.

## Lộ trình đọc

1. [MongoDB: dữ liệu được lưu ra sao](mongodb-data-guide.md) — xem dữ liệu đi từ trình duyệt vào database và ngược lại. Đây là tài liệu nên đọc đầu tiên nếu bạn đang thắc mắc về lưu trữ.
2. [Sơ đồ thư mục](project-map.md) — tìm HTML, JavaScript, Java và cấu hình.
3. [Cài đặt và chạy](setup-and-run.md) — mở website, backend và MongoDB trên Windows.
4. [Kiến trúc trợ lý AI](ai-architecture.md) — theo dõi một câu hỏi từ lúc gửi đến lúc nhận câu trả lời.
5. [Tổng hợp video AI](Motelona_AI_video_review.docx) — các lớp xây agent trong video và hướng áp dụng sơ bộ cho MoteLona.
6. [Kế hoạch nâng cấp AI](ai-upgrade-plan.md) — phạm vi thay đổi, tiêu chí hoàn thành và thứ tự triển khai.

## Bản đồ hoạt động

~~~mermaid
flowchart LR
  Browser[Trình duyệt]
  Page[Portfolio HTML CSS JS]
  Widget[Widget MoteLona]
  API[Backend Java Spring Boot]
  Mongo[(MongoDB)]
  Models[Gemini Groq OpenRouter]

  Browser --> Page
  Browser --> Widget
  Page -->|GET Portfolio| API
  Widget -->|Chat, history, memory| API
  API -->|Đọc và ghi| Mongo
  API -->|Câu hỏi và bằng chứng| Models
~~~

Portfolio được đọc từ MongoDB khi backend hoạt động. File portfolio-seed.json chỉ cung cấp dữ liệu ban đầu cho database trống. Khi API không kết nối được, website còn nội dung HTML làm bản dự phòng.

## Ba nơi cần nhớ

- Giao diện Portfolio: index.html và assets/.
- Giao diện trợ lý: Project/1/motelona-ai/frontend/.
- Đọc/ghi dữ liệu: Project/1/motelona-ai/backend/src/main/java/ và MongoDB.

Mỗi dự án nên dùng database riêng. Cài MongoDB Compass không tự cài hoặc khởi động MongoDB Server.

