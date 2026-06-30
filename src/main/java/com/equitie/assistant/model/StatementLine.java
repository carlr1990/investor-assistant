package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "statement_lines")
@Data
public class StatementLine {
    @Id
    @Column(name = "line_id")
    private String lineId;

    @Column(name = "investor_id")
    private String investorId;

    private LocalDate date;

    private String type; // Capital Contribution, Management Fee, etc.

    @Column(name = "deal_id")
    private String dealId;

    private BigDecimal amount; // Signed amount (negative = cash out, positive = cash in)

    private String currency;

    @Column(name = "reference_id")
    private String referenceId; // Points back to call/fee/distribution

    // Helper methods
    public boolean isOutgoing() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncoming() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getTypeDescription() {
        return switch (type) {
            case "Capital Contribution" -> "Capital Contribution";
            case "Management Fee" -> "Management Fee";
            case "Structuring Fee" -> "Structuring Fee";
            case "Admin Fee" -> "Admin Fee";
            case "Exit Proceeds" -> "Exit Proceeds";
            case "Secondary Sale" -> "Secondary Sale";
            default -> type;
        };
    }
}