package com.equitie.assistant.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class AccountStatement {
    private String investorId;
    private String investorName;
    private String reportingCurrency;
    private int year;
    private LocalDate statementDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StatementLine> lines;
    private BigDecimal totalOutgoing;
    private BigDecimal totalIncoming;
    private BigDecimal netPosition;
    private Map<String, BigDecimal> totalsByType;
    private Map<String, BigDecimal> totalsByCurrency;
    private Map<String, BigDecimal> totalsByDeal;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private String kycStatus;
    private String investorType;
    private String country;

    // Summary statistics
    private int totalTransactions;
    private int outgoingTransactions;
    private int incomingTransactions;
    private BigDecimal averageTransactionSize;
    private BigDecimal largestOutgoing;
    private BigDecimal largestIncoming;
    private LocalDate lastTransactionDate;

    // For personalization
    private String preferredFormat; // "detailed", "summary", "executive"

    // Helper methods
    public BigDecimal getNetCashFlow() {
        return totalIncoming != null && totalOutgoing != null
                ? totalIncoming.subtract(totalOutgoing)
                : BigDecimal.ZERO;
    }

    public int getTransactionCount() {
        return lines != null ? lines.size() : 0;
    }

    public List<StatementLine> getOutgoingLines() {
        return lines != null ? lines.stream()
                .filter(StatementLine::isOutgoing)
                .collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();
    }

    public List<StatementLine> getIncomingLines() {
        return lines != null ? lines.stream()
                .filter(StatementLine::isIncoming)
                .collect(java.util.stream.Collectors.toList())
                : (List<StatementLine>) java.util.stream.Collectors.toList();
    }
}