package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "investors")
@Data
public class Investor {
    @Id
    @Column(name = "investor_id")
    private String investorId;

    @Column(name = "investor_name")
    private String investorName;

    @Column(name = "investor_type")
    private String investorType;

    private String country;

    @Column(name = "reporting_currency")
    private String reportingCurrency;

    private Integer age;

    @Column(name = "tech_savviness")
    private String techSavviness;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "onboarded_date")
    private LocalDate onboardedDate;

    private String email;
}