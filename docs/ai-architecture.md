# Kiến trúc AI MoteLona, model và Java

Xem [hướng dẫn chạy](setup-and-run.md), [sơ đồ thư mục](project-map.md) và [cách dữ liệu được lưu trong MongoDB](mongodb-data-guide.md).

## 1. Luồng xử lý chat

~~~mermaid
flowchart LR
    U[Người dùng] --> FE[motelona.js trong trình duyệt]
    FE -->|POST /api/ai/chat hoặc /explain| API[AiController]
    API --> ORCH[AiChatService]
    ORCH --> PAGE[Ngữ cảnh trang gửi từ browser]
    ORCH --> DB[PortfolioService / MongoDB]
    ORCH --> FILES[ProjectFileSearchService / allow-list]
    ORCH --> SEARCH[LocalSearchService / TF-IDF]
    ORCH --> GUARD[ScopeGuard]
    GUARD -->|Ngoài phạm vi| BLOCK[Trả lời nhắc phạm vi]
    GUARD -->|Trong phạm vi hoặc đã bật web| FASTCHECK[Kiểm tra FAQ và bằng chứng cục bộ]
    FASTCHECK -->|Đủ bằng chứng; không bật web| FAST[LocalAiEngine.quickAnswer]
    FASTCHECK -->|Cần tổng hợp; không bật web| ROUTER[ModelProviderRouter]
    FASTCHECK -->|Bật Tìm web| WEB[WebResearchService]
    WEB --> DDG[DuckDuckGo HTML + trang công khai]
    DDG --> ROUTER
    ROUTER -->|1| GEM[GeminiModelService]
    GEM -->|Lỗi/quota| GROQ[GroqModelService]
    GROQ -->|Lỗi/quota| OR[OpenRouterModelService]
    OR -->|Tất cả lỗi| LOCAL[LocalAiEngine + Glossary]
    ROUTER -->|Safety refusal: dừng| REFUSE[Không chuyển provider]
    LOCAL --> NLP[Intent + TF-IDF/cosine + luật]
    ORCH --> MEMORY[Memory/history theo consent]
    MEMORY --> DB
    ROUTER --> OUT[ChatResponse + nguồn nếu có]
    BLOCK --> OUT
    FAST --> OUT
    REFUSE --> OUT
    LOCAL --> OUT
    ORCH -->|Yêu cầu điều hướng rõ| ACTION[ActionRouter: đích được cho phép]
    OUT --> FE
    ACTION --> FE
    FE --> UI[Hiển thị câu trả lời, điều hướng/tô sáng]
~~~

Với câu hỏi nội dung thông thường, backend kiểm tra phạm vi trước; sau khi được phép mới tìm trong dữ liệu Portfolio, context trang và file dự án được allow-list. FAQ về hồ sơ, kỹ năng, dự án, học vấn, kinh nghiệm, liên hệ và thuật ngữ sẽ được trả lời cục bộ nếu có bằng chứng đủ phù hợp; nếu không, câu hỏi đi tiếp tới model provider. Bật Tìm web sẽ bỏ qua nhánh trả lời nhanh để xử lý nguồn công khai theo yêu cầu. Kết quả tìm thấy tình cờ không thể tự mở khóa câu ngoài chủ đề. Lời chào, yêu cầu ghi/xóa memory và navigation tường minh có thể được xử lý riêng. Thông tin cá nhân/dự án luôn phải dựa trên evidence được gửi vào prompt.

### Các lớp chính

| Lớp | Vai trò |
|---|---|
| <code>AiController</code> | REST endpoint, request/response và rate limit. |
| <code>AiChatService</code> | Điều phối nghiệp vụ chat: tìm context, web tùy chọn, consent, chọn provider và archive history. |
| <code>ScopeGuard</code> | Chặn prompt ngoài phạm vi website/dự án trước khi gửi ra dịch vụ model; cho phép nếu người dùng chủ động bật tìm web. |
| <code>ModelPromptBuilder</code> | Tạo cùng một prompt có giới hạn cho mọi provider. |
| <code>ModelProviderRouter</code> | Thử lần lượt Gemini, Groq, OpenRouter trong ngân sách tổng 12 giây; mỗi provider chỉ thử một lần và mỗi lượt tối đa 7 giây. Khi hết ngân sách hoặc provider lỗi thì quay về local fallback. Không chuyển một safety refusal sang provider khác. |
| <code>PortfolioService</code> | Truy vấn Portfolio/knowledge documents trong MongoDB, thêm số lượng bản ghi công khai vào AI context, lưu contact và lịch sử chat đã được cho phép. |
| <code>LocalSearchService</code> | Tìm passage liên quan trong text website/Portfolio. |
| <code>ProjectFileSearchService</code> | Tìm tài liệu/source nằm trong allow-list, có giới hạn file và cache. |
| <code>WebResearchService</code> | Dùng provider tìm kiếm và đọc trang công khai khi được yêu cầu. |
| <code>GeminiModelService</code> | Gửi evidence và câu hỏi đến Gemini generateContent. |
| <code>GroqModelService</code>, <code>OpenRouterModelService</code> | Provider dự phòng dùng giao thức chat completions tương thích OpenAI; chỉ chạy khi có key tương ứng. |
| <code>LocalAiEngine</code> | Trả lời cục bộ bằng greeting, FAQ, glossary, intent và passage đã truy xuất; chỉ dùng fast path khi có bằng chứng phù hợp. |
| <code>MemoryService</code> | CRUD memory gắn với session, chỉ lưu khi có yêu cầu và consent. |
| <code>ActionRouter</code> | Nhận yêu cầu điều hướng rõ ràng và ánh xạ sang section/page cố định. |

## 2. Model thực tế

- Provider chính là Gemini qua HTTPS; mặc định model là <code>gemini-3.8-flash</code>. Có thể cấu hình <code>GEMINI_MODEL</code> và <code>GEMINI_API_ENDPOINT</code>.
- Provider dự phòng theo thứ tự: Groq, rồi OpenRouter. Đặt <code>GROQ_API_KEY</code> / <code>OPENROUTER_API_KEY</code> mới bật từng provider; <code>OPENROUTER_MODEL=openrouter/free</code> dùng bộ định tuyến các model miễn phí và chất lượng/model có thể thay đổi.
- Khóa chỉ nằm ở backend: <code>GEMINI_API_KEY</code>, <code>GROQ_API_KEY</code>, <code>OPENROUTER_API_KEY</code>. Không luân phiên nhiều key trong cùng một dịch vụ để lách quota.
- <code>ModelPromptBuilder</code> đóng gói system instruction, câu hỏi mới nhất và evidence có giới hạn chiều dài. Gemini dùng <code>generateContent</code>; Groq/OpenRouter dùng chat completions tương thích OpenAI.
- Câu hỏi FAQ có bằng chứng phù hợp được trả lời bằng nguồn Portfolio/tài liệu cục bộ trước khi gọi hosted model. Những câu hỏi cần tổng hợp sẽ tiếp tục sang model; bật Tìm web vẫn giữ nguyên hành vi tra cứu và trích dẫn nguồn.
- Router dành tối đa 12 giây cho toàn bộ chuỗi model; mỗi provider được cấp tối đa 7 giây hoặc phần ngân sách còn lại, tùy giá trị nào thấp hơn. Giao diện cập nhật trạng thái chờ sau 3,5 và 9 giây, rồi dừng chat thường sau 25 giây hoặc lượt bật tìm web sau 100 giây.
- Đầu ra mặc định tối đa 384 token; câu hỏi dài dùng tối đa 768 token; liệt kê dự án, giải thích văn bản được chọn và câu hỏi có nguồn web dùng tối đa 1.024 token. System prompt yêu cầu đặt đáp án trước, giữ phản hồi gọn và chỉ hỏi một câu làm rõ khi cần.
- Khi API trả lỗi, hết quota, timeout hoặc model chưa được cấp quyền, router thử provider kế tiếp trong cùng ngân sách. Nếu hết ngân sách hoặc mọi provider lỗi, <code>LocalAiEngine</code> tra cứu cục bộ. Safety refusal không được chuyển sang provider khác.
- Ngoài FAQ Portfolio, <code>LocalAiEngine</code> trả lời cục bộ về cách dùng MoteLona, cách xử lý/lưu dữ liệu chat và hành vi khi provider lỗi hoặc hết hạn mức. Widget hiển thị ba câu hỏi gợi ý tương ứng khi mở chat; các câu gợi ý được ẩn sau khi người dùng bắt đầu chat.
- Không có local LLM, Ollama, file model, inference server, training/fine-tuning, embedding model hoặc vector database trong workspace.
- Không có API key nào thì chạy local fallback: quy tắc, từ điển song ngữ và truy xuất text. Fallback không tạo văn bản tổng quát như LLM.
- Hạn mức miễn phí và model khả dụng do từng nhà cung cấp quyết định, có thể đổi; OpenRouter <code>openrouter/free</code> có giới hạn thấp và lựa chọn model thay đổi. Không xem chuỗi dự phòng là đảm bảo uptime.
- Các provider đều nhận câu hỏi và evidence liên quan khi đến lượt được gọi. Kiểm tra chính sách lưu/training của từng tài khoản trước khi gửi nội dung riêng tư; không nhập secret vào chat.

### Free tier và kỳ vọng chất lượng (kiểm tra 24/11/2026)

- **Gemini:** giới hạn được áp dụng theo project và model, theo RPM/TPM/RPD; vượt ngưỡng trả lỗi giới hạn. Google khuyến nghị giảm context hoặc độ dài đầu ra để giảm yêu cầu tốn tài nguyên. Hạn mức thực tế xem trong AI Studio; model cụ thể phải được bật cho API key trước khi chạy.
- **Groq:** giới hạn gồm RPM/RPD/TPM/TPD và có thể có giới hạn input/output riêng; quota chính xác phụ thuộc model và tổ chức, xem trong Groq Console. Vượt ngưỡng trả HTTP 429. Không suy ra quota Free từ bảng Developer.
- **OpenRouter:** bộ chọn <code>openrouter/free</code> định tuyến tới model miễn phí đang phù hợp. Trang chính thức hiện ghi biến thể miễn phí có giới hạn 20 request/phút và 50 request/ngày (mức này có thể đổi); yêu cầu lỗi vẫn có thể tính vào hạn mức theo ngày. Model, chất lượng và độ trễ có thể thay đổi.
- **Chất lượng:** “miễn phí” không tự động có nghĩa là trả lời kém, nhưng model dự phòng có thể khác Gemini về tiếng Việt, lập luận, giới hạn context, tốc độ và mức ổn định. Nên thử với các câu hỏi thật của portfolio trước khi công bố rộng. Nếu cả provider hosted đều lỗi/hết quota, ứng dụng dùng tìm kiếm cục bộ có sẵn; phần đó không có năng lực sinh văn bản tổng quát như LLM.
- Nguồn chính thức đã kiểm tra: [Gemini rate limits](https://ai.google.dev/gemini-api/docs/rate-limits), [Groq rate limits](https://console.groq.com/docs/rate-limits), [OpenRouter free models](https://openrouter.ai/collections/free-models). Xem hạn mức trực tiếp trong tài khoản trước khi vận hành vì mức Free có thể đổi.

### Tìm và xếp hạng bằng chứng

<code>TextVectorizer</code> chuẩn hóa tiếng Việt bằng cách bỏ dấu, tách token, loại stop words và thêm một số bigram. <code>LocalSearchService</code> kết hợp cosine similarity TF-IDF và từ giao nhau để xếp hạng đoạn.

<code>IntentClassifier</code> dùng các cụm từ rõ và độ gần với ví dụ. <code>KnowledgeRetrievalService</code> dùng intent để bổ sung từ khóa và xếp hạng passage cho local fallback. Cơ chế này là tìm kiếm văn bản theo từ, không phải semantic embedding.

<code>ProjectFileSearchService</code> xét README, index, trang chi tiết, <code>docs/</code>, <code>Project/1/motelona-ai/</code> và <code>tools/</code>. Một số thư mục tùy chọn có thể không tồn tại. Giới hạn hiện tại: 1000 file, 300 KB/file, 10 MB corpus, cache 5 phút. Dịch vụ loại <code>.env*</code>, tên file chứa secret/credential/password và thư mục build/dependencies.

### Tìm web, memory và điều hướng

- Tìm web qua HTML công khai DuckDuckGo, không cần search key; chỉ gọi khi user bật công tắc hoặc yêu cầu rõ. Lấy tối đa 8 kết quả rồi đọc tối đa 5 trang nguồn công khai; model nhận nguồn và số thứ tự để trích dẫn.
- Lịch sử trong collection <code>conversation_messages</code> chỉ lưu nếu bật và giữ tối đa 200 message/session. Memory trong <code>user_memories</code> chỉ lưu khi user bật memory và yêu cầu ghi nhớ rõ; secret/password/API key bị chặn.
- Session ID tách dữ liệu giữa các browser session nhưng không phải xác thực người dùng.
- <code>ActionRouter</code> chỉ chọn các section/page đã biết. Frontend cuộn tới đích và highlight cụm văn bản; model không trả URL điều hướng tùy ý.

## 3. Endpoint

| Method | Đường dẫn | Mục đích |
|---|---|---|
| GET | <code>/api/ai/health</code> | Trạng thái app, model/key và tìm web; có key không đồng nghĩa key đã được model chấp nhận. |
| POST | <code>/api/ai/chat</code> | Hỏi đáp theo page context, session và consent. |
| POST | <code>/api/ai/explain</code> | Giải thích văn bản được chọn. |
| POST | <code>/api/ai/search</code> | Tìm trong website context được gửi lên. |
| GET | <code>/api/ai/search/web?query=...</code> | Truy vấn kết quả tìm web. |
| GET/POST/DELETE | <code>/api/ai/memories</code> | Liệt kê, thêm, xóa memory theo session. |
| GET/DELETE | <code>/api/ai/history/{sessionId}</code> | Đọc/xóa lịch sử đã bật. |
| GET | <code>/api/ai/export/{sessionId}</code> | Xuất history và memory theo session. |
| GET | <code>/api/portfolio/status</code> | Trạng thái kết nối MongoDB và tên database đang dùng. |
| GET | <code>/api/portfolio/public</code> | Đọc dữ liệu Portfolio công khai và số lượng mục tri thức AI đang hoạt động. Nội dung tri thức không công khai qua endpoint này. |
| POST | <code>/api/portfolio/contact</code> | Lưu form liên hệ. |

## 4. Java trong dự án

- **Java/JVM:** ngôn ngữ kiểu tĩnh, chạy trên Java Virtual Machine. POM đặt <code>java.version</code> là 21.
- **Spring Boot:** tự cấu hình ứng dụng; <code>@SpringBootApplication</code> là điểm vào và component scan.
- **Controller/service:** controller xử lý HTTP; service giữ nghiệp vụ và thao tác MongoDB, tránh trộn logic AI vào endpoint.
- **Dependency injection:** Spring cấp service qua constructor, quản lý vòng đời bean.
- **DTO/record:** định nghĩa schema request/response JSON và các dữ liệu chuyển giữa lớp.
- **Spring Data MongoDB:** <code>MongoTemplate</code> đọc và ghi BSON document qua collection; truy vấn được dựng bằng <code>Query</code>/<code>Criteria</code>.
- **HTTP/JSON:** Java <code>HttpClient</code> gọi Gemini; Jackson tạo request và đọc response.
- **Ngoại lệ:** <code>ApiExceptionHandler</code> chuyển input error thành HTTP 400, rate limit thành 429, dịch vụ AI/search thành 503 và lỗi bất ngờ thành phản hồi tổng quát.
- **Cấu hình:** <code>application.yml</code> có mặc định; biến môi trường <code>MONGODB_URI</code> chọn máy chủ và database. Các biến khác cấu hình model, CORS và project root.
- **Maven:** <code>pom.xml</code> quản lý dependencies/compile/test. Tại thư mục backend chạy <code>mvn test</code>. Workspace không có <code>mvnw.cmd</code> hoặc Maven wrapper JAR.

## 5. MongoDB trong dự án và cách tái sử dụng

- **Database** là vùng dữ liệu của một ứng dụng. Mặc định dùng <code>motelona_portfolio</code>; dự án sau nên có database riêng như <code>inventory_app</code> qua URI riêng. Không chia sẻ một database `test` giữa các app.
- **Collection** tương tự nhóm dữ liệu, nhưng mỗi record là document BSON. Các tên hiện tại: <code>portfolio_profile</code>, <code>portfolio_education</code>, <code>portfolio_experience</code>, <code>portfolio_skill_categories</code>, <code>portfolio_projects</code>, <code>contact_messages</code>, <code>conversation_sessions</code>, <code>conversation_messages</code>, <code>user_memories</code> và <code>ai_knowledge_items</code>.
- **Nhúng hay tham chiếu:** nhóm kỹ năng được nhúng trong category và technology được nhúng trong project vì thường đọc cùng nhau, danh sách nhỏ. History dùng một document cho mỗi message vì có thể tăng theo thời gian; backend chỉ giữ 200 message/session. MongoDB giới hạn một BSON document tối đa 16 MiB.
- **Index:** <code>MongoPortfolioInitializer</code> tạo index theo thứ tự hiển thị, trạng thái công khai, slug duy nhất, thời gian nhận liên hệ và session/thời gian của history/memory. Thêm index dựa trên truy vấn thực tế, vì mỗi index cũng tiêu tốn dung lượng và chi phí ghi.
- **Seed:** <code>portfolio-seed.json</code> là nguồn dữ liệu khởi tạo song ngữ. Seed chỉ thêm khi collection đang trống nên không ghi đè nội dung được chỉnh trong Compass.
- **Compass:** kết nối tới MongoDB Server để xem/sửa database. Compass không thay thế Server và không tự tạo database khi chưa có dữ liệu.
- **Spring Data:** <code>MongoTemplate</code> được inject qua constructor. Đặt truy vấn và thao tác dữ liệu trong service, kiểm tra input ở service/controller, rồi bổ sung test khi đổi shape/index.
- **Bảo mật:** đặt URI/mật khẩu trong biến môi trường hoặc secret manager. Database dùng auth cần user có quyền tối thiểu cho đúng database; encode ký tự đặc biệt trong URI.

### Dữ liệu công khai hiện có

Đã đọc endpoint Portfolio và kiểm tra collection tại MongoDB `motelona_portfolio` ngày 24/11/2026. Backend cục bộ báo kết nối thành công. Ảnh chụp lúc kiểm tra có một hồ sơ, một bản ghi học vấn, một hoạt động tự học, 13 kỹ năng, một dự án MoteLona đã công khai và không có mục tri thức AI đang hoạt động. Các con số có thể thay đổi khi dữ liệu được cập nhật; trang chi tiết gọi `/api/portfolio/public` để làm mới số liệu. Endpoint chỉ trả số lượng mục tri thức, không công khai nội dung của chúng.

AI context cũng có một dòng đếm hồ sơ, học vấn, hoạt động, kỹ năng, dự án và mục tri thức đang hoạt động. FAQ về dữ liệu database có thể trả lời cục bộ từ dòng này, không cần gọi model.

Mô tả dự án MoteLona trong collection `portfolio_projects` cần giữ đồng bộ với `portfolio-seed.json` và trang `Project/1/motelona.html`. Seed chỉ chạy trên collection trống nên sửa file seed không cập nhật record đã tồn tại.

Khi thêm tính năng, cập nhật DTO/controller/service, lựa chọn document shape theo cách đọc/ghi dữ liệu, tạo index phù hợp trong initializer, cập nhật seed nếu cần và thêm test contract.

## 6. Ghi chú vận hành

- CORS chỉ cho phép các origin tường minh; wildcard bị từ chối.
- Rate limit hiện chạy trong memory của một backend instance, reset khi restart, không dùng Redis hoặc <code>ai_api_daily_usage</code>.
- Không có đăng nhập/quyền admin trong API. Không dùng API công khai hiện tại làm bảng quản trị.
- Spring Boot cần MongoDB Server hoạt động trước; collection/database được tạo khi ghi seed đầu tiên. Xem [hướng dẫn MongoDB](mongodb-data-guide.md).
- MongoDB là database duy nhất mà backend sử dụng. Xem [hướng dẫn lưu trữ](mongodb-data-guide.md) để biết các collection ứng dụng và luồng đọc/ghi.
- Ghi kết quả test và chạy thực theo kết quả mới nhất sau khi hoàn tất thay đổi; test đơn vị không chứng minh MongoDB Server đã chạy.
