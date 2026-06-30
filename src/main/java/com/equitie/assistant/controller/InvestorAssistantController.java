package com.equitie.assistant.controller;

import com.equitie.assistant.service.InvestorAssistantService;
import com.equitie.assistant.service.PortfolioService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "*")
public class InvestorAssistantController {

    @Autowired
    private InvestorAssistantService assistantService;

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = assistantService.answerQuestion(request.getInvestorId(), request.getQuestion());
        return new ChatResponse(answer);
    }

    @GetMapping("/{investorId}/portfolio")
    public PortfolioService.PortfolioSummary getPortfolioSummary(@PathVariable String investorId) {
        return portfolioService.getPortfolioSummary(investorId);
    }

    @GetMapping("/{investorId}/obligations")
    public List<PortfolioService.Obligation> getObligations(@PathVariable String investorId) {
        return portfolioService.getObligations(investorId);
    }

    @GetMapping("/{investorId}/sectors")
    public Map<String, Long> getSectorCounts(@PathVariable String investorId) {
        return portfolioService.getSectorCounts(investorId);
    }

    @Data
    public static class ChatRequest {
        private String investorId;
        private String question;
    }

    @Data
    public static class ChatResponse {
        private final String answer;
    }
}