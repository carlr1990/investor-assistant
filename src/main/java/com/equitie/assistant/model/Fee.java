package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fees")
@Data
public class Fee {
    @Id
    @Column(name = "fee_id")
    private String feeId;

    @Column(name = "allocation_id")
    private String allocationId;

    @Column(name = "investor_id")
    private String investorId;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "fee_type")
    private String feeType;

    private Integer period;

    @Column(name = "fee_rate_pct")
    private BigDecimal feeRatePct;

    private String basis;
    private BigDecimal amount;
    private String currency;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private String status;
}