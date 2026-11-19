(function () {
  "use strict";

  var translations = {
    vi: {
      "Skip to main content": "Bỏ qua để đến nội dung chính",
      "Home": "Trang chủ",
      "Trần Thành Nam — Home": "Trang chủ của Trần Thành Nam",
      "About": "Giới thiệu",
      "Education": "Học vấn",
      "Skills": "Kỹ năng",
      "Projects": "Dự án",
      "Stats": "Thống kê",
      "Contact": "Liên hệ",
      "LET'S CONNECT": "HÃY KẾT NỐI",
      "Let's build something meaningful.": "Cùng xây dựng những điều có ý nghĩa.",
      "Have a question, an idea, or an opportunity? Share a little context and let's start the conversation.": "Bạn có câu hỏi, ý tưởng hoặc cơ hội hợp tác? Hãy chia sẻ đôi chút để chúng ta bắt đầu cuộc trò chuyện.",
      "MESSAGE FORM": "GỬI TIN NHẮN",
      "Send me a message": "Gửi tin nhắn cho tôi",
      "Tell me a little about what you'd like to discuss.": "Hãy cho tôi biết đôi điều bạn muốn trao đổi.",
      "Your name": "Họ và tên của bạn",
      "you@example.com": "ban@example.com",
      "Tell me what you have in mind...": "Hãy chia sẻ điều bạn đang nghĩ đến...",
      "Thank you! Your message has been sent.": "Cảm ơn bạn! Tin nhắn đã được gửi.",
      "A GOOD PLACE TO START": "BẮT ĐẦU TỪ ĐÂY",
      "What's on your mind?": "Bạn đang nghĩ đến điều gì?",
      "Whether you're exploring an idea or looking to connect, a little context helps start a useful conversation.": "Dù bạn đang tìm hiểu một ý tưởng hay muốn kết nối, một chút bối cảnh sẽ giúp cuộc trò chuyện hữu ích hơn.",
      "Share the context": "Chia sẻ bối cảnh",
      "What are you building, learning, or curious about?": "Bạn đang xây dựng, học hỏi hoặc quan tâm đến điều gì?",
      "Explain your goal": "Chia sẻ mục tiêu",
      "Tell me what you'd like to understand or achieve.": "Bạn muốn tìm hiểu hoặc đạt được điều gì?",
      "Start simply": "Bắt đầu đơn giản",
      "One clear question or idea is a great beginning.": "Một câu hỏi hoặc ý tưởng rõ ràng là khởi đầu tốt.",
      "A thoughtful conversation starts with a simple message.": "Một cuộc trò chuyện hữu ích có thể bắt đầu bằng một tin nhắn đơn giản.",
      "Colors": "Màu sắc",
      "Green Canopy": "Tán cây xanh",
      "Peach Blossom": "Hoa đào",
      "Golden Apricot": "Hoa mai vàng",
      "Rainbow Spectrum": "Cầu vồng chuyển sắc",
      "Learning, building, growing every day.": "Học hỏi, xây dựng và phát triển mỗi ngày.",
      "View My Projects": "Xem dự án",
      "Contact Me": "Liên hệ với tôi",
      "About Me": "Giới thiệu bản thân",
      "I'm an Information Technology student who enjoys learning, adapting to new technologies, and solving problems with others. I'm building Full Stack skills across frontend, backend, and databases, while learning how to apply AI thoughtfully in software development. I value teamwork, open communication, and continuous improvement, and I aim to create useful, reliable technology in a rapidly evolving AI-driven world.": "Tôi là sinh viên Công nghệ thông tin ham học hỏi, chủ động thích nghi với công nghệ mới và yêu thích cùng mọi người giải quyết vấn đề. Tôi đang phát triển kỹ năng Full Stack ở cả frontend, backend và cơ sở dữ liệu, đồng thời tìm hiểu cách ứng dụng AI hợp lý trong quá trình phát triển phần mềm. Tôi đề cao tinh thần làm việc nhóm, giao tiếp cởi mở và cải thiện bản thân liên tục, hướng đến việc tạo ra những sản phẩm công nghệ hữu ích, đáng tin cậy trong thời đại AI phát triển nhanh chóng.",
      "Technical": "Kỹ thuật",
      "Academic": "Học tập & nghiên cứu",
      "Learning & Research": "Học tập & nghiên cứu",
      "Soft Skills": "Kỹ năng mềm",
      "Self-directed Learning": "Tự học chủ động",
      "Research & Information Literacy": "Tìm kiếm và đánh giá thông tin",
      "Critical Thinking & AI Verification": "Tư duy phản biện và kiểm chứng AI",
      "Idea Development": "Phát triển ý tưởng",
      "Problem Analysis": "Phân tích vấn đề",
      "Technical Presentation": "Thuyết trình và truyền đạt kỹ thuật",
      "Current technology focus": "Công nghệ đang tập trung",
      "AI brain illustration with glowing electronic circuits, representing the MoteLona assistant project": "Minh họa bộ não AI với mạch điện phát sáng, đại diện cho dự án trợ lý MoteLona",
      "Explore the MoteLona project overview": "Xem trang giới thiệu dự án MoteLona",
      "Teamwork": "Làm việc nhóm",
      "Problem Solving": "Giải quyết vấn đề",
      "Time Management": "Quản lý thời gian",
      "Featured Projects": "Dự án tiêu biểu",
      "An IoT-powered campus management system that monitors room occupancy, HVAC efficiency, and energy consumption. Won Best Capstone Project award and reduced energy costs by 18% in pilot.": "Hệ thống quản lý khuôn viên ứng dụng IoT để theo dõi tình trạng sử dụng phòng, hiệu suất HVAC và mức tiêu thụ năng lượng. Dự án giành giải Capstone xuất sắc nhất và giúp giảm 18% chi phí năng lượng trong giai đoạn thử nghiệm.",
      "Built in 36 hours at HackMIT. An app that matches students with leftover meal swipes to those in need. Won 1st place and the social impact award. Now used by 3 universities.": "Được phát triển trong 36 giờ tại HackMIT. Ứng dụng kết nối sinh viên còn lượt sử dụng bữa ăn với những người cần hỗ trợ. Dự án giành giải nhất và giải tác động xã hội, hiện được sử dụng tại 3 trường đại học.",
      "Undergraduate research on detecting and mitigating bias in ML-based hiring tools. Analyzed 5 commercial systems and proposed a fairness-aware re-ranking algorithm. Published at the FAccT student track.": "Nghiên cứu bậc đại học về phát hiện và giảm thiên lệch trong các công cụ tuyển dụng dùng học máy. Phân tích 5 hệ thống thương mại và đề xuất thuật toán xếp hạng lại có tính đến công bằng. Được công bố tại nhánh nghiên cứu sinh viên FAccT.",
      "A peer tutoring platform connecting upperclassmen with freshmen. Built as a volunteer project, now used by 500+ students. Features video calls, scheduling, and progress tracking.": "Nền tảng học tập đồng đẳng kết nối sinh viên khóa trên với sinh viên năm nhất. Dự án tình nguyện hiện có hơn 500 sinh viên sử dụng, với các tính năng gọi video, lên lịch và theo dõi tiến độ.",
      "Live Demo": "Xem bản chạy",
      "Live demo": "Xem bản chạy",
      "Source": "Mã nguồn",
      "Source code": "Mã nguồn",
      "Let's build something together": "Cùng nhau xây dựng điều gì đó",
      "I'm open to collaboration, Full Stack projects, and opportunities to build with AI. Let's connect!": "Tôi sẵn sàng hợp tác, tham gia các dự án Full Stack và phát triển sản phẩm với AI. Hãy kết nối với tôi!",
      "Name": "Họ và tên",
      "Name is required.": "Vui lòng nhập họ tên.",
      "Valid email is required.": "Vui lòng nhập email hợp lệ.",
      "Message": "Nội dung",
      "Message is required.": "Vui lòng nhập nội dung.",
      "Present": "Hiện tại",
      "Other": "Khác",
      "Send Message": "Gửi tin nhắn",
      "Scan to save my contact": "Quét mã để lưu thông tin liên hệ",
      "Download vCard": "Tải danh thiếp vCard",
      "Information Technology Student": "Sinh viên Công nghệ thông tin",
      "Hanoi University of Business and Technology (HUBT)": "Trường Đại học Kinh doanh và Công nghệ Hà Nội (HUBT)",
      "Class K30 · TH30.03": "Lớp K30 · TH30.03",
      "Studying Information Technology at HUBT has introduced me to a broad range of foundational topics, including programming, software development, databases, and system analysis. I actively build self-learning habits, explore unfamiliar topics, connect knowledge across courses, and apply what I learn in assignments and projects. My current direction is Full Stack development, Java, SQL/MySQL/NoSQL, and the responsible use of AI in software development.": "Học Công nghệ thông tin tại HUBT giúp tôi từng bước tiếp cận kiến thức nền tảng ở nhiều mảng như lập trình, phát triển phần mềm, cơ sở dữ liệu và phân tích hệ thống. Tôi chú trọng rèn luyện khả năng tự học, chủ động tìm hiểu chủ đề mới, kết nối kiến thức giữa các môn học và vận dụng vào bài tập, dự án. Định hướng hiện tại của tôi là phát triển Full Stack, Java, SQL/MySQL/NoSQL và tìm hiểu cách ứng dụng AI có trách nhiệm trong phát triển phần mềm.",
      "Thank you! I'll get back to you soon.": "Cảm ơn bạn! Tôi sẽ phản hồi sớm nhất có thể.",
      "Toggle navigation": "Mở hoặc đóng điều hướng",
      "Toggle dark mode": "Chuyển chế độ sáng/tối",
      "Toggle light and dark theme": "Chuyển chế độ sáng và tối",
      "Visit my LinkedIn profile": "Xem hồ sơ LinkedIn của tôi",
      "Visit my GitHub profile": "Xem hồ sơ GitHub của tôi",
      "Visit my Twitter profile": "Xem hồ sơ Twitter của tôi",
      "Visit my Dribbble profile": "Xem hồ sơ Dribbble của tôi",
      "Send me an email": "Gửi email cho tôi",
      "Passionate about technology and innovation.": "Đam mê công nghệ và đổi mới sáng tạo.",
      "Learning, building, growing every day.": "Học hỏi, xây dựng và phát triển mỗi ngày.",
      "Turning ideas into useful digital experiences.": "Biến ý tưởng thành trải nghiệm số hữu ích.",
      "Creating, learning, and improving with every project.": "Sáng tạo, học hỏi và cải tiến qua từng dự án.",
      "Choose color scheme": "Chọn bảng màu",
      "Prefer email?": "Bạn muốn gửi email trực tiếp?",
      "Progress Bars": "Thanh tiến trình",
      "SmartCampus — Capstone Project": "SmartCampus — Đồ án tổng kết",
      "MealBuddy — Hackathon Winner": "MealBuddy — Dự án đoạt giải Hackathon",
      "Bias in Hiring Algorithms — Research Paper": "Thiên lệch trong thuật toán tuyển dụng — Bài nghiên cứu",
      "StudyTogether — Volunteer Platform": "StudyTogether — Nền tảng tình nguyện",
      "Choose website language": "Chọn ngôn ngữ website",
      "Portfolio navigation": "Điều hướng danh mục",
      "Color schemes": "Bảng màu",
      "A place to start a conversation": "Nơi bắt đầu cuộc trò chuyện",
      "Email": "Email",
      "HUBT · INFORMATION TECHNOLOGY · ABOUT ME": "HUBT · CÔNG NGHỆ THÔNG TIN · GIỚI THIỆU BẢN THÂN",
      "CURRENT FOCUS": "ĐANG TẬP TRUNG",
      "01 / LEARN": "01 / HỌC HỎI",
      "Build the basics": "Xây nền tảng vững chắc",
      "Curiosity into knowledge": "Biến tò mò thành kiến thức",
      "02 / CREATE": "02 / SÁNG TẠO",
      "Make it useful": "Tạo ra giá trị hữu ích",
      "Ideas into real projects": "Biến ý tưởng thành dự án",
      "03 / AI TOOLS": "03 / CÔNG CỤ AI",
      "Explore AI tools": "Khám phá công cụ AI",
      "Turn tools into smarter workflows": "Biến công cụ thành cách làm thông minh"
    }
  };

  var originalText = new WeakMap();
  var textNodes = [];
  var originalAttributes = new WeakMap();
  var attributeElements = [];
  var originalDocumentTitle = document.title;
  var activeLanguage = "en";

  function collectTextNodes(scope) {
    var root = scope || document.querySelector(".animfolio-container");
    if (!root) return false;
    var changed = false;
    function remember(node) {
      if (!node || originalText.has(node) || !node.nodeValue || !node.nodeValue.trim()) return;
      var parent = node.parentElement;
      if (!parent || /^(SCRIPT|STYLE|NOSCRIPT|TEXTAREA)$/i.test(parent.tagName)) return;
      if (parent.closest(".animfolio-language-switch, [data-animfolio-typed]")) return;
      originalText.set(node, node.nodeValue);
      textNodes.push(node);
      changed = true;
    }
    if (root.nodeType === Node.TEXT_NODE) {
      remember(root);
      return changed;
    }
    var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
      acceptNode: function (node) {
        if (!node.nodeValue || !node.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
        var parent = node.parentElement;
        if (!parent || /^(SCRIPT|STYLE|NOSCRIPT|TEXTAREA)$/i.test(parent.tagName)) return NodeFilter.FILTER_REJECT;
        if (parent.closest(".animfolio-language-switch, [data-animfolio-typed]")) return NodeFilter.FILTER_REJECT;
        return NodeFilter.FILTER_ACCEPT;
      }
    });
    while (walker.nextNode()) {
      remember(walker.currentNode);
    }
    return changed;
  }

  function collectAttributes(scope) {
    var root = scope || document.querySelector(".animfolio-container");
    if (!root) return;
    var selector = "[aria-label], [data-tooltip], [title], [alt]";
    var elements = [];
    if (root.matches && root.matches(selector)) elements.push(root);
    if (root.querySelectorAll) elements = elements.concat(Array.from(root.querySelectorAll(selector)));
    elements.forEach(function (element) {
      if (originalAttributes.has(element)) return;
      var values = {};
      ["aria-label", "data-tooltip", "title", "alt"].forEach(function (attribute) {
        if (element.hasAttribute(attribute)) values[attribute] = element.getAttribute(attribute);
      });
      originalAttributes.set(element, values);
      attributeElements.push(element);
    });
  }

  function translate(language, dispatchChange) {
    activeLanguage = language;
    var dictionary = translations[language] || {};
    textNodes.forEach(function (node) {
      var original = originalText.get(node);
      if (original == null) return;
      var trimmed = original.trim();
      if (language === "en") {
        node.nodeValue = original;
      } else if (Object.prototype.hasOwnProperty.call(dictionary, trimmed)) {
        node.nodeValue = original.replace(trimmed, dictionary[trimmed]);
      } else {
        var translated = original;
        Object.keys(dictionary).filter(function (english) { return english.length > 8; })
          .sort(function (a, b) { return b.length - a.length; })
          .forEach(function (english) {
            if (translated.indexOf(english) !== -1) {
              translated = translated.split(english).join(dictionary[english]);
            }
          });
        node.nodeValue = translated;
      }
    });

    document.documentElement.lang = language;
    document.querySelectorAll("meta[data-en][data-vi]").forEach(function (meta) {
      meta.setAttribute("content", meta.getAttribute("data-" + language));
    });
    attributeElements.forEach(function (element) {
      var originals = originalAttributes.get(element) || {};
      Object.keys(originals).forEach(function (attribute) {
        var original = originals[attribute];
        var translated = dictionary[original];
        element.setAttribute(attribute, language === "vi" && translated ? translated : original);
      });
    });

    // The browser tab title sits outside the portfolio container, so translate it separately.
    document.title = language === "vi"
      ? originalDocumentTitle
          .replace(" — Full-Stack Developer | Portfolio", " — Lập trình viên Full Stack | Hồ sơ cá nhân")
          .replace("Green Canopy", dictionary["Green Canopy"])
          .replace("Peach Blossom", dictionary["Peach Blossom"])
          .replace("Golden Apricot", dictionary["Golden Apricot"])
      : originalDocumentTitle;
    document.querySelectorAll(".animfolio-language-switch__button").forEach(function (button) {
      var selected = button.getAttribute("data-language") === language;
      button.classList.toggle("is-active", selected);
      button.setAttribute("aria-pressed", selected ? "true" : "false");
    });

    var nameInput = document.querySelector('input[name="name"], input[placeholder="Name"]');
    if (nameInput) nameInput.placeholder = language === "vi" ? "Họ và tên của bạn" : "Your name";
    var emailInput = document.querySelector('input[type="email"]');
    if (emailInput) emailInput.placeholder = language === "vi" ? "ban@example.com" : "you@example.com";
    var messageInput = document.querySelector("textarea");
    if (messageInput) messageInput.placeholder = language === "vi" ? "Hãy chia sẻ điều bạn đang nghĩ đến..." : "Tell me what you have in mind...";
    if (dispatchChange !== false) window.dispatchEvent(new CustomEvent("animfolio:language-change", { detail: { language: language } }));
    try { localStorage.setItem("animfolio-language", language); } catch (e) {}
  }

  function init() {
    collectTextNodes();
    collectAttributes();
    var contentRoot = document.querySelector(".animfolio-container");
    if (contentRoot && window.MutationObserver) {
      var observer = new MutationObserver(function (records) {
        var changed = false;
        records.forEach(function (record) {
          record.addedNodes.forEach(function (node) {
            if (node.nodeType !== Node.TEXT_NODE && node.nodeType !== Node.ELEMENT_NODE) return;
            changed = collectTextNodes(node) || changed;
            collectAttributes(node);
          });
        });
        if (changed) translate(activeLanguage, false);
      });
      observer.observe(contentRoot, { childList: true, subtree: true });
    }
    document.querySelectorAll(".animfolio-language-switch__button").forEach(function (button) {
      button.addEventListener("click", function () {
        translate(button.getAttribute("data-language") === "vi" ? "vi" : "en");
      });
    });
    var saved = "en";
    try { saved = localStorage.getItem("animfolio-language") || "en"; } catch (e) {}
    translate(saved === "vi" ? "vi" : "en");
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
