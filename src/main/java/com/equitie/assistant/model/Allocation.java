package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "allocations")
@Data
public class Allocation {
    @Id
    @Column(name = "allocation_id")
    private String allocationId;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "investor_id")
    private String investorId;

    @Column(name = "deal_currency")
    private String dealCurrency;

    @Column(name = "commitment_amount")
    private BigDecimal commitmentAmount;

    @Column(name = "price_discount_pct")
    private BigDecimal priceDiscountPct;

    @Column(name = "effective_share_price")
    private BigDecimal effectiveSharePrice;

    private BigDecimal units;

    @Column(name = "contributed_amount")
    private BigDecimal contributedAmount;

    @Column(name = "outstanding_commitment")
    private BigDecimal outstandingCommitment;

    @Column(name = "mgmt_fee_pct")
    private BigDecimal mgmtFeePct;

    @Column(name = "performance_fee_pct")
    private BigDecimal performanceFeePct;

    @Column(name = "structuring_fee_pct")
    private BigDecimal structuringFeePct;

    @Column(name = "admin_fee_usd")
    private BigDecimal adminFeeUsd;

    @Column(name = "fee_discount")
    private String feeDiscount;

    @Column(name = "allocation_status")
    private String allocationStatus;

    @Column(name = "allocation_date")
    private LocalDate allocationDate;
}