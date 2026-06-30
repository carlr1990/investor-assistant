package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "distributions")
@Data
public class Distribution {
    @Id
    @Column(name = "distribution_id")
    private String distributionId;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "allocation_id")
    private String allocationId;

    @Column(name = "investor_id")
    private String investorId;

    @Column(name = "distribution_date")
    private LocalDate distributionDate;

    @Column(name = "distribution_type")
    private String distributionType;

    @Column(name = "gross_amount")
    private BigDecimal grossAmount;

    @Column(name = "performance_fee_pct")
    private BigDecimal performanceFeePct;

    @Column(name = "performance_fee_amount")
    private BigDecimal performanceFeeAmount;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    private String currency;

    @Column(name = "fraction_of_units")
    private BigDecimal fractionOfUnits;
}