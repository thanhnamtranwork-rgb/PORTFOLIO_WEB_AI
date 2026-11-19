(function () {
  "use strict";
  if (window.__motelonaInitialized) return;
  window.__motelonaInitialized = true;

  const API_BASE = String(window.MOTELONA_API_BASE || "").trim().replace(/\/$/, "");
  const API_CONFIGURED = API_BASE.length > 0;
  const scriptUrl = document.currentScript && document.currentScript.src ? document.currentScript.src : "";
  const root = document.querySelector("[data-motelona-ai-root]");
  if (!root) return;
  // Inject the new stylesheet into <head>; the only HTML source change remains inside Featured Projects.
  const styleHref = scriptUrl ? new URL("motelona.css?v=20261118-agent", scriptUrl).href : "./motelona-ai/frontend/motelona.css?v=20261118-agent";
  if (!document.querySelector('link[data-motelona-stylesheet="true"]')) {
    const stylesheet = document.createElement("link"); stylesheet.rel = "stylesheet"; stylesheet.href = styleHref; stylesheet.dataset.motelonaStylesheet = "true"; document.head.appendChild(stylesheet);
  }
  // Move only the assistant mount point to body so fixed positioning is viewport-wide, even if the project section animates.
  // Copy the active theme tokens and observe theme class changes so all three palette variants remain synchronized.
  const themeContainer = document.querySelector(".animfolio-container");
  if (themeContainer && themeContainer.classList.contains("motelona-detail-root")) {
    root.classList.add("motelona-ai-root--detail");
  }
  const themeVariables = ["--animfolio-accent","--animfolio-accent-hover","--animfolio-accent-light","--animfolio-secondary","--animfolio-secondary-hover","--animfolio-secondary-light","--animfolio-on-accent","--animfolio-on-secondary","--animfolio-gradient","--animfolio-bg","--animfolio-bg-secondary","--animfolio-surface","--animfolio-surface-hover","--animfolio-accent-text","--animfolio-text","--animfolio-text-secondary","--animfolio-text-muted","--animfolio-border","--animfolio-font-body","--animfolio-font-heading","--animfolio-card-radius"];
  function syncThemeVariables() {
    if (!themeContainer) return;
    const computed = window.getComputedStyle(themeContainer);
    themeVariables.forEach(name => { const value=computed.getPropertyValue(name).trim(); if(value) root.style.setProperty(name,value); });
    const activePalette = themeContainer.getAttribute("data-portfolio-theme") || "tree";
    const assistantAccent = activePalette === "peach" ? "#B8426D" : (activePalette === "apricot" ? "#986005" : (computed.getPropertyValue("--animfolio-accent-hover").trim() || "#1D6948"));
    root.style.setProperty("--motelona-accent", assistantAccent);
    root.style.setProperty("--animfolio-on-accent", "#ffffff");

    // The project-detail page uses --ml-* tokens instead of the portfolio tokens.
    // Bridge them here so its floating launcher follows the same selected palette.
    if (themeContainer.classList.contains("motelona-detail-root")) {
      const bridges = {
        "--animfolio-accent": "--ml-accent",
        "--animfolio-accent-hover": "--ml-accent-hover",
        "--animfolio-secondary": "--ml-secondary",
        "--animfolio-accent-rgb": "--ml-accent-rgb",
        "--animfolio-secondary-rgb": "--ml-secondary-rgb"
      };
      Object.entries(bridges).forEach(([target, source]) => {
        const value = computed.getPropertyValue(source).trim();
        if (value) root.style.setProperty(target, value);
      });
      root.style.setProperty("--animfolio-on-accent", "#ffffff");
      root.style.setProperty("--animfolio-bg", computed.getPropertyValue("--ml-bg").trim() || "#121b17");
      root.style.setProperty("--animfolio-bg-secondary", computed.getPropertyValue("--ml-surface").trim() || "#1a2821");
      root.style.setProperty("--animfolio-surface", computed.getPropertyValue("--ml-surface").trim() || "#1a2821");
      root.style.setProperty("--animfolio-text", computed.getPropertyValue("--ml-text").trim() || "#edf3ef");
      root.style.setProperty("--animfolio-text-secondary", computed.getPropertyValue("--ml-muted").trim() || "#b6c4bc");
      root.style.setProperty("--animfolio-border", computed.getPropertyValue("--ml-border").trim() || "rgba(237,243,239,.12)");
    }
  }
  if (themeContainer) {
    syncThemeVariables();
    if (root.parentElement !== document.body) document.body.appendChild(root);
    if (window.MutationObserver) new MutationObserver(syncThemeVariables).observe(themeContainer,{attributes:true,attributeFilter:["class","data-animfolio-scheme","data-portfolio-theme","style"]});
  }
  const dict = {
    en: {
      openAssistant:"MoteLona", openAssistantLabel:"Open MoteLona assistant", close:"Close assistant", title:"MoteLona", subtitle:"AI website assistant", resizePanel:"Drag the corner to resize",
      statusChecking:"Checking Java backend…", statusOnline:"Backend reachable · self-built local NLP engine ready", statusOffline:"Backend unreachable · check the Java service", statusConfigure:"Public backend URL is not configured for this hosted website", backendNotConfigured:"The chat interface is ready, but no Java backend URL is configured. Set window.MOTELONA_API_BASE to your deployed HTTPS backend URL to enable real replies.",
      welcome:"Hi! I’m MoteLona. Ask about this portfolio, request an explanation, or ask me to open the relevant page and highlight a passage. For other topics, turn on Web search or ask “Search the web for …” to review public sources.",
      placeholder:"Ask a question…", send:"Send message", explainSelection:"Explain selected text", settings:"Privacy & memory", clear:"Clear this chat", closePanel:"Close", settingsTitle:"Privacy controls",
      settingsDescription:"Choose what this assistant may save in MySQL. Chat history and long-term memory are separate and off by default. MoteLona processes chat with local Java algorithms and uses no AI API. If you ask for a web search or turn on Web search, your query is sent to DuckDuckGo to retrieve public sources.",
      historyLabel:"Save chat history to the MySQL database", memoryLabel:"Allow long-term memory only when I explicitly ask to remember something", onlineLabel:"Search terms are sent to DuckDuckGo only when you ask or turn on Web search. No AI API key is used.", saveSettings:"Save choices", cancel:"Cancel", historyOn:"History: on", historyOff:"History: off", memoryOn:"Memory: on", memoryOff:"Memory: off",
      emptySelection:"Select some text on the page first, then choose “Explain selected text”.", working:"Thinking…", retry:"Try again", requestFailed:"Could not get a MoteLona response. Check the Java backend and MySQL connection. No external AI API key or quota is needed.",
      greetingTip:"Try asking about this website", sampleProjects:"What projects are on this page?", sampleSkills:"Take me to the skills section", sampleApi:"Explain REST API in simple terms", webDisabled:"Internet search is unavailable right now.",
      stackAria:"Technology stack", previewAria:"MoteLona capabilities", projectKicker:"JAVA WEBSITE AGENT · LOCAL READY", projectTitle:"MoteLona — AI Website Assistant", projectDescription:"A Java website assistant that answers from Portfolio and MySQL data, explains technical terms, opens relevant pages and highlights passages in yellow. Visitors can request web search and review DuckDuckGo sources; no AI API key is required.", projectStatus:"Runs locally with Java and MySQL. Web search requires an Internet connection from the backend. Database and deployment checks depend on the hosting environment.", learnMore:"View project details", projectAria:"Explore the MoteLona project overview",
      navDone:"Taking you to the requested section.", navMissing:"I could not find that section on this version of the page.", localAI:"Java · local NLP assistant (no API)", actionDone:"Action completed", sourceWebsite:"Website context", sourceGlossary:"Built-in glossary", sourceModel:"Local processing engine", saved:"Memory saved.", forgotten:"Matching memory items were deleted.", memoryOffNotice:"Long-term memory is off. Open Privacy & memory to enable it before asking me to remember something.",
      termLevels:"Explanation level", basic:"Basic", technical:"Technical", deep:"In depth", explain:"Explain", exportData:"Export saved data", deleteHistory:"Delete saved history", deleteMemories:"Delete all saved memories", confirmDeleteHistory:"Delete saved chat history for this session?", confirmDeleteMemories:"Delete every saved memory? This cannot be undone.", historyDeleted:"Saved chat history deleted.", memoriesDeleted:"All saved memories deleted.", settingsSaved:"Privacy choices saved.", settingsNotPersisted:"The choices could not be saved in browser storage; they may reset when this page closes.", historyDisableDeleteFailed:"History recording is now off, but previously saved history could not be deleted. Use Delete saved history when the backend is available.", localSearch:"Search this page", webSearch:"Web search", webSearchOn:"Web search: on", webSearchOff:"Web search: off", webSearchSource:"DuckDuckGo web results", webSearchHeading:"Search results", webSearchPrompt:"Search the web for", webSearchFailed:"Internet search is temporarily unavailable. Try again later.", webSearchNoResults:"No readable search results were returned. Try a shorter query.", navHighlightMissing:"I reached the section, but could not find that exact phrase to highlight.", searchPlaceholder:"Search words on this page", searchSubmit:"Search", searchClose:"Close search", noResults:"No matching page text found."
    },
    vi: {
      openAssistant:"MoteLona", openAssistantLabel:"Mở trợ lý MoteLona", close:"Đóng trợ lý", title:"MoteLona", subtitle:"Trợ lý website MoteLona", resizePanel:"Kéo góc để đổi kích thước",
      statusChecking:"Đang kiểm tra backend Java…", statusOnline:"Đã kết nối backend · bộ máy AI Java cục bộ đã sẵn sàng", statusOffline:"Không kết nối được backend · hãy kiểm tra dịch vụ Java", statusConfigure:"Website công khai chưa được cấu hình URL backend", backendNotConfigured:"Giao diện chat đã sẵn sàng nhưng chưa có URL backend Java. Hãy đặt window.MOTELONA_API_BASE thành URL HTTPS của backend đã triển khai để bật phản hồi thực tế.",
      welcome:"Xin chào! Tôi là MoteLona. Bạn có thể hỏi về Portfolio, yêu cầu giải thích hoặc bảo tôi mở đúng trang và tô vàng đoạn cần xem. Với chủ đề bên ngoài website, hãy bật Tìm web hoặc yêu cầu “Tìm trên Internet: …” để xem nguồn công khai.",
      placeholder:"Nhập câu hỏi…", send:"Gửi tin nhắn", explainSelection:"Giải thích phần đã bôi đen", settings:"Quyền riêng tư & bộ nhớ", clear:"Xóa hội thoại này", closePanel:"Đóng", settingsTitle:"Quản lý quyền riêng tư",
      settingsDescription:"Chọn dữ liệu mà trợ lý được phép lưu trong MySQL. Lịch sử chat và bộ nhớ dài hạn tách biệt, mặc định đều tắt. Chat được xử lý bằng thuật toán Java cục bộ, không dùng API AI. Nếu bạn yêu cầu tìm web hoặc bật Tìm web, câu hỏi sẽ được gửi đến DuckDuckGo để lấy nguồn công khai.",
      historyLabel:"Lưu lịch sử trò chuyện trong MySQL (backend Java)", memoryLabel:"Cho phép bộ nhớ dài hạn, chỉ khi tôi chủ động yêu cầu ghi nhớ", onlineLabel:"Chỉ gửi câu hỏi đến DuckDuckGo khi bạn chủ động yêu cầu hoặc bật Tìm web. Không dùng khóa API AI.",
      saveSettings:"Lưu lựa chọn", cancel:"Hủy", historyOn:"Lịch sử: bật", historyOff:"Lịch sử: tắt", memoryOn:"Bộ nhớ: bật", memoryOff:"Bộ nhớ: tắt",
      emptySelection:"Hãy bôi đen một đoạn chữ trên trang trước, sau đó chọn “Giải thích phần đã bôi đen”.", working:"Đang xử lý…", retry:"Thử lại", requestFailed:"Không nhận được phản hồi từ MoteLona. Hãy kiểm tra backend Java và kết nối MySQL. Hệ thống không cần API key hoặc quota AI bên ngoài.",
      greetingTip:"Thử hỏi về website này", sampleProjects:"Trang này có những dự án nào?", sampleSkills:"Cuộn đến phần kỹ năng", sampleApi:"Giải thích REST API thật dễ hiểu", webDisabled:"Tìm kiếm trên Internet hiện tạm thời không khả dụng.",
      stackAria:"Công nghệ sử dụng", previewAria:"Các chức năng MoteLona", projectKicker:"TRỢ LÝ AI WEBSITE BẰNG JAVA · CHẠY CỤC BỘ", projectTitle:"MoteLona — Trợ lý AI cho website", projectDescription:"Trợ lý AI cho website, xây dựng bằng Java: trả lời dựa trên dữ liệu Portfolio và MySQL, giải thích thuật ngữ, mở trang hoặc mục liên quan và tô vàng đoạn cần xem. Người xem có thể chủ động tìm trên web và mở nguồn DuckDuckGo; không cần khóa API AI.", projectStatus:"Chạy cục bộ bằng Java và MySQL. Tìm trên web cần backend có kết nối Internet. Việc kiểm tra database và triển khai còn tùy môi trường sử dụng.", learnMore:"Xem chi tiết dự án", projectAria:"Mở trang giới thiệu dự án MoteLona",
      navDone:"Đang chuyển đến mục bạn yêu cầu.", navMissing:"Không tìm thấy mục đó trong phiên bản trang này.", localAI:"Java · AI xử lý cục bộ (không API)", actionDone:"Đã thực hiện hành động", sourceWebsite:"Ngữ cảnh website", sourceGlossary:"Từ điển tích hợp", sourceModel:"Bộ máy xử lý cục bộ", saved:"Đã lưu bộ nhớ.", forgotten:"Đã xóa những mục bộ nhớ phù hợp.", memoryOffNotice:"Bộ nhớ dài hạn đang tắt. Mở Quyền riêng tư và bộ nhớ để bật trước khi yêu cầu ghi nhớ.",
      termLevels:"Mức giải thích", basic:"Cơ bản", technical:"Kỹ thuật", deep:"Chuyên sâu", explain:"Giải thích", exportData:"Xuất dữ liệu đã lưu", deleteHistory:"Xóa lịch sử đã lưu", deleteMemories:"Xóa toàn bộ bộ nhớ đã lưu", confirmDeleteHistory:"Xóa lịch sử chat đã lưu của phiên này?", confirmDeleteMemories:"Xóa tất cả bộ nhớ đã lưu? Thao tác này không thể hoàn tác.", historyDeleted:"Đã xóa lịch sử chat đã lưu.", memoriesDeleted:"Đã xóa toàn bộ bộ nhớ đã lưu.", settingsSaved:"Đã lưu lựa chọn quyền riêng tư.", settingsNotPersisted:"Không thể lưu lựa chọn trong bộ nhớ trình duyệt; cài đặt có thể bị đặt lại khi đóng trang.", historyDisableDeleteFailed:"Đã tắt việc ghi lịch sử mới nhưng không xóa được lịch sử đã lưu trước đó. Hãy thử Xóa lịch sử đã lưu khi backend hoạt động.", localSearch:"Tìm trên trang này", webSearch:"Tìm web", webSearchOn:"Tìm web: bật", webSearchOff:"Tìm web: tắt", webSearchSource:"Kết quả tìm kiếm DuckDuckGo", webSearchHeading:"Kết quả tìm kiếm", webSearchPrompt:"Tìm trên Internet:", webSearchFailed:"Tạm thời không tìm được trên Internet. Bạn hãy thử lại sau.", webSearchNoResults:"Không có kết quả tìm kiếm đọc được. Hãy thử truy vấn ngắn hơn.", navHighlightMissing:"Đã đến mục liên quan nhưng không tìm thấy chính xác cụm từ để tô vàng.", searchPlaceholder:"Tìm từ khóa trong trang", searchSubmit:"Tìm kiếm", searchClose:"Đóng tìm kiếm", noResults:"Không tìm thấy nội dung phù hợp trên trang."
    }
  };
  const terms = [
    {term:"REST API", vi:"REST API", en:"REST API", enDef:"A web interface that lets software systems exchange resources through standard HTTP methods such as GET and POST.", viDef:"Giao diện web cho phép các hệ thống phần mềm trao đổi tài nguyên bằng những phương thức HTTP chuẩn như GET và POST."},
    {term:"API", vi:"API", en:"API", enDef:"A defined interface that lets one program request data or actions from another program.", viDef:"Giao diện được quy định để một chương trình yêu cầu dữ liệu hoặc chức năng từ chương trình khác."},
    {term:"Backend", vi:"Backend", en:"Backend", enDef:"The server-side part of an application that validates requests, applies business rules, and accesses data.", viDef:"Phần máy chủ của ứng dụng, kiểm tra yêu cầu, xử lý nghiệp vụ và truy cập dữ liệu."},
    {term:"Frontend", vi:"Frontend", en:"Frontend", enDef:"The user interface and browser-side code people see and interact with.", viDef:"Giao diện và mã chạy trong trình duyệt mà người dùng nhìn thấy và tương tác."},
    {term:"NLP", vi:"Xử lý ngôn ngữ tự nhiên", en:"NLP", enDef:"Natural Language Processing: methods for processing, analyzing, classifying, and retrieving human language.", viDef:"Các phương pháp giúp phần mềm xử lý, phân tích, phân loại và truy xuất ngôn ngữ của con người."},
    {term:"TF-IDF", vi:"TF-IDF", en:"TF-IDF", enDef:"A text-weighting method that highlights terms important to a document compared with a collection of documents.", viDef:"Phương pháp gán trọng số từ theo tần suất trong tài liệu và độ hiếm của từ trong cả tập tài liệu."},
    {term:"Cosine Similarity", vi:"Cosine Similarity", en:"Cosine Similarity", enDef:"A measure that compares two vectors by the cosine of the angle between them.", viDef:"Thước đo so sánh hai vector dựa trên cosine của góc giữa chúng."},
    {term:"Intent Classification", vi:"Phân loại ý định", en:"Intent Classification", enDef:"The process of assigning a user message to an intent such as asking about skills, projects, or contact details.", viDef:"Quá trình gán tin nhắn của người dùng vào một ý định như hỏi kỹ năng, dự án hoặc thông tin liên hệ."},
    {term:"Tokenization", vi:"Tokenization", en:"Tokenization", enDef:"The process of splitting text into smaller units such as words or tokens for further processing.", viDef:"Quá trình tách văn bản thành các đơn vị nhỏ như từ hoặc token để xử lý tiếp."},
    {term:"Knowledge Base", vi:"Kho kiến thức", en:"Knowledge Base", enDef:"A structured collection of trusted information that an application searches to answer domain-specific questions.", viDef:"Tập hợp thông tin có cấu trúc và được kiểm soát để ứng dụng tìm kiếm khi trả lời câu hỏi theo lĩnh vực."},
    {term:"Machine Learning", vi:"Machine Learning", en:"Machine Learning", enDef:"A branch of AI where algorithms learn patterns from data; rule-based AI does not necessarily use it.", viDef:"Nhánh AI trong đó thuật toán học quy luật từ dữ liệu; AI dựa trên luật không nhất thiết sử dụng Machine Learning."},
    {term:"AI Model", vi:"Mô hình AI", en:"AI Model", enDef:"A computational model designed or trained to map inputs to outputs such as classifications, predictions, or generated text.", viDef:"Mô hình tính toán được thiết kế hoặc huấn luyện để ánh xạ đầu vào thành đầu ra như phân loại, dự đoán hoặc văn bản."},
    {term:"Local AI", vi:"AI cục bộ", en:"Local AI", enDef:"AI capabilities executed within infrastructure controlled by the application owner without sending prompts to a third-party AI API.", viDef:"Năng lực AI chạy trong hạ tầng do chủ ứng dụng kiểm soát, không gửi câu hỏi đến API AI của bên thứ ba."},
    {term:"Spring Boot", vi:"Spring Boot", en:"Spring Boot", enDef:"A Java framework that simplifies building stand-alone Spring applications.", viDef:"Framework Java giúp đơn giản hóa việc xây dựng ứng dụng Spring độc lập."},
    {term:"MySQL", vi:"MySQL", en:"MySQL", enDef:"A relational database management system that stores structured data in tables.", viDef:"Hệ quản trị cơ sở dữ liệu quan hệ lưu dữ liệu có cấu trúc trong các bảng."},
    {term:"Java", vi:"Java", en:"Java", enDef:"A statically typed, object-oriented programming language commonly executed on the Java Virtual Machine.", viDef:"Ngôn ngữ lập trình hướng đối tượng, kiểu tĩnh, thường chạy trên Java Virtual Machine."},
    {term:"HTML", vi:"HTML", en:"HTML", enDef:"The markup language that defines the structure and content of a web page.", viDef:"Ngôn ngữ đánh dấu xác định cấu trúc và nội dung trang web."},
    {term:"CSS", vi:"CSS", en:"CSS", enDef:"The style language that controls web page appearance and layout.", viDef:"Ngôn ngữ kiểu dáng điều khiển giao diện và bố cục trang web."},
    {term:"JavaScript", vi:"JavaScript", en:"JavaScript", enDef:"A programming language commonly used to add behavior and interactivity to web pages.", viDef:"Ngôn ngữ lập trình thường dùng để bổ sung hành vi và tính tương tác cho trang web."},
    {term:"DOM", vi:"DOM", en:"DOM", enDef:"The browser's object representation of an HTML document, which JavaScript can inspect and update.", viDef:"Biểu diễn tài liệu HTML thành đối tượng trong trình duyệt để JavaScript đọc và cập nhật."},
    {term:"CORS", vi:"CORS", en:"CORS", enDef:"A browser security mechanism that controls which origins may read responses from another origin.", viDef:"Cơ chế bảo mật trình duyệt kiểm soát những nguồn nào được đọc phản hồi từ nguồn khác."},
    {term:"JSON", vi:"JSON", en:"JSON", enDef:"A text format for representing structured data exchanged between programs.", viDef:"Định dạng văn bản biểu diễn dữ liệu có cấu trúc được trao đổi giữa các chương trình."},
    {term:"Prompt Injection", vi:"Prompt Injection", en:"Prompt Injection", enDef:"An attack where untrusted content attempts to override the intended instructions of an AI system.", viDef:"Kiểu tấn công trong đó nội dung không đáng tin cố ghi đè chỉ dẫn dự kiến của hệ thống AI."}
  ];
  const termExamples = {
    "REST API": {en:"The website sends JSON requests to Java endpoints such as POST /api/ai/chat and GET /api/ai/health.",vi:"Website gửi yêu cầu JSON đến các endpoint Java như POST /api/ai/chat và GET /api/ai/health."},
    "Backend": {en:"The Java backend validates input, classifies intent, retrieves relevant Portfolio passages, and stores consented data in MySQL.",vi:"Backend Java kiểm tra đầu vào, phân loại ý định, truy xuất nội dung Portfolio liên quan và lưu dữ liệu được người dùng cho phép trong MySQL."},
    "Frontend": {en:"HTML, CSS, and JavaScript create the chat interface and send questions to the Java backend.",vi:"HTML, CSS và JavaScript tạo giao diện chat rồi gửi câu hỏi đến backend Java."},
    "NLP": {en:"MoteLona uses local NLP for text normalization, tokenization, intent classification, and document retrieval.",vi:"MoteLona dùng NLP cục bộ để chuẩn hóa văn bản, tách token, phân loại ý định và truy xuất tài liệu."},
    "TF-IDF": {en:"TF-IDF gives higher weight to useful terms that distinguish one Portfolio text passage from other passages.",vi:"TF-IDF tăng trọng số cho những từ hữu ích giúp phân biệt một đoạn Portfolio với các đoạn khác."},
    "Cosine Similarity": {en:"MoteLona compares the vector for a user question with vectors for content passages to rank relevant text.",vi:"MoteLona so sánh vector câu hỏi với vector các đoạn nội dung để xếp hạng văn bản liên quan."},
    "Intent Classification": {en:"The local classifier maps messages to intents such as skills, projects, education, experience, and contact.",vi:"Bộ phân loại cục bộ ánh xạ tin nhắn vào các ý định như kỹ năng, dự án, học vấn, kinh nghiệm và liên hệ."},
    "Knowledge Base": {en:"The assistant retrieves facts from visible page text, published Portfolio records, and active MySQL knowledge items.",vi:"Trợ lý truy xuất dữ kiện từ nội dung trang, bản ghi Portfolio đã công khai và mục kiến thức đang hoạt động trong MySQL."},
    "Local AI": {en:"This implementation does not call Gemini, OpenAI, or another external inference API; its behavior comes from Java algorithms and supplied data.",vi:"Phiên bản này không gọi Gemini, OpenAI hay API suy luận bên ngoài; hành vi của trợ lý đến từ thuật toán Java và dữ liệu được cung cấp."},
    "Machine Learning": {en:"The current MoteLona release uses TF-IDF retrieval and nearest-example intent matching; it does not train a deep neural network.",vi:"Phiên bản MoteLona hiện tại dùng truy xuất TF-IDF và so khớp ý định theo ví dụ gần nhất; chưa huấn luyện mạng nơ-ron sâu."},
    "AI Model": {en:"MoteLona's current local engine is an algorithmic NLP pipeline, not a generative large language model.",vi:"Bộ máy cục bộ hiện tại của MoteLona là pipeline NLP theo thuật toán, không phải mô hình ngôn ngữ lớn tạo sinh."},
    "MySQL": {en:"MySQL stores published Portfolio data, contact messages, opted-in chat history, and user-controlled memories.",vi:"MySQL lưu dữ liệu Portfolio đã công khai, tin nhắn liên hệ, lịch sử chat được cho phép và bộ nhớ do người dùng kiểm soát."},
    "Spring Boot": {en:"Spring Boot starts the Java REST backend and wires controllers, services, and configuration.",vi:"Spring Boot khởi chạy backend REST Java và kết nối controller, service cùng cấu hình."},
    "Java": {en:"Java handles API endpoints, text processing, intent classification, retrieval, business logic, and database access.",vi:"Java xử lý endpoint, văn bản, phân loại ý định, truy xuất dữ liệu, nghiệp vụ và truy cập cơ sở dữ liệu."},
    "DOM": {en:"The browser reads approved page sections and uses scrollIntoView for safe, allow-listed navigation.",vi:"Trình duyệt đọc các mục được cho phép và dùng scrollIntoView để điều hướng an toàn theo danh sách."},
    "CORS": {en:"The backend accepts browser requests only from origins configured in MOTELONA_CORS_ORIGINS.",vi:"Backend chỉ nhận yêu cầu trình duyệt từ origin được cấu hình trong MOTELONA_CORS_ORIGINS."},
    "JSON": {en:"The browser sends a JSON object containing the message, language, session ID, and limited page context.",vi:"Trình duyệt gửi đối tượng JSON gồm tin nhắn, ngôn ngữ, mã phiên và ngữ cảnh trang có giới hạn."}
  };
  let language = readLanguage();
  let historyEnabled = readFlag("motelona-history-enabled");
  let memoryEnabled = readFlag("motelona-memory-enabled");
  let sessionId = getSessionId();
  let isBusy = false;
  let messages = [];
  let lastQuestion = "";
  let highlightedRanges = [];
  let webSearchEnabled = false;

  function readLanguage() { try { return localStorage.getItem("animfolio-language") === "vi" ? "vi" : "en"; } catch (_) { return document.documentElement.lang === "vi" ? "vi" : "en"; } }
  function readFlag(key) { try { return localStorage.getItem(key) === "true"; } catch (_) { return false; } }
  function t(key) { return (dict[language] && dict[language][key]) || dict.en[key] || key; }
  function getSessionId() {
    function secureRandomId() {
      if (window.crypto && typeof window.crypto.randomUUID === "function") return window.crypto.randomUUID();
      if (window.crypto && typeof window.crypto.getRandomValues === "function") {
        const bytes = new Uint8Array(16); window.crypto.getRandomValues(bytes);
        return Array.from(bytes, value => value.toString(16).padStart(2, "0")).join("");
      }
      throw new Error("Secure browser randomness is required for a private assistant session.");
    }
    try { let id = localStorage.getItem("motelona-session-id"); if (!id) { id = "mt-" + secureRandomId(); localStorage.setItem("motelona-session-id", id); } return id; }
    catch (_) { return "mt-" + secureRandomId(); }
  }
  function el(tag, attrs, text) { const node = document.createElement(tag); Object.entries(attrs || {}).forEach(([key,value]) => { if (key === "class") node.className = value; else if (key === "dataset") Object.entries(value).forEach(([k,v]) => node.dataset[k]=v); else if (key === "hidden") node.hidden = value; else node.setAttribute(key,value); }); if (text != null) node.textContent = text; return node; }

  root.innerHTML = '<button class="motelona-ai-launcher" type="button" data-motelona-launcher aria-expanded="false" aria-controls="motelona-assistant-panel" aria-haspopup="dialog" aria-label="Open MoteLona"><span data-i18n="openAssistant">MoteLona</span></button>' +
    '<section class="motelona-ai-panel" id="motelona-assistant-panel" data-motelona-panel aria-labelledby="motelona-assistant-title" role="dialog" aria-modal="false" hidden>' +
      '<header class="motelona-ai-header"><div class="motelona-ai-brand" aria-hidden="true">M·</div><div class="motelona-ai-heading"><h2 class="motelona-ai-title" id="motelona-assistant-title" data-i18n="title">MoteLona</h2><p class="motelona-ai-subtitle" data-i18n="subtitle">AI website assistant</p></div><button class="motelona-ai-icon-button" type="button" data-motelona-settings aria-label="Privacy and memory" title="Privacy and memory">⚙</button><button class="motelona-ai-icon-button" type="button" data-motelona-close aria-label="Close assistant" title="Close">×</button></header>' +
      '<div class="motelona-ai-toolsbar"><button class="motelona-ai-chip" type="button" data-motelona-history aria-pressed="false">History: off</button><button class="motelona-ai-chip" type="button" data-motelona-memory aria-pressed="false">Memory: off</button><button class="motelona-ai-chip" type="button" data-motelona-explain-selection>Explain selection</button><button class="motelona-ai-chip" type="button" data-motelona-web-search aria-pressed="false">Search web: off</button><button class="motelona-ai-chip" type="button" data-motelona-search-toggle data-i18n="localSearch">Search this page</button></div>' +
      '<form class="motelona-ai-search" data-motelona-search-form hidden><input class="motelona-ai-search__input" data-motelona-search-input type="search" maxlength="120" autocomplete="off" data-i18n-placeholder="searchPlaceholder" placeholder="Search words on this page" aria-label="Search words on this page"><button class="motelona-ai-chip" type="submit" data-i18n="searchSubmit">Search</button><button class="motelona-ai-chip" type="button" data-motelona-search-close data-i18n="searchClose">Close search</button></form>' +
      '<div class="motelona-ai-search-results" data-motelona-search-results hidden aria-live="polite"></div>' +
      '<div class="motelona-ai-status" data-motelona-status data-state="checking" role="status"><span class="motelona-ai-status__dot"></span><span data-motelona-status-text>Checking Java backend…</span></div>' +
      '<div class="motelona-ai-messages" data-motelona-messages role="log" aria-live="polite" aria-relevant="additions text"></div>' +
      '<div class="motelona-ai-suggestions" data-motelona-suggestions><button class="motelona-ai-chip" type="button" data-prompt="sampleProjects">What projects are on this page?</button><button class="motelona-ai-chip" type="button" data-prompt="sampleSkills">Take me to the skills section</button><button class="motelona-ai-chip" type="button" data-prompt="sampleApi">Explain REST API in simple terms</button></div>' +
      '<form class="motelona-ai-compose" data-motelona-form><textarea class="motelona-ai-input" data-motelona-input rows="1" maxlength="3000" aria-label="Ask MoteLona" placeholder="Ask a question…"></textarea><button class="motelona-ai-send" data-motelona-send type="submit" aria-label="Send message">➤</button></form>' +
      '<footer class="motelona-ai-footer"><span data-i18n="localAI">Local AI</span><button type="button" data-motelona-clear data-i18n="clear">Clear this chat</button></footer>' +
      '<div class="motelona-ai-consent" data-motelona-consent><h3 data-i18n="settingsTitle">Privacy controls</h3><p data-i18n="settingsDescription">Choose what this local assistant may save.</p><label class="motelona-ai-consent__row"><input type="checkbox" data-history-consent><span data-i18n="historyLabel">Save chat history to this computer</span></label><label class="motelona-ai-consent__row"><input type="checkbox" data-memory-consent><span data-i18n="memoryLabel">Allow long-term memory only when I explicitly ask</span></label><p data-i18n="onlineLabel">Web searches are sent to DuckDuckGo only when requested.</p><div class="motelona-ai-consent__actions"><button class="motelona-ai-primary" type="button" data-save-consent data-i18n="saveSettings">Save choices</button><button type="button" data-cancel-consent data-i18n="cancel">Cancel</button><button type="button" data-export-data data-i18n="exportData">Export saved data</button><button type="button" data-delete-history data-i18n="deleteHistory">Delete saved history</button><button type="button" data-delete-memories data-i18n="deleteMemories">Delete all saved memories</button></div></div>' +
    '<button class="motelona-ai-resize-handle" type="button" data-motelona-resize aria-label="Drag the corner to resize" title="Drag the corner to resize"></button>' +
    '</section>' +
    '<aside class="motelona-ai-popover" data-motelona-popover hidden role="dialog" aria-modal="false"><button class="motelona-ai-icon-button motelona-ai-popover__close" type="button" data-popover-close aria-label="Close">×</button><h4 data-popover-title></h4><p data-popover-content></p></aside>';

  const panel = root.querySelector("[data-motelona-panel]");
  const launcher = root.querySelector("[data-motelona-launcher]");
  const messagesEl = root.querySelector("[data-motelona-messages]");
  const form = root.querySelector("[data-motelona-form]");
  const input = root.querySelector("[data-motelona-input]");
  const sendButton = root.querySelector("[data-motelona-send]");
  const statusEl = root.querySelector("[data-motelona-status]");
  const statusText = root.querySelector("[data-motelona-status-text]");
  const consentEl = root.querySelector("[data-motelona-consent]");
  const popover = root.querySelector("[data-motelona-popover]");


  // Allow the open assistant window to be repositioned by dragging its header.
  // Pointer events cover mouse, pen and touch without interfering with header buttons.
  function enablePanelDragging() {
    const dragHandle = root.querySelector(".motelona-ai-header");
    if (!dragHandle || !panel) return;

    let dragState = null;

    function getVisualScale(rect) {
      const widthScale = panel.offsetWidth ? rect.width / panel.offsetWidth : 1;
      const heightScale = panel.offsetHeight ? rect.height / panel.offsetHeight : widthScale;
      const scale = (widthScale + heightScale) / 2;
      return Number.isFinite(scale) && scale > 0 ? scale : 1;
    }

    function finishDrag(event) {
      if (!dragState || (event && event.pointerId !== dragState.pointerId)) return;
      dragState = null;
      panel.classList.remove("is-dragging");
      if (event && dragHandle.hasPointerCapture && dragHandle.hasPointerCapture(event.pointerId)) {
        try { dragHandle.releasePointerCapture(event.pointerId); } catch (_) { /* Pointer may already be released. */ }
      }
    }

    dragHandle.addEventListener("pointerdown", event => {
      if (event.button !== undefined && event.button !== 0) return;
      if (event.target.closest("button, a, input, textarea, select, [contenteditable='true']")) return;
      if (panel.hidden) return;

      const rect = panel.getBoundingClientRect();
      const scale = getVisualScale(rect);
      dragState = {
        pointerId: event.pointerId,
        startX: event.clientX,
        startY: event.clientY,
        startLeft: rect.left / scale,
        startTop: rect.top / scale,
        scale: scale
      };
      panel.classList.add("is-dragging");
      try { dragHandle.setPointerCapture(event.pointerId); } catch (_) { /* Continue with document pointer events. */ }
    });

    dragHandle.addEventListener("pointermove", event => {
      if (!dragState || event.pointerId !== dragState.pointerId) return;
      const rect = panel.getBoundingClientRect();
      const scale = dragState.scale;
      const visualWidth = rect.width;
      const visualHeight = rect.height;
      const minLeft = 8 / scale;
      const minTop = 8 / scale;
      const maxLeft = Math.max(minLeft, (window.innerWidth - visualWidth - 8) / scale);
      const maxTop = Math.max(minTop, (window.innerHeight - visualHeight - 8) / scale);
      const nextLeft = dragState.startLeft + (event.clientX - dragState.startX) / scale;
      const nextTop = dragState.startTop + (event.clientY - dragState.startY) / scale;

      panel.style.right = "auto";
      panel.style.bottom = "auto";
      panel.style.left = Math.min(maxLeft, Math.max(minLeft, nextLeft)) + "px";
      panel.style.top = Math.min(maxTop, Math.max(minTop, nextTop)) + "px";
    });

    dragHandle.addEventListener("pointerup", finishDrag);
    dragHandle.addEventListener("pointercancel", finishDrag);
    dragHandle.addEventListener("lostpointercapture", finishDrag);

    window.addEventListener("resize", () => {
      if (panel.hidden || (!panel.style.left && !panel.style.top)) return;
      const rect = panel.getBoundingClientRect();
      const scale = getVisualScale(rect);
      const minLeft = 8 / scale;
      const minTop = 8 / scale;
      const maxLeft = Math.max(minLeft, (window.innerWidth - rect.width - 8) / scale);
      const maxTop = Math.max(minTop, (window.innerHeight - rect.height - 8) / scale);
      const currentLeft = rect.left / scale;
      const currentTop = rect.top / scale;
      panel.style.left = Math.min(maxLeft, Math.max(minLeft, currentLeft)) + "px";
      panel.style.top = Math.min(maxTop, Math.max(minTop, currentTop)) + "px";
    }, { passive: true });
  }
  enablePanelDragging();

  // Resize the assistant like a desktop window by dragging its bottom-right corner.
  // Pointer events support mouse, pen and touch; arrow keys provide an accessible alternative.
  function enablePanelResizing() {
    const resizeHandle = root.querySelector("[data-motelona-resize]");
    if (!resizeHandle || !panel) return;

    let resizeState = null;

    function getScale(rect) {
      const widthScale = panel.offsetWidth ? rect.width / panel.offsetWidth : 1;
      const heightScale = panel.offsetHeight ? rect.height / panel.offsetHeight : widthScale;
      const scale = (widthScale + heightScale) / 2;
      return Number.isFinite(scale) && scale > 0 ? scale : 1;
    }

    function getLimits(left, top, scale) {
      const availableWidth = Math.max(1, (window.innerWidth - left * scale - 8) / scale);
      const availableHeight = Math.max(1, (window.innerHeight - top * scale - 8) / scale);
      return {
        minWidth: Math.min(320, availableWidth),
        minHeight: Math.min(360, availableHeight),
        maxWidth: availableWidth,
        maxHeight: availableHeight
      };
    }

    function setSize(width, height, limits) {
      const nextWidth = Math.min(limits.maxWidth, Math.max(limits.minWidth, width));
      const nextHeight = Math.min(limits.maxHeight, Math.max(limits.minHeight, height));
      panel.style.width = nextWidth + "px";
      panel.style.height = nextHeight + "px";
      resizeHandle.setAttribute("aria-valuetext", Math.round(nextWidth) + " × " + Math.round(nextHeight) + " px");
    }

    function finishResize(event) {
      if (!resizeState || (event && event.pointerId !== resizeState.pointerId)) return;
      resizeState = null;
      panel.classList.remove("is-resizing");
      if (event && resizeHandle.hasPointerCapture && resizeHandle.hasPointerCapture(event.pointerId)) {
        try { resizeHandle.releasePointerCapture(event.pointerId); } catch (_) { /* Pointer may already be released. */ }
      }
    }

    resizeHandle.addEventListener("pointerdown", event => {
      if (event.button !== undefined && event.button !== 0) return;
      if (panel.hidden) return;
      event.preventDefault();
      event.stopPropagation();

      const rect = panel.getBoundingClientRect();
      const scale = getScale(rect);
      const left = rect.left / scale;
      const top = rect.top / scale;
      const width = panel.offsetWidth || rect.width / scale;
      const height = panel.offsetHeight || rect.height / scale;

      // Convert the default right/bottom anchoring into a top/left anchor so
      // dragging the bottom-right grip changes only the width and height.
      panel.style.right = "auto";
      panel.style.bottom = "auto";
      panel.style.left = left + "px";
      panel.style.top = top + "px";
      panel.style.boxSizing = "border-box";
      panel.style.maxWidth = "none";
      panel.style.maxHeight = "none";

      resizeState = {
        pointerId: event.pointerId,
        startX: event.clientX,
        startY: event.clientY,
        startWidth: width,
        startHeight: height,
        left,
        top,
        scale
      };
      panel.classList.add("is-resizing");
      try { resizeHandle.setPointerCapture(event.pointerId); } catch (_) { /* Document movement remains available. */ }
    });

    resizeHandle.addEventListener("pointermove", event => {
      if (!resizeState || event.pointerId !== resizeState.pointerId) return;
      const limits = getLimits(resizeState.left, resizeState.top, resizeState.scale);
      const deltaX = (event.clientX - resizeState.startX) / resizeState.scale;
      const deltaY = (event.clientY - resizeState.startY) / resizeState.scale;
      setSize(resizeState.startWidth + deltaX, resizeState.startHeight + deltaY, limits);
    });

    resizeHandle.addEventListener("pointerup", finishResize);
    resizeHandle.addEventListener("pointercancel", finishResize);
    resizeHandle.addEventListener("lostpointercapture", finishResize);

    resizeHandle.addEventListener("keydown", event => {
      if (panel.hidden || !["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight"].includes(event.key)) return;
      event.preventDefault();
      const rect = panel.getBoundingClientRect();
      const scale = getScale(rect);
      const left = rect.left / scale;
      const top = rect.top / scale;
      const limits = getLimits(left, top, scale);
      const step = event.shiftKey ? 48 : 16;
      const delta = event.key === "ArrowRight" ? step : event.key === "ArrowLeft" ? -step : 0;
      const vertical = event.key === "ArrowDown" ? step : event.key === "ArrowUp" ? -step : 0;
      panel.style.right = "auto";
      panel.style.bottom = "auto";
      panel.style.left = left + "px";
      panel.style.top = top + "px";
      panel.style.boxSizing = "border-box";
      panel.style.maxWidth = "none";
      panel.style.maxHeight = "none";
      setSize((panel.offsetWidth || rect.width / scale) + delta, (panel.offsetHeight || rect.height / scale) + vertical, limits);
    });

    resizeHandle.addEventListener("dblclick", event => {
      event.preventDefault();
      const rect = panel.getBoundingClientRect();
      const scale = getScale(rect);
      const left = rect.left / scale;
      const top = rect.top / scale;
      const limits = getLimits(left, top, scale);
      setSize(Math.min(456, limits.maxWidth), Math.min(680, limits.maxHeight), limits);
    });

    resizeHandle.title = t("resizePanel");
    resizeHandle.setAttribute("aria-label", t("resizePanel"));

    window.addEventListener("resize", () => {
      if (panel.hidden || (!panel.style.left && !panel.style.top)) return;
      const rect = panel.getBoundingClientRect();
      const scale = getScale(rect);
      const left = Math.max(8 / scale, Math.min(rect.left / scale, (window.innerWidth - 8 - rect.width) / scale));
      const top = Math.max(8 / scale, Math.min(rect.top / scale, (window.innerHeight - 8 - rect.height) / scale));
      panel.style.left = left + "px";
      panel.style.top = top + "px";
      const limits = getLimits(left, top, scale);
      setSize(Math.min(panel.offsetWidth || rect.width / scale, limits.maxWidth), Math.min(panel.offsetHeight || rect.height / scale, limits.maxHeight), limits);
    }, { passive: true });
  }
  enablePanelResizing();

  const searchForm = root.querySelector("[data-motelona-search-form]");
  const searchInput = root.querySelector("[data-motelona-search-input]");
  const searchResultsEl = root.querySelector("[data-motelona-search-results]");

  function setLanguage(next) {
    language = next === "vi" ? "vi" : "en";
    closePopover();
    document.querySelectorAll(".motelona-project-card [data-i18n], .motelona-project-note [data-i18n], .motelona-ai-root [data-i18n]").forEach(node => { node.textContent = t(node.dataset.i18n); });
    document.querySelectorAll(".motelona-project-card [data-i18n-aria], .motelona-project-note [data-i18n-aria]").forEach(node => { node.setAttribute("aria-label", t(node.dataset.i18nAria)); });
    root.querySelectorAll("[data-prompt]").forEach(node => { node.textContent = t(node.dataset.prompt); });
    root.querySelectorAll("[data-i18n-placeholder]").forEach(node => { node.placeholder = t(node.dataset.i18nPlaceholder); node.setAttribute("aria-label",t(node.dataset.i18nPlaceholder)); });
    root.querySelectorAll("[data-motelona-launcher] span[data-i18n]").forEach(node => { node.textContent = t("openAssistant"); });
    input.placeholder = t("placeholder");
    input.setAttribute("aria-label",t("placeholder"));
    sendButton.setAttribute("aria-label",t("send"));
    root.querySelector("[data-motelona-settings]").setAttribute("aria-label",t("settings"));
    root.querySelector("[data-motelona-close]").setAttribute("aria-label",t("closePanel"));
    launcher.setAttribute("aria-label",t("openAssistantLabel"));
    root.querySelector("[data-popover-close]").setAttribute("aria-label",t("closePanel"));
    root.querySelector("[data-motelona-settings]").title=t("settings");
    root.querySelector("[data-motelona-close]").title=t("closePanel");
    const resizeHandle = root.querySelector("[data-motelona-resize]");
    if (resizeHandle) { resizeHandle.setAttribute("aria-label",t("resizePanel")); resizeHandle.title=t("resizePanel"); }
    panel.setAttribute("aria-label",t("subtitle"));
    root.querySelector("[data-motelona-explain-selection]").textContent = t("explainSelection");
    root.querySelector("[data-motelona-clear]").textContent = t("clear");
    syncConsentUI(); syncToggles(); syncWebSearchButton();
    if (messages.length === 0) addMessage("assistant",t("welcome"),"local");
    refreshTermHighlights();
    if (API_CONFIGURED) checkHealth(); else setStatus('offline', t('statusConfigure'));
  }

  function syncToggles() {
    const h = root.querySelector("[data-motelona-history]"), m = root.querySelector("[data-motelona-memory]");
    h.textContent = historyEnabled ? t("historyOn") : t("historyOff"); h.setAttribute("aria-pressed",String(historyEnabled));
    m.textContent = memoryEnabled ? t("memoryOn") : t("memoryOff"); m.setAttribute("aria-pressed",String(memoryEnabled));
  }
  function syncWebSearchButton() {
    const button = root.querySelector("[data-motelona-web-search]");
    if (!button) return;
    button.textContent = webSearchEnabled ? t("webSearchOn") : t("webSearchOff");
    button.setAttribute("aria-pressed", String(webSearchEnabled));
  }
  function syncConsentUI() {
    root.querySelector("[data-history-consent]").checked = historyEnabled;
    root.querySelector("[data-memory-consent]").checked = memoryEnabled;
  }
  function setStatus(state, text) { statusEl.dataset.state = state; statusText.textContent = text; }
  function addMessage(role, content, source, isError) {
    const item = el("div",{class:"motelona-ai-message motelona-ai-message--" + role + (isError ? " motelona-ai-message--error" : "")},String(content || ""));
    if (source && role === "assistant") item.appendChild(el("span",{class:"motelona-ai-message__meta"},source));
    messagesEl.appendChild(item); messagesEl.scrollTop = messagesEl.scrollHeight;
    if (role === "user" || role === "assistant") messages.push({role: role === "user" ? "user" : "assistant", content:String(content || "").slice(0,2500)});
    return item;
  }
  function addWebResults(results) {
    const validResults = Array.isArray(results) ? results.filter(item => item && safeExternalUrl(item.url)) : [];
    if (!validResults.length) return;
    const group = el("div", { class: "motelona-ai-web-results" });
    group.appendChild(el("strong", { class: "motelona-ai-web-results__heading" }, t("webSearchHeading")));
    validResults.slice(0, 8).forEach(result => {
      const card = el("article", { class: "motelona-ai-web-result" });
      card.appendChild(el("a", { href: safeExternalUrl(result.url), target: "_blank", rel: "noopener noreferrer" }, String(result.title || result.url)));
      if (result.snippet) card.appendChild(el("p", {}, String(result.snippet)));
      group.appendChild(card);
    });
    messagesEl.appendChild(group);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }
  function safeExternalUrl(value) {
    try {
      const url = new URL(String(value || ""));
      return url.protocol === "https:" || url.protocol === "http:" ? url.href : "";
    } catch (_) { return ""; }
  }
  function addTyping() { const item=el("div",{class:"motelona-ai-message motelona-ai-message--assistant"},t("working")); messagesEl.appendChild(item); messagesEl.scrollTop=messagesEl.scrollHeight; return item; }
  let lastOpenTrigger = null;
  function openPanel(event) {
    const source = event && event.currentTarget;
    lastOpenTrigger = source && source !== launcher ? source : launcher;
    panel.hidden=false; launcher.setAttribute("aria-expanded","true"); launcher.hidden=true; input.focus(); checkHealth();
  }
  function closePanel() {
    panel.hidden=true; launcher.hidden=false; launcher.setAttribute("aria-expanded","false");
    const focusTarget = lastOpenTrigger && document.contains(lastOpenTrigger) ? lastOpenTrigger : launcher;
    focusTarget.focus();
    closePopover();
  }

  async function fetchJson(url, options, timeoutMs) {
    const controller = new AbortController(); const timer = setTimeout(() => controller.abort(), timeoutMs || 15000);
    try {
      const response = await fetch(url,{...(options || {}),signal:controller.signal,headers:{"Content-Type":"application/json",...((options||{}).headers||{})}});
      let data={}; try { data=await response.json(); } catch (_) {}
      if (!response.ok) { const err = new Error(data.message || data.error || ("HTTP " + response.status)); err.status=response.status; throw err; }
      return data;
    } finally { clearTimeout(timer); }
  }
  async function checkHealth() {
    if (!API_CONFIGURED) { setStatus("offline",t("statusConfigure")); return; }
    setStatus("checking",t("statusChecking"));
    try {
      const h = await fetchJson(API_BASE + "/api/ai/health",{method:"GET"},4500);
      if (h.backend === "up" && h.provider === "java-local" && h.apiKeyRequired === false) {
        const label = language === "vi" ? "Backend trực tuyến · AI Java tự xây đã sẵn sàng · " : "Backend online · self-built Java AI engine ready · ";
        setStatus("online", label + (h.engineVersion || "Local NLP Engine"));
      } else if (h.backend === "up") {
        setStatus("offline", h.message || (language === "vi" ? "Bộ máy AI cục bộ chưa sẵn sàng." : "The local AI engine is not ready."));
      } else setStatus("offline",t("statusOffline"));
    } catch (_) { setStatus("offline",t("statusOffline")); }
  }
  function pageContext() {
    const container = document.querySelector(".animfolio-container") || document.body;
    const parts = [];
    const detailPage = document.querySelector(".motelona-detail-root");

    if (detailPage) {
      const hero = detailPage.querySelector(".ml-hero-copy");
      const heroText = hero ? (hero.innerText || hero.textContent || "").replace(/\s+/g, " ").trim() : "";
      if (heroText) parts.push("PROJECT OVERVIEW: " + heroText.slice(0, 900));
      ["overview", "features", "architecture", "knowledge", "database", "quality", "scope", "roadmap"].forEach(id => {
        const node = document.getElementById(id);
        if (!node) return;
        const text = (node.innerText || node.textContent || "").replace(/\s+/g, " ").trim();
        if (text) parts.push("SECTION #" + id + ": " + text.slice(0, 950));
      });
      return parts.join("\n\n").slice(0, 9000);
    }

    const ids = ["hero", "about", "education", "skills", "projects", "stats", "contact"];
    ids.forEach(id => {
      const node = document.getElementById(id);
      if (!node) return;
      const text = (node.innerText || node.textContent || "").replace(/\s+/g, " ").trim();
      if (text) parts.push("SECTION #" + id + ": " + text.slice(0, 1800));
    });
    if (!parts.length) parts.push((container.innerText || container.textContent || "").replace(/\s+/g, " ").slice(0, 5500));
    return parts.join("\n\n").slice(0, 9000);
  }
  async function sendMessage(rawText, mode) {
    const text=String(rawText || "").trim();
    if (!text || isBusy) return;
    if (!API_CONFIGURED) { addMessage("user",text); addMessage("assistant",t("backendNotConfigured"),"",true); return; }
    lastQuestion=text; isBusy=true; sendButton.disabled=true; input.disabled=true;
    addMessage("user",text);
    const typing=addTyping();
    const context=pageContext();
    try {
      // Keep request bodies aligned with their Java record DTOs. Do not send chat-only
      // fields to ExplainRequest or explain-only fields to ChatRequest.
      const requestBody = mode === "explain"
        ? { sessionId, selectedText: text, level: "basic", language, websiteContext: context }
        : { sessionId, message: text, language, websiteContext: context,
            history: messages.slice(-13,-1).map(m => ({ role: m.role, content: m.content })),
            historyEnabled, memoryEnabled, searchWeb: webSearchEnabled };
      const response = await fetchJson(API_BASE + (mode === "explain" ? "/api/ai/explain" : "/api/ai/chat"),{
        method:"POST", body:JSON.stringify(requestBody)
      },125000);
      typing.remove();
      const responseText = response.message || response.answer || (language === "vi" ? "Backend không trả về nội dung." : "The backend returned no content.");
      const sourceLabel = response.source === "local-ai" ? t("localAI") : (response.source === "web-search" ? t("webSearchSource") : (response.source || (response.aiPowered ? t("sourceModel") : t("sourceWebsite"))));
      addMessage("assistant",responseText,sourceLabel);
      if (response.webResults) addWebResults(response.webResults);
      if (response.action && response.action.type === "navigate") performNavigation(response.action.target, response.action.term);
      if (response.action && response.action.type === "explain") showTermPopover(response.action.term || text, response.message, window.innerWidth-360, window.innerHeight-180);
      if (response.source === "web-disabled") addMessage("assistant",t("webDisabled"));
      if (response.health) await checkHealth();
    } catch (error) {
      typing.remove();
      const msg = webSearchEnabled || isExplicitWebSearch(text) ? t("webSearchFailed") : t("requestFailed");
      addMessage("assistant",msg,"",true);
      setStatus("offline",t("statusOffline"));
      const retry=el("button",{type:"button",class:"motelona-ai-chip"},t("retry"));
      retry.addEventListener("click",()=>sendMessage(lastQuestion,mode));
      messagesEl.appendChild(retry); messagesEl.scrollTop=messagesEl.scrollHeight;
    } finally { isBusy=false; sendButton.disabled=false; input.disabled=false; input.focus(); }
  }
  function isExplicitWebSearch(value) {
    return /^(?:t[iì]m(?: ki[eế]m)?\s+(?:tr[eê]n\s+)?(?:internet|m[aạ]ng|web)|tra c[uứ]u\s+(?:tr[eê]n\s+)?(?:internet|m[aạ]ng|web)|search\s+(?:the\s+)?(?:web|internet)|look\s+up(?:\s+online)?)/iu.test(String(value || "").trim());
  }
  function performNavigation(target, term) {
    const portfolioTargets = new Set(["hero", "about", "education", "skills", "projects", "stats", "contact"]);
    const detailTargets = new Set(["main-content", "motelona", "overview", "features", "architecture", "knowledge", "database", "quality", "scope", "roadmap"]);
    const isDetailPage = Boolean(document.querySelector(".motelona-detail-root"));
    const requestedTarget = String(target || "");
    const isPortfolioTarget = portfolioTargets.has(requestedTarget);
    const isDetailTarget = detailTargets.has(requestedTarget);
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    if ((!isDetailPage && isDetailTarget) || (isDetailPage && isPortfolioTarget)) {
      const destination = isDetailPage ? new URL("../../index.html", document.baseURI) : new URL("Project/1/motelona.html", document.baseURI);
      const section = requestedTarget === "motelona" ? "main-content" : requestedTarget;
      destination.hash = section;
      if (term) destination.searchParams.set("motelonaHighlight", String(term).slice(0, 120));
      window.location.assign(destination.href);
      return;
    }

    const safeTargets = isDetailPage ? detailTargets : portfolioTargets;
    const destination = isDetailPage && requestedTarget === "motelona" ? "main-content" : requestedTarget;
    if (!safeTargets.has(destination)) return addMessage("assistant", t("navMissing"));
    const node = document.getElementById(destination);
    if (!node) return addMessage("assistant", t("navMissing"));
    closePopover();
    node.scrollIntoView({ behavior: reduceMotion ? "auto" : "smooth", block: "start" });
    node.classList.add("motelona-ai-target-pulse");
    setTimeout(() => node.classList.remove("motelona-ai-target-pulse"), 2200);
    const highlightTerm = term || (destination === "main-content" ? "MoteLona" : destination);
    setTimeout(() => {
      if (highlightTerm && !highlightRequestedText(node, highlightTerm) && term) addMessage("assistant", t("navHighlightMissing"));
    }, reduceMotion ? 0 : 180);
  }

  function highlightRequestedText(container, rawTerm) {
    if (!container || !rawTerm) return false;
    const raw = String(rawTerm).trim().slice(0, 120);
    const tokens = raw.match(/[\\p{L}\\p{N}][\\p{L}\\p{N}'’’-]*/gu) || [];
    const stopWords = new Set(["the", "and", "that", "this", "about", "section", "paragraph", "part", "find", "show", "take", "me", "to", "đến", "phần", "đoạn", "nói", "về", "cho", "tôi", "xem"]);
    const useful = tokens.filter(word => !stopWords.has(word.toLowerCase()));
    const candidates = [raw, useful.slice(-3).join(" "), useful.slice(-2).join(" "), ...useful.sort((a, b) => b.length - a.length)]
      .map(value => value.trim()).filter((value, index, all) => value.length > 1 && all.indexOf(value) === index);
    const exclusion = "script,style,noscript,textarea,input,button,a,code,pre,[contenteditable='true'],.motelona-ai-root,.motelona-ai-panel,.motelona-ai-launcher";
    let foundRange = null;
    for (const candidate of candidates) {
      const expression = Array.from(candidate).map(ch => "\\^$.*+?()[]{}|".includes(ch) ? "\\" + ch : ch).join("").replace(/\s+/g, "\\s+");
      let regex;
      try { regex = new RegExp(expression, "iu"); } catch (_) { continue; }
      const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT, {
        acceptNode(textNode) {
          if (!textNode.nodeValue || !textNode.nodeValue.trim() || !textNode.parentElement || textNode.parentElement.closest(exclusion)) return NodeFilter.FILTER_REJECT;
          return NodeFilter.FILTER_ACCEPT;
        }
      });
      while (walker.nextNode()) {
        const textNode = walker.currentNode;
        regex.lastIndex = 0;
        const match = regex.exec(textNode.nodeValue);
        if (!match) continue;
        foundRange = document.createRange();
        foundRange.setStart(textNode, match.index);
        foundRange.setEnd(textNode, match.index + match[0].length);
        break;
      }
      if (foundRange) break;
    }
    if (!foundRange) return false;

    const block = foundRange.startContainer.parentElement.closest("p,li,h1,h2,h3,h4,article") || container;
    block.scrollIntoView({ behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth", block: "center" });
    if (window.CSS && CSS.highlights && typeof Highlight !== "undefined") {
      try {
        CSS.highlights.set("motelona-ai-target", new Highlight(foundRange));
        setTimeout(() => CSS.highlights.delete("motelona-ai-target"), 7000);
        return true;
      } catch (_) { /* Use the semantic mark fallback below. */ }
    }
    try {
      const mark = document.createElement("mark");
      mark.className = "motelona-ai-navigation-highlight";
      foundRange.surroundContents(mark);
      setTimeout(() => {
        if (!mark.parentNode) return;
        const parent = mark.parentNode;
        while (mark.firstChild) parent.insertBefore(mark.firstChild, mark);
        mark.remove();
        parent.normalize();
      }, 7000);
      return true;
    } catch (_) { return false; }
  }

  function applyPendingNavigationHighlight() {
    const url = new URL(window.location.href);
    const term = url.searchParams.get("motelonaHighlight");
    if (!term) return;
    const destination = decodeURIComponent(window.location.hash.slice(1));
    const allowed = document.querySelector(".motelona-detail-root")
      ? new Set(["main-content", "overview", "features", "architecture", "knowledge", "database", "quality", "scope", "roadmap"])
      : new Set(["hero", "about", "education", "skills", "projects", "stats", "contact"]);
    if (!allowed.has(destination)) return;
    setTimeout(() => {
      const node = document.getElementById(destination);
      if (node) {
        node.scrollIntoView({ behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth", block: "start" });
        node.classList.add("motelona-ai-target-pulse");
        setTimeout(() => node.classList.remove("motelona-ai-target-pulse"), 2200);
        highlightRequestedText(node, term);
      }
      url.searchParams.delete("motelonaHighlight");
      history.replaceState(null, "", url.pathname + (url.search ? "?" + url.searchParams.toString() : "") + url.hash);
    }, 180);
  }
  function closePopover() { popover.hidden=true; }
  // Escape closes the top-most assistant surface and restores a predictable focus target.
  document.addEventListener("keydown", event => {
    if (event.key !== "Escape" && event.key !== "Esc") return;
    if (!popover.hidden) { closePopover(); return; }
    if (consentEl.classList.contains("is-open")) {
      consentEl.classList.remove("is-open");
      root.querySelector("[data-motelona-settings]").focus();
      return;
    }
    if (!panel.hidden) closePanel();
  });
  function showTermPopover(term, content, x, y) {
    root.querySelector("[data-popover-title]").textContent=term;
    root.querySelector("[data-popover-content]").textContent=content || (language === "vi" ? "Chưa có định nghĩa." : "No definition available.");
    popover.hidden=false;
    const width=Math.min(330,window.innerWidth-24), height=popover.offsetHeight || 140;
    popover.style.left=Math.max(12,Math.min(Number(x)||12,window.innerWidth-width-12))+"px";
    popover.style.top=Math.max(12,Math.min(Number(y)||12,window.innerHeight-height-12))+"px";
  }
  function termAtPoint(event) {
    let range=null;
    if (document.caretRangeFromPoint) range=document.caretRangeFromPoint(event.clientX,event.clientY);
    else if (document.caretPositionFromPoint) { const p=document.caretPositionFromPoint(event.clientX,event.clientY); if (p) { range=document.createRange(); range.setStart(p.offsetNode,p.offset); range.collapse(true); } }
    if (!range || !range.startContainer || range.startContainer.nodeType!==Node.TEXT_NODE) return null;
    const n=range.startContainer, offset=range.startOffset;
    return highlightedRanges.find(item=>item.range.startContainer===n && offset>=item.range.startOffset && offset<=item.range.endOffset) || null;
  }
  function refreshTermHighlights() {
    highlightedRanges=[];
    if (!window.CSS || !CSS.highlights || typeof Highlight === "undefined") return;
    try { CSS.highlights.delete("motelona-ai-terms"); } catch (_) {}
    const container=document.querySelector(".animfolio-container") || document.body;
    const exclusion="script,style,noscript,textarea,input,button,a,code,pre,[contenteditable='true'],.motelona-ai-root,.motelona-ai-panel,.motelona-ai-launcher";
    const termTexts=Array.from(new Set(terms.flatMap(term=>[term.en,term.vi]))).sort((a,b)=>b.length-a.length);
    const escaped=termTexts.map(s=>s.replace(/[.*+?^${}()|[\]\\]/g,"\\$&"));
    let regex; try { regex=new RegExp("(?<![\\p{L}\\p{N}_])("+escaped.join("|")+")(?![\\p{L}\\p{N}_])","giu"); } catch (_) { regex=new RegExp("\\b("+termTexts.map(s=>s.replace(/[.*+?^${}()|[\]\\]/g,"\\$&")).join("|")+")\\b","gi"); }
    const walker=document.createTreeWalker(container,NodeFilter.SHOW_TEXT,{acceptNode(node){ if(!node.nodeValue || !node.nodeValue.trim() || !node.parentElement || node.parentElement.closest(exclusion)) return NodeFilter.FILTER_REJECT; return NodeFilter.FILTER_ACCEPT; }});
    const ranges=[];
    while(walker.nextNode()) { const node=walker.currentNode; regex.lastIndex=0; let match; while((match=regex.exec(node.nodeValue))) { const range=document.createRange(); range.setStart(node,match.index); range.setEnd(node,match.index+match[0].length); ranges.push(range); highlightedRanges.push({term:match[0],range}); if(!match[0].length) regex.lastIndex++; } }
    if(ranges.length) CSS.highlights.set("motelona-ai-terms",new Highlight(...ranges));
  }
  async function loadSavedHistory() {
    if (!historyEnabled || !API_CONFIGURED) return;
    try {
      const data=await fetchJson(API_BASE+"/api/ai/history/"+encodeURIComponent(sessionId),{method:"GET"},8000);
      if (Array.isArray(data.items) && data.items.length) {
        messages=[]; messagesEl.replaceChildren();
        data.items.slice(-40).forEach(m=>{ const node=el("div",{class:"motelona-ai-message motelona-ai-message--"+(m.role==="user"?"user":"assistant")},m.content); messagesEl.appendChild(node); messages.push({role:m.role,content:m.content}); });
        messagesEl.scrollTop=messagesEl.scrollHeight;
      }
    } catch (_) { /* History is optional; don't block chat if it is unavailable. */ }
  }
  async function getMemoryItems() {
    if (!memoryEnabled || !API_CONFIGURED) return;
    try {
      const data=await fetchJson(API_BASE+"/api/ai/memories?sessionId="+encodeURIComponent(sessionId),{method:"GET"},8000);
      if (Array.isArray(data.items)) {
        const wrap=el("div",{class:"motelona-ai-message motelona-ai-message--assistant"});
        wrap.appendChild(el("strong",{},language==="vi"?"Bộ nhớ đã lưu":"Saved memories"));
        data.items.slice(0,20).forEach(item=>{ const row=el("p",{},"• "+item.content); row.style.margin="8px 0"; const group=el("div",{}); const del=el("button",{type:"button",class:"motelona-ai-chip"},language==="vi"?"Xóa":"Delete"); del.addEventListener("click",async()=>{ del.disabled=true; try { await fetchJson(API_BASE+"/api/ai/memories/"+item.id+"?sessionId="+encodeURIComponent(sessionId),{method:"DELETE"},8000); group.remove(); } catch (_) { del.disabled=false; addMessage("assistant",t("requestFailed"),"",true); } }); group.append(row,del); wrap.appendChild(group); });
        messagesEl.appendChild(wrap); messagesEl.scrollTop=messagesEl.scrollHeight;
      }
    } catch (_) { addMessage("assistant",t("requestFailed"),"",true); }
  }

  launcher.addEventListener("click",openPanel);
  document.querySelectorAll("[data-motelona-open]").forEach(button=>button.addEventListener("click",openPanel));
  root.querySelector("[data-motelona-close]").addEventListener("click",closePanel);
  root.querySelector("[data-motelona-web-search]").addEventListener("click",()=>{
    webSearchEnabled = !webSearchEnabled;
    syncWebSearchButton();
    addMessage("assistant", webSearchEnabled
      ? (language === "vi" ? "Đã bật tìm kiếm web. Câu hỏi tiếp theo sẽ được gửi đến DuckDuckGo để lấy nguồn công khai." : "Web search is on. Your next question will be sent to DuckDuckGo for public sources.")
      : (language === "vi" ? "Đã tắt tìm kiếm web." : "Web search is off."));
  });
  root.querySelector("[data-motelona-search-toggle]").addEventListener("click",()=>{
    searchForm.hidden=!searchForm.hidden;
    if(!searchForm.hidden){searchInput.focus();}else{searchResultsEl.hidden=true;searchResultsEl.replaceChildren();}
  });
  root.querySelector("[data-motelona-search-close]").addEventListener("click",()=>{searchForm.hidden=true;searchResultsEl.hidden=true;searchResultsEl.replaceChildren();root.querySelector("[data-motelona-search-toggle]").focus();});
  searchForm.addEventListener("submit",async event=>{
    event.preventDefault();
    const query=searchInput.value.trim();
    if(!API_CONFIGURED){searchResultsEl.hidden=false;searchResultsEl.replaceChildren(el("p",{class:"motelona-ai-search-result"},t("backendNotConfigured")));return;}
    if(!query){searchResultsEl.hidden=false;searchResultsEl.replaceChildren(el("p",{},t("noResults")));return;}
    const submit=searchForm.querySelector('[type="submit"]'); submit.disabled=true; searchResultsEl.hidden=false; searchResultsEl.replaceChildren(el("p",{},language==="vi"?"Đang tìm…":"Searching…"));
    try{
      const result=await fetchJson(API_BASE+"/api/ai/search",{method:"POST",body:JSON.stringify({query,websiteContext:pageContext(),language})},10000);
      searchResultsEl.replaceChildren();
      if(!Array.isArray(result.results)||result.results.length===0){searchResultsEl.appendChild(el("p",{},t("noResults")));}
      else{
        const heading=el("strong",{},language==="vi"?`Tìm thấy ${result.results.length} kết quả`:`Found ${result.results.length} result(s)`); searchResultsEl.appendChild(heading);
        result.results.forEach(match=>{searchResultsEl.appendChild(el("p",{class:"motelona-ai-search-result"},String(match.snippet||"")));});
      }
    }catch(_){searchResultsEl.replaceChildren(el("p",{class:"motelona-ai-search-result motelona-ai-search-result--error"},t("requestFailed")));}
    finally{submit.disabled=false;}
  });
  root.querySelector("[data-motelona-settings]").addEventListener("click",()=>{syncConsentUI();consentEl.classList.add("is-open");});
  root.querySelector("[data-cancel-consent]").addEventListener("click",()=>consentEl.classList.remove("is-open"));
  root.querySelector("[data-save-consent]").addEventListener("click",async()=>{
    const oldHistory=historyEnabled;
    historyEnabled=root.querySelector("[data-history-consent]").checked;
    memoryEnabled=root.querySelector("[data-memory-consent]").checked;
    let preferencesPersisted=true;
    try { localStorage.setItem("motelona-history-enabled",String(historyEnabled)); localStorage.setItem("motelona-memory-enabled",String(memoryEnabled)); }
    catch (_) { preferencesPersisted=false; }
    let historyDeleteFailed=false;
    if (oldHistory && !historyEnabled) {
      try { await fetchJson(API_BASE+"/api/ai/history/"+encodeURIComponent(sessionId),{method:"DELETE"},8000); }
      catch (_) { historyDeleteFailed=true; }
    }
    syncToggles(); consentEl.classList.remove("is-open");
    addMessage("assistant",preferencesPersisted?t("settingsSaved"):t("settingsNotPersisted"));
    if (historyDeleteFailed) addMessage("assistant",t("historyDisableDeleteFailed"),"",true);
    if (historyEnabled) loadSavedHistory();
    if (memoryEnabled) getMemoryItems();
  });
  root.querySelector("[data-motelona-history]").addEventListener("click",()=>{syncConsentUI();consentEl.classList.add("is-open");root.querySelector("[data-history-consent]").focus();});
  root.querySelector("[data-motelona-memory]").addEventListener("click",()=>{syncConsentUI();consentEl.classList.add("is-open");root.querySelector("[data-memory-consent]").focus();});
  root.querySelector("[data-export-data]").addEventListener("click",async()=>{
    try {
      const data=await fetchJson(API_BASE+"/api/ai/export/"+encodeURIComponent(sessionId),{method:"GET"},12000);
      const blob=new Blob([JSON.stringify(data,null,2)],{type:"application/json;charset=utf-8"});
      const objectUrl=URL.createObjectURL(blob); const link=el("a",{href:objectUrl,download:"motelona-data-export-"+new Date().toISOString().slice(0,10)+".json"});
      document.body.appendChild(link); link.click(); link.remove(); setTimeout(()=>URL.revokeObjectURL(objectUrl),1000);
      addMessage("assistant",language==="vi"?"Đã tạo tệp xuất dữ liệu JSON. Hãy giữ tệp này ở nơi riêng tư.":"Created the JSON data export. Keep the file private.");
    } catch (_) { addMessage("assistant",t("requestFailed"),"",true); }
  });
  root.querySelector("[data-delete-history]").addEventListener("click",async()=>{if(!confirm(t("confirmDeleteHistory")))return; try{await fetchJson(API_BASE+"/api/ai/history/"+encodeURIComponent(sessionId),{method:"DELETE"},8000);messages=[];messagesEl.replaceChildren();addMessage("assistant",t("historyDeleted"));}catch(_){addMessage("assistant",t("requestFailed"),"",true);}});
  root.querySelector("[data-delete-memories]").addEventListener("click",async()=>{if(!confirm(t("confirmDeleteMemories")))return;try{await fetchJson(API_BASE+"/api/ai/memories?sessionId="+encodeURIComponent(sessionId),{method:"DELETE"},8000);addMessage("assistant",t("memoriesDeleted"));}catch(_){addMessage("assistant",t("requestFailed"),"",true);}});
  root.querySelector("[data-motelona-clear]").addEventListener("click",async()=>{
    let historyDeleteFailed=false;
    if(historyEnabled){try{await fetchJson(API_BASE+"/api/ai/history/"+encodeURIComponent(sessionId),{method:"DELETE"},8000);}catch(_){historyDeleteFailed=true;}}
    messages=[];messagesEl.replaceChildren();addMessage("assistant",t("welcome"),"local");
    if(historyDeleteFailed) addMessage("assistant",t("historyDisableDeleteFailed"),"",true);
  });
  root.querySelector("[data-motelona-explain-selection]").addEventListener("click",()=>{const selected=String(window.getSelection ? window.getSelection() : "").trim(); if(!selected){addMessage("assistant",t("emptySelection"));return;} openPanel(); sendMessage(selected,"explain");});
  root.querySelector("[data-popover-close]").addEventListener("click",closePopover);
  root.querySelectorAll("[data-prompt]").forEach(button=>button.addEventListener("click",()=>sendMessage(t(button.dataset.prompt))));
  form.addEventListener("submit",event=>{event.preventDefault();const value=input.value;input.value="";sendMessage(value);});
  input.addEventListener("keydown",event=>{if(event.key==="Enter"&&!event.shiftKey){event.preventDefault();form.requestSubmit();}});
  document.addEventListener("click",event=>{
    if (event.target.closest(".motelona-ai-root") || event.target.closest("[data-motelona-open]")) return;
    const found=termAtPoint(event); if(!found) return;
    const termObject=terms.find(item=>item.term.toLowerCase()===found.term.toLowerCase() || item.en.toLowerCase()===found.term.toLowerCase() || item.vi.toLowerCase()===found.term.toLowerCase());
    if(!termObject)return;
    const definition=language==="vi"?termObject.viDef:termObject.enDef;
    const sample=termExamples[termObject.term] ? termExamples[termObject.term][language] : "";
    const normalizedContext=pageContext().toLocaleLowerCase();
    const isOnPage=normalizedContext.includes(termObject.term.toLocaleLowerCase());
    const contextNote=language==="vi" ? (isOnPage?"Liên hệ với nội dung trang: thuật ngữ này xuất hiện trong phần văn bản được cung cấp.":"Ghi chú dự án: thuật ngữ này không được xác nhận trong phần văn bản đang hiển thị; không nên kết luận rằng dự án đã dùng nó.") : (isOnPage?"Page context: this term appears in the supplied page text.":"Project note: this term was not found in the supplied page text, so its use in this project is not confirmed.");
    showTermPopover(found.term,definition+(sample?"\n\n"+(language==="vi"?"Ví dụ: ":"Example: ")+sample:"")+"\n\n"+contextNote,event.clientX+12,event.clientY+12);
  },true);
  window.addEventListener("resize",refreshTermHighlights);
  window.addEventListener("animfolio:language-change",event=>{setLanguage(event.detail && event.detail.language);});
  window.addEventListener("storage",event=>{if(event.key==="animfolio-language")setLanguage(event.newValue);});

  // Initial UI text is synced without mutating existing website text nodes.
  setLanguage(language); syncToggles(); syncWebSearchButton(); refreshTermHighlights(); applyPendingNavigationHighlight();
  if(historyEnabled) loadSavedHistory();
  checkHealth();
})();
