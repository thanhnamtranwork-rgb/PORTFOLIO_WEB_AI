package com.motelona.assistant.controller;

import com.motelona.assistant.dto.ContactMessageRequest;
import com.motelona.assistant.service.PortfolioService;
import com.motelona.assistant.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolio;
    private final RateLimitService rateLimit;

    public PortfolioController(PortfolioService portfolio, RateLimitService rateLimit) {
        this.portfolio = portfolio;
        this.rateLimit = rateLimit;
    }

    @GetMapping("/status")
    public Map<String, Object> status() { return portfolio.status(); }

    @GetMapping("/public")
    public Map<String, Object> publicPortfolio() { return portfolio.publicPortfolio(); }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> saveContact(@RequestBody ContactMessageRequest request, HttpServletRequest http) {
        rateLimit.check("portfolio-contact-minute", http.getRemoteAddr(), 4, Duration.ofMinutes(15));
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.honeypot() != null && !request.honeypot().isBlank()) return ResponseEntity.ok(Map.of("received", true));
        long id = portfolio.saveContact(request.name(), request.email(), request.subject(), request.message());
        return ResponseEntity.ok(Map.of("received", true, "messageId", id));
    }
}
