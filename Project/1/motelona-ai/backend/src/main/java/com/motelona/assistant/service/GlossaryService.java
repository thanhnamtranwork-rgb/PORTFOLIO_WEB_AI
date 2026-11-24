package com.motelona.assistant.service;

import org.springframework.stereotype.Service;
import com.motelona.assistant.ai.TextVectorizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GlossaryService {
    private final Map<String, Definition> glossary = new LinkedHashMap<>();
    public GlossaryService() {
        add("API", "A defined way for software to request data or actions from another program.", "Cách thức được quy định để phần mềm yêu cầu dữ liệu hoặc chức năng từ chương trình khác.");
        add("REST API", "A web interface that uses standard HTTP methods to exchange resources between systems.", "Giao diện web dùng các phương thức HTTP chuẩn để trao đổi tài nguyên giữa các hệ thống.");
        add("Backend", "The server-side part of an application that handles business rules, data, and service connections.", "Phần máy chủ của ứng dụng xử lý nghiệp vụ, dữ liệu và kết nối dịch vụ.");
        add("Frontend", "The user interface and browser-side code people see and interact with.", "Giao diện và mã chạy trong trình duyệt mà người dùng nhìn thấy, tương tác.");
        add("LLM", "A large language model trained to understand and generate language.", "Mô hình ngôn ngữ lớn được huấn luyện để hiểu và tạo ngôn ngữ.");
        add("RAG", "Retrieval-Augmented Generation: retrieve relevant source material and give it to a model as context before generating an answer.", "Retrieval-Augmented Generation: truy xuất tài liệu liên quan và đưa làm ngữ cảnh cho mô hình trước khi tạo câu trả lời.");
        add("Embedding", "A vector representation of data used to compare semantic similarity.", "Biểu diễn dữ liệu thành vector để so sánh độ tương đồng về ngữ nghĩa.");
        add("Token", "A unit of text processed by a model, such as a word, part of a word, or punctuation.", "Đơn vị văn bản được mô hình xử lý, ví dụ một từ, phần từ hoặc dấu câu.");
        add("Vector Database", "A database that stores vectors and searches for nearby representations, often by semantic similarity.", "Cơ sở dữ liệu lưu vector và tìm những biểu diễn gần nhau, thường dựa trên tương đồng ngữ nghĩa.");
        add("Spring Boot", "A Java framework that simplifies creating stand-alone Spring applications with sensible defaults.", "Framework Java giúp tạo ứng dụng Spring độc lập với các cấu hình mặc định hợp lý.");
        add("Spring AI", "A Spring ecosystem project with abstractions and integrations for building AI-powered Java applications.", "Dự án trong hệ sinh thái Spring cung cấp lớp trừu tượng và tích hợp để xây ứng dụng Java có AI.");
        add("MongoDB", "A document database that stores flexible BSON documents in collections and supports indexes for common queries.", "Cơ sở dữ liệu dạng tài liệu, lưu BSON linh hoạt trong collection và hỗ trợ index cho các truy vấn thường dùng.");
        add("Gemini API", "Google's API for generating model responses from system instructions and user input.", "API của Google dùng để tạo câu trả lời từ chỉ dẫn hệ thống và nội dung người dùng gửi lên.");
        add("AI Agent", "An AI system that selects from a controlled set of tools to work toward a task.", "Hệ thống AI chọn dùng một bộ công cụ được kiểm soát để giải quyết nhiệm vụ.");
        add("Java", "A statically typed, object-oriented programming language that commonly runs on the Java Virtual Machine.", "Ngôn ngữ lập trình hướng đối tượng, kiểu tĩnh, thường chạy trên Java Virtual Machine.");
        add("DOM", "The browser's object model of a document, which JavaScript can inspect and update.", "Mô hình đối tượng của tài liệu trong trình duyệt, cho phép JavaScript đọc và cập nhật.");
        add("HTML", "The markup language that defines a web page's structure and content.", "Ngôn ngữ đánh dấu xác định cấu trúc và nội dung trang web.");
        add("CSS", "The style language that controls the appearance and layout of web content.", "Ngôn ngữ kiểu dáng điều khiển giao diện và bố cục nội dung web.");
        add("JavaScript", "A programming language often used to make web pages interactive.", "Ngôn ngữ lập trình thường dùng để tạo tính tương tác cho trang web.");
        add("CORS", "A browser security mechanism controlling which origins can read cross-origin responses.", "Cơ chế bảo mật trình duyệt kiểm soát nguồn nào được đọc phản hồi từ origin khác.");
        add("JSON", "A text format for representing structured data exchanged between applications.", "Định dạng văn bản biểu diễn dữ liệu có cấu trúc được trao đổi giữa ứng dụng.");
        add("Prompt Injection", "An attack where untrusted content tries to override an AI system's intended instructions.", "Kiểu tấn công trong đó nội dung không đáng tin cố ghi đè chỉ dẫn của hệ thống AI.");
        add("Tool Calling", "A model output that requests a registered function; the application validates and executes that function.", "Đầu ra của mô hình yêu cầu một hàm đã đăng ký; ứng dụng kiểm tra và thực thi hàm đó.");
        add("NLP", "Natural Language Processing: methods that let software process, analyze, classify, and retrieve human language.", "Xử lý ngôn ngữ tự nhiên: các phương pháp giúp phần mềm xử lý, phân tích, phân loại và truy xuất ngôn ngữ của con người.");
        add("TF-IDF", "Term Frequency–Inverse Document Frequency: a weighting method that highlights terms that are important to a document compared with a collection of documents.", "Phương pháp gán trọng số từ dựa trên tần suất xuất hiện trong tài liệu và mức độ hiếm của từ trong toàn bộ tập tài liệu.");
        add("Cosine Similarity", "A measure of similarity between two vectors based on the cosine of the angle between them; it is commonly used in text retrieval.", "Thước đo độ tương đồng giữa hai vector dựa trên cosine của góc giữa chúng; thường được dùng trong tìm kiếm văn bản.");
        add("Intent Classification", "The task of assigning a user message to an intent category, such as asking about skills, projects, or contact details.", "Nhiệm vụ phân loại tin nhắn người dùng vào một nhóm ý định, chẳng hạn hỏi về kỹ năng, dự án hoặc thông tin liên hệ.");
        add("Tokenization", "The process of splitting text into smaller units such as words or tokens before further language processing.", "Quá trình tách văn bản thành các đơn vị nhỏ hơn như từ hoặc token trước khi xử lý ngôn ngữ.");
        add("Knowledge Base", "A structured collection of trusted information that an application can search to answer domain-specific questions.", "Tập hợp thông tin có cấu trúc và được kiểm soát mà ứng dụng có thể tìm kiếm để trả lời câu hỏi theo lĩnh vực.");
        add("Machine Learning", "A branch of AI in which algorithms learn patterns from data to make predictions or decisions; not every rule-based AI system uses machine learning.", "Một nhánh của AI trong đó thuật toán học quy luật từ dữ liệu để dự đoán hoặc đưa ra quyết định; không phải hệ thống AI dựa trên luật nào cũng dùng Machine Learning.");
        add("AI Model", "A computational model that has been trained or designed to map inputs to outputs, such as classifications, predictions, or generated text.", "Mô hình tính toán được huấn luyện hoặc thiết kế để ánh xạ đầu vào thành đầu ra như phân loại, dự đoán hoặc văn bản được tạo ra.");
        add("Portfolio fallback", "A limited response mode based on local rules, glossary entries, and retrieved Portfolio facts; it is not a generative language model.", "Chế độ trả lời giới hạn dựa trên luật cục bộ, từ điển và dữ kiện Portfolio được truy xuất; đây không phải mô hình ngôn ngữ tạo sinh.");
    }
    private void add(String term, String en, String vi) { glossary.put(term.toLowerCase(Locale.ROOT), new Definition(term,en,vi)); }
    public Definition find(String term) {
        if (term == null || term.isBlank()) return null;
        String key = TextVectorizer.normalize(term).toLowerCase(Locale.ROOT);
        Definition exact = glossary.values().stream()
                .filter(d -> TextVectorizer.normalize(d.term()).equals(key)).findFirst().orElse(null);
        if (exact != null) return exact;
        return glossary.values().stream().sorted((a, b) -> Integer.compare(b.term().length(), a.term().length())).filter(d -> {
            String candidate = TextVectorizer.normalize(d.term());
            return key.contains(candidate) || candidate.contains(key);
        }).findFirst().orElse(null);
    }

    public List<Definition> findMentioned(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = " " + TextVectorizer.normalize(text) + " ";
        List<Definition> found = glossary.values().stream()
                .sorted((a, b) -> Integer.compare(b.term().length(), a.term().length()))
                .filter(definition -> normalized.contains(" " + TextVectorizer.normalize(definition.term()) + " "))
                .toList();
        return found.stream().filter(candidate -> found.stream().noneMatch(longer ->
                longer != candidate && longer.term().length() > candidate.term().length()
                        && TextVectorizer.normalize(longer.term()).contains(TextVectorizer.normalize(candidate.term())))).toList();
    }

    public Map<String,String> publicTerms() { Map<String,String> result=new LinkedHashMap<>(); glossary.values().forEach(d->result.put(d.term(),d.en())); return result; }
    public record Definition(String term, String en, String vi) { }
}
