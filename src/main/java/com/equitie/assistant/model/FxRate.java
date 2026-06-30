package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fx_rates")
@Data
public class FxRate {
    @Id
    private String currency;

    @Column(name = "to_usd")
    private BigDecimal toUsd;

    @Column(name = "as_of")
    private LocalDate asOf;
}