# MongoDB: dữ liệu được lưu và chạy như thế nào?

Tài liệu này giải thích đường đi của dữ liệu trong dự án bằng các bước đơn giản. Database hiện tại là motelona_portfolio.

## 1. Phân biệt ba khái niệm

- **MongoDB Server** là chương trình chạy nền, giữ dữ liệu trên ổ đĩa và nhận kết nối. Với launcher portable của dự án, file dữ liệu thật nằm ngoài mã nguồn tại `%LOCALAPPDATA%\MongoDB\data\db`. Nếu dùng MongoDB cài dạng Windows Service, vị trí do cấu hình service quyết định.
- **MongoDB Compass** là ứng dụng để xem và sửa dữ liệu. Compass không thay thế Server.
- **Database → collection → document**: database chứa các nhóm dữ liệu (collection); mỗi bản ghi trong collection là một document dạng BSON, gần giống JSON.

Ví dụ: database motelona_portfolio có collection portfolio_projects; mỗi dự án là một document.

## 2. Đường đi khi mở website

~~~mermaid
sequenceDiagram
  participant Browser as Trình duyệt
  participant JS as assets/js/portfolio-data.js
  participant API as PortfolioController
  participant Service as PortfolioService
  participant DB as MongoDB

  Browser->>JS: Mở Portfolio
  JS->>API: GET /api/portfolio/status
  API->>DB: Kiểm tra kết nối
  JS->>API: GET /api/portfolio/public
  API->>Service: Yêu cầu dữ liệu công khai
  Service->>DB: Đọc profile, education, skills, projects
  DB-->>Service: Trả document
  Service-->>JS: Trả JSON
  JS-->>Browser: Hiển thị dữ liệu
~~~

Khi backend hoặc database tắt, portfolio vẫn có nội dung HTML tĩnh để làm bản dự phòng. Khi API hoạt động, portfolio-data.js lấy các bản ghi công khai từ MongoDB.

## 3. Tạo dữ liệu ban đầu

Khi backend khởi động, MongoPortfolioInitializer.java:

1. Tạo các index cần cho truy vấn.
2. Đọc dữ liệu mẫu từ backend/src/main/resources/portfolio-seed.json.
3. Thêm dữ liệu mẫu vào collection còn trống.
4. Không nạp lại collection đã có dữ liệu, để tránh ghi đè nội dung đã sửa trong Compass.

Vì vậy sửa file seed không tự đổi dữ liệu trong database hiện tại. Để sửa nội dung đang dùng, mở document tương ứng trong Compass hoặc viết một migration có chủ đích. Sao lưu trước khi sửa hàng loạt.

## 4. Những collection chính

Mở Compass → localhost:27017 → motelona_portfolio.

| Collection | Nội dung | Khi nào được ghi |
|---|---|---|
| portfolio_profile | Hồ sơ chính; hiện dùng một document có _id là main. | Khởi tạo nếu chưa có hồ sơ. |
| portfolio_education | Các mục học vấn song ngữ. | Seed vào collection trống. |
| portfolio_experience | Hoạt động và kinh nghiệm. | Seed vào collection trống. |
| portfolio_skill_categories | Nhóm kỹ năng; mảng skills nằm trong document nhóm. | Seed vào collection trống. |
| portfolio_projects | Dự án; technologies được nhúng trong từng dự án. | Seed vào collection trống. |
| ai_knowledge_items | Ghi chú bổ sung cho việc tìm kiến thức. | Seed nếu có; seed hiện có thể để danh sách này trống. |
| contact_messages | Tin nhắn gửi từ form liên hệ. | Khi khách gửi form hợp lệ. |
| conversation_sessions | Thông tin phiên chat đã lưu. | Khi người dùng bật lưu lịch sử và gửi chat. |
| conversation_messages | Mỗi lượt người dùng/trợ lý là document riêng, gắn với sessionId. | Cùng điều kiện với conversation_sessions; tối đa 200 tin nhắn mỗi phiên. |
| user_memories | Ghi chú dài hạn được người dùng yêu cầu lưu. | Chỉ khi bật memory, yêu cầu ghi nhớ và đồng ý lưu. |

## 5. Document trông như thế nào?

Một nhóm kỹ năng là một document; các kỹ năng nhỏ nằm trong mảng skills:

~~~json
{
  "_id": "technical",
  "categoryName": "Technical",
  "categoryNameVi": "Kỹ thuật",
  "skills": [
    {
      "_id": "technical-mongodb",
      "skillName": "MongoDB",
      "skillNameVi": "MongoDB",
      "proficiencyLevel": 70,
      "isVisible": true
    }
  ]
}
~~~

Một tin nhắn chat được lưu riêng, không nhét vào một mảng vô hạn:

~~~json
{
  "sessionId": "motelona-session-example",
  "role": "user",
  "content": "Giải thích MongoDB",
  "model": "local",
  "createdAt": "2026-11-24T08:00:00Z"
}
~~~

Các ví dụ trên chỉ minh họa cấu trúc, không phải bản ghi thật của người dùng.

## 6. Khi nào nội dung cá nhân được lưu?

- **Portfolio**: backend đọc nội dung công khai từ các collection portfolio_*.
- **Form liên hệ**: gửi POST /api/portfolio/contact thì backend kiểm tra dữ liệu rồi ghi vào contact_messages.
- **Chat**: mặc định không lưu vào MongoDB. Chỉ lưu khi người dùng bật Lịch sử; backend giới hạn tối đa 200 tin nhắn cho mỗi phiên.
- **Memory**: chỉ lưu khi người dùng bật bộ nhớ, yêu cầu ghi nhớ rõ ràng và chấp thuận. Mật khẩu, API key và secret bị chặn.
- **Mã phiên và lựa chọn giao diện**: giữ trong trình duyệt để nhận diện phiên và trạng thái bật/tắt; chúng không phải tài khoản đăng nhập.

Các API đọc/xóa lịch sử và memory nằm trong MemoryController.java. Không có trang quản trị Portfolio để sửa hồ sơ; dữ liệu hiện tại có thể xem/sửa bằng Compass.

## 7. Tìm đúng file để sửa dữ liệu

| Muốn thay đổi | Bắt đầu ở |
|---|---|
| Dữ liệu mặc định cho database mới | backend/src/main/resources/portfolio-seed.json |
| Cách tạo index và nạp dữ liệu ban đầu | backend/src/main/java/com/motelona/assistant/config/MongoPortfolioInitializer.java |
| API đọc Portfolio, lưu liên hệ và lịch sử | backend/src/main/java/com/motelona/assistant/service/PortfolioService.java |
| Lưu/xóa memory | backend/src/main/java/com/motelona/assistant/memory/MemoryService.java |
| Endpoint nhận request | backend/src/main/java/com/motelona/assistant/controller/ |
| Cách website lấy dữ liệu Portfolio | assets/js/portfolio-data.js |

Khi thêm một trường mới, thường cần cập nhật document/seed, API đọc trường đó, giao diện hiển thị và test tương ứng.

## 8. Dùng database riêng cho dự án tiếp theo

Giữ MongoDB Server nhưng đổi tên database trong MONGODB_URI, ví dụ:

~~~text
mongodb://127.0.0.1:27017/inventory_app
~~~

Database mới sẽ được tạo khi ứng dụng ghi document đầu tiên. Không dùng chung database motelona_portfolio hoặc database test cho một dự án khác. Khi dùng Atlas hoặc bật xác thực, giữ username/password trong biến môi trường, không đặt trong HTML hay JavaScript.

