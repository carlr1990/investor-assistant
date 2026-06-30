package com.equitie.assistant.service;

import com.equitie.assistant.model.Investor;
import com.equitie.assistant.repository.InvestorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class InvestorAssistantService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public String answerQuestion(String investorId, String question) {
        try {
            // 1. Get investor profile for personalization
            Investor investor = portfolioService.getInvestor(investorId);
            if (investor == null) {
                return "I couldn't find your investor profile. Please contact support.";
            }

            // 2. Get portfolio data
            PortfolioService.PortfolioSummary portfolio = portfolioService.getPortfolioSummary(investorId);
            List<PortfolioService.Obligation> obligations = portfolioService.getObligations(investorId);
            Map<String, Long> sectorCounts = portfolioService.getSectorCounts(investorId);
            Map<String, BigDecimal> sectorValues = portfolioService.getSectorValues(investorId);

            // 3. Generate personalized prompt
            String systemPrompt = buildSystemPrompt(investor, portfolio, obligations, sectorCounts, sectorValues);

            // 4. Create messages
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(new UserMessage(question));

            // 5. Configure AI options
            ChatOptions options = OpenAiChatOptions.builder()
                    .withModel("gpt-4o-mini")
                    .withTemperature(0.7f)
                    .withMaxTokens(1000)
                    .build();

            // 6. Get response
            Prompt prompt = new Prompt(messages, options);
            ChatResponse response = chatClient.call(prompt);

            return response.getResult().getOutput().getContent();

        } catch (Exception e) {
            log.error("Error processing question for investor {}: {}", investorId, e.getMessage(), e);
            return "I apologize, but I encountered an error processing your request. Please try again or contact support if the issue persists.";
        }
    }

    private String buildSystemPrompt(Investor investor,
                                     PortfolioService.PortfolioSummary portfolio,
                                     List<PortfolioService.Obligation> obligations,
                                     Map<String, Long> sectorCounts,
                                     Map<String, BigDecimal> sectorValues) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant for EquiTie, a venture capital firm. ");
        prompt.append("Your role is to help investors understand their portfolio and obligations. ");
        prompt.append("Always maintain confidentiality and only discuss this investor's data. ");
        prompt.append("Always cite numbers and their sources. ");
        prompt.append("Never provide investment advice or recommendations. ");
        prompt.append("If asked for investment advice, politely decline and offer to provide factual information instead. ");

        // Personalization based on investor profile
        String tone = determineTone(investor);
        prompt.append("\n\nUse a ").append(tone).append(" tone. ");

        if ("Low".equals(investor.getTechSavviness()) || (investor.getAge() != null && investor.getAge() > 60)) {
            prompt.append("Use plain language, avoid jargon, and explain any financial terms. ");
            prompt.append("Keep answers concise and clear, with explanations of key concepts. ");
            prompt.append("When mentioning MOIC, explain it means 'Multiple on Invested Capital' - how many times your investment has returned. ");
            prompt.append("When mentioning carry or performance fee, explain it's the performance-based fee taken from profits. ");
        } else if ("High".equals(investor.getTechSavviness()) && portfolio.getDealCount() > 5) {
            prompt.append("Use concise, data-dense responses. ");
            prompt.append("Assume financial fluency and don't explain basic terms. ");
            prompt.append("Include specific numbers, ratios, and percentages. ");
            prompt.append("Use professional financial terminology (MOIC, DPI, RVPI, carry). ");
        } else {
            prompt.append("Use professional but accessible language. ");
            prompt.append("Explain terms briefly when first used. ");
        }

        // Include investor profile
        prompt.append("\n\nInvestor profile:\n");
        prompt.append("- Name: ").append(investor.getInvestorName()).append("\n");
        prompt.append("- Type: ").append(investor.getInvestorType()).append("\n");
        prompt.append("- Age: ").append(investor.getAge() != null ? investor.getAge() : "N/A").append("\n");
        prompt.append("- Tech savviness: ").append(investor.getTechSavviness()).append("\n");
        prompt.append("- Reporting currency: ").append(investor.getReportingCurrency()).append("\n");
        prompt.append("- KYC Status: ").append(investor.getKycStatus()).append("\n");
        prompt.append("- Onboarded: ").append(investor.getOnboardedDate().format(DATE_FORMATTER)).append("\n");

        // Portfolio summary
        prompt.append("\nPortfolio summary as of ").append(portfolio.getReportDate().format(DATE_FORMATTER)).append(":\n");
        prompt.append("- Total committed: ").append(formatCurrency(portfolio.getTotalCommitted(), investor.getReportingCurrency())).append("\n");
        prompt.append("- Total contributed: ").append(formatCurrency(portfolio.getTotalContributed(), investor.getReportingCurrency())).append("\n");
        prompt.append("- Total current value: ").append(formatCurrency(portfolio.getTotalCurrentValue(), investor.getReportingCurrency())).append("\n");
        prompt.append("- Total distributions: ").append(formatCurrency(portfolio.getTotalDistributions(), investor.getReportingCurrency())).append("\n");
        prompt.append("- Total cost basis: ").append(formatCurrency(portfolio.getTotalCostBasis(), investor.getReportingCurrency())).append("\n");
        prompt.append("- Number of positions: ").append(portfolio.getDealCount()).append("\n");
        prompt.append("- Overall MOIC: ").append(portfolio.getOverallMoic()).append("x\n");
        prompt.append("- Overall DPI: ").append(portfolio.getOverallDpi()).append("x\n");
        prompt.append("- Overall RVPI: ").append(portfolio.getOverallRvpi()).append("x\n");

        // Sector concentration
        if (!sectorCounts.isEmpty()) {
            prompt.append("\nSector concentration (by count):\n");
            for (Map.Entry<String, Long> entry : sectorCounts.entrySet()) {
                prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" deals");
                if (sectorValues.containsKey(entry.getKey())) {
                    prompt.append(" (value: ").append(formatCurrency(sectorValues.get(entry.getKey()), investor.getReportingCurrency())).append(")");
                }
                prompt.append("\n");
            }
        }

        // Detailed positions
        prompt.append("\nDetailed positions (all values in ").append(investor.getReportingCurrency()).append("):\n");
        for (PortfolioService.PositionDetail position : portfolio.getPositions()) {
            prompt.append("- ").append(position.getCompanyName())
                    .append(" (").append(position.getRound()).append("):\n");
            prompt.append("  * Status: ").append(position.getStatus()).append("\n");
            prompt.append("  * MOIC: ").append(position.getMoic()).append("x\n");
            prompt.append("  * DPI: ").append(position.getDpi()).append("x\n");
            prompt.append("  * RVPI: ").append(position.getRvpi()).append("x\n");
            prompt.append("  * Current value: ").append(formatCurrency(position.getCurrentValue(), investor.getReportingCurrency())).append("\n");
            prompt.append("  * Cost basis: ").append(formatCurrency(position.getCostBasis(), investor.getReportingCurrency())).append("\n");
            prompt.append("  * Contributed: ").append(formatCurrency(position.getContributed(), investor.getReportingCurrency())).append("\n");
            prompt.append("  * Distributions: ").append(formatCurrency(position.getDistributions(), investor.getReportingCurrency())).append("\n");
            if (position.getEffectiveSharePrice() != null && position.getEntrySharePrice() != null) {
                prompt.append("  * Entry share price: ").append(position.getEntrySharePrice())
                        .append(" (").append(position.getDealCurrency()).append(")\n");
                prompt.append("  * Effective share price: ").append(position.getEffectiveSharePrice())
                        .append(" (").append(position.getDealCurrency()).append(")\n");
                if (position.getPriceDiscountPct() != null && position.getPriceDiscountPct().compareTo(BigDecimal.ZERO) > 0) {
                    prompt.append("  * Price discount: ").append(position.getPriceDiscountPct()).append("%\n");
                }
                if (position.getLatestSharePrice() != null) {
                    prompt.append("  * Latest share price: ").append(position.getLatestSharePrice())
                            .append(" (").append(position.getDealCurrency()).append(")\n");
                    prompt.append("  * Price change: ").append(position.getPriceChangePct()).append("%\n");
                }
            }
            if (position.getRealizedFraction() != null && position.getRealizedFraction().compareTo(BigDecimal.ZERO) > 0) {
                prompt.append("  * Realized: ").append(position.getRealizedFraction().multiply(new BigDecimal(100)))
                        .append("% of position\n");
            }
        }

        // Obligations
        if (!obligations.isEmpty()) {
            prompt.append("\nUpcoming obligations (all values in ").append(investor.getReportingCurrency()).append("):\n");
            for (PortfolioService.Obligation obligation : obligations) {
                prompt.append("- ").append(obligation.getType())
                        .append(" for ").append(obligation.getDealName())
                        .append("\n");
                prompt.append("  * Due: ").append(obligation.getDueDate().format(DATE_FORMATTER)).append("\n");
                prompt.append("  * Amount: ").append(formatCurrency(obligation.getAmount(), investor.getReportingCurrency()));
                if (!obligation.getOriginalCurrency().equals(investor.getReportingCurrency())) {
                    prompt.append(" (Original: ").append(formatCurrency(obligation.getOriginalAmount(), obligation.getOriginalCurrency())).append(")");
                }
                prompt.append("\n");
                prompt.append("  * Status: ").append(obligation.getStatus()).append("\n");
            }
        }

        // Important constraints
        prompt.append("\n\nImportant constraints:\n");
        prompt.append("1. ONLY discuss data for this investor. Never mention other investors or compare to others.\n");
        prompt.append("2. Always cite the source of numbers (e.g., 'portfolio summary as of June 25, 2026').\n");
        prompt.append("3. If you don't know something, say so and suggest what information would help.\n");
        prompt.append("4. Never provide investment advice, predictions, or recommendations.\n");
        prompt.append("5. Keep responses professional and grounded in the data provided.\n");
        prompt.append("6. If asked about specific calculations, show how you derived the numbers.\n");
        prompt.append("7. For fee questions, distinguish between deal-standard fees and the investor's effective fees (with discounts).\n");
        prompt.append("8. For multi-currency positions, note the original currency and the conversion rate used.\n");

        return prompt.toString();
    }

    private String determineTone(Investor investor) {
        if ("Low".equals(investor.getTechSavviness()) || (investor.getAge() != null && investor.getAge() > 60)) {
            return "helpful, patient, and educational";
        } else if ("High".equals(investor.getTechSavviness())) {
            return "professional, concise, and data-driven";
        } else {
            return "professional and clear";
        }
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) amount = BigDecimal.ZERO;
        return String.format("%s %,.2f", currency, amount);
    }
}