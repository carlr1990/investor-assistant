package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "valuations")
@Data
public class Valuation {
    @Id
    @Column(name = "valuation_id")
    private String valuationId;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "valuation_date")
    private LocalDate valuationDate;

    @Column(name = "share_price")
    private BigDecimal sharePrice;

    @Column(name = "company_valuation_m")
    private BigDecimal companyValuationM;

    @Column(name = "mark_source")
    private String markSource;

    @Column(name = "multiple_vs_entry")
    private BigDecimal multipleVsEntry;
}