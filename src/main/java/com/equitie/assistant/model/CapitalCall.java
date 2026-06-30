package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "capital_calls")
@Data
public class CapitalCall {
    @Id
    @Column(name = "call_id")
    private String callId;

    @Column(name = "allocation_id")
    private String allocationId;

    @Column(name = "investor_id")
    private String investorId;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "call_number")
    private Integer callNumber;

    @Column(name = "call_date")
    private LocalDate callDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private BigDecimal amount;
    private String currency;
    private String status;
}