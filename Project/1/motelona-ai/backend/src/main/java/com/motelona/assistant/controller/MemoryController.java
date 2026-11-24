package com.motelona.assistant.controller;

import com.motelona.assistant.dto.MemoryRequest;
import com.motelona.assistant.dto.MemoryItem;
import com.motelona.assistant.dto.ConversationItem;
import com.motelona.assistant.memory.MemoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.Duration;
import jakarta.servlet.http.HttpServletRequest;
import com.motelona.assistant.service.RateLimitService;

@RestController
@RequestMapping("/api/ai")
public class MemoryController {
    private final MemoryService memoryService;
    private final com.motelona.assistant.service.PortfolioService portfolio;
    private final RateLimitService rateLimit;
    public MemoryController(MemoryService memoryService, com.motelona.assistant.service.PortfolioService portfolio, RateLimitService rateLimit){
        this.memoryService=memoryService; this.portfolio=portfolio; this.rateLimit=rateLimit;
    }

    @GetMapping("/memories")
    public Map<String,Object> listMemories(@RequestParam String sessionId, HttpServletRequest http){rateLimit.check("memory-read", http.getRemoteAddr(), 30, Duration.ofMinutes(1));List<MemoryItem> items=memoryService.list(validateSession(sessionId));return Map.of("items",items,"count",items.size());}

    @PostMapping("/memories")
    public Map<String,Object> saveMemory(@RequestBody MemoryRequest request, HttpServletRequest http){rateLimit.check("memory-write", http.getRemoteAddr(), 10, Duration.ofMinutes(1));
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if(!Boolean.TRUE.equals(request.consent()))throw new IllegalArgumentException("Explicit user consent is required to save a long-term memory.");
        MemoryItem item=memoryService.saveExplicitly(validateSession(request.sessionId()),request.content()); return Map.of("saved",true,"item",item);
    }

    @DeleteMapping("/memories/{id}")
    public Map<String,Object> deleteMemory(@PathVariable String id,@RequestParam String sessionId, HttpServletRequest http){rateLimit.check("memory-delete", http.getRemoteAddr(), 10, Duration.ofMinutes(1));memoryService.delete(validateSession(sessionId),id);return Map.of("deleted",true,"id",id);}

    @DeleteMapping("/memories")
    public Map<String,Object> deleteAllMemories(@RequestParam String sessionId, HttpServletRequest http){rateLimit.check("memory-delete-all", http.getRemoteAddr(), 5, Duration.ofMinutes(1));memoryService.deleteAll(validateSession(sessionId));return Map.of("deleted",true);}

    @GetMapping("/history/{sessionId}")
    public Map<String,Object> history(@PathVariable String sessionId, HttpServletRequest http){rateLimit.check("history-read", http.getRemoteAddr(), 30, Duration.ofMinutes(1));List<ConversationItem> items=portfolio.history(validateSession(sessionId));return Map.of("items",items,"count",items.size());}

    @DeleteMapping("/history/{sessionId}")
    public Map<String,Object> deleteHistory(@PathVariable String sessionId, HttpServletRequest http){rateLimit.check("history-delete", http.getRemoteAddr(), 10, Duration.ofMinutes(1));
        validateSession(sessionId);
        int count=portfolio.history(sessionId).size();
        boolean archiveDeleted=portfolio.deleteArchivedConversation(sessionId)>0;
        return Map.of("deleted",true,"count",count,"archiveDeleted",archiveDeleted);
    }

    @GetMapping("/export/{sessionId}")
    public Map<String,Object> exportData(@PathVariable String sessionId, HttpServletRequest http){rateLimit.check("history-export", http.getRemoteAddr(), 5, Duration.ofMinutes(1));
        validateSession(sessionId);
        List<ConversationItem> history=portfolio.history(sessionId);
        return Map.of("exportedAt",Instant.now().toString(),"sessionId",sessionId,"memories",memoryService.list(sessionId),"history",history,
                "note","This export contains data scoped to the current browser session. Keep the file private.");
    }

    private String validateSession(String value){String id=value==null?"":value.trim();if(!id.matches("[A-Za-z0-9._-]{4,100}"))throw new IllegalArgumentException("Invalid session ID format.");return id;}
}
