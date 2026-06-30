package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deals")
@Data
public class Deal {
    @Id
    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "company_name")
    private String companyName;

    private String round;
    private String instrument;

    @Column(name = "spv_name")
    private String spvName;

    @Column(name = "deal_currency")
    private String dealCurrency;

    @Column(name = "deal_date")
    private LocalDate dealDate;

    @Column(name = "pre_money_valuation_m")
    private BigDecimal preMoneyValuationM;

    @Column(name = "post_money_valuation_m")
    private BigDecimal postMoneyValuationM;

    @Column(name = "round_size_m")
    private BigDecimal roundSizeM;

    @Column(name = "equitie_allocation_m")
    private BigDecimal equitieAllocationM;

    @Column(name = "entry_share_price")
    private BigDecimal entrySharePrice;

    @Column(name = "contributed_pct")
    private BigDecimal contributedPct;

    @Column(name = "std_mgmt_fee_pct")
    private BigDecimal stdMgmtFeePct;

    @Column(name = "std_performance_fee_pct")
    private BigDecimal stdPerformanceFeePct;

    @Column(name = "std_structuring_fee_pct")
    private BigDecimal stdStructuringFeePct;

    @Column(name = "std_admin_fee_usd")
    private BigDecimal stdAdminFeeUsd;

    private String status;
}