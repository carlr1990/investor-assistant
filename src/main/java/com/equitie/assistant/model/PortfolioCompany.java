package com.equitie.assistant.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "portfolio_companies")
@Data
public class PortfolioCompany {
    @Id
    @Column(name = "company_id")
    private String companyId;

    @Column(name = "company_name")
    private String companyName;

    private String sector;

    @Column(name = "hq_country")
    private String hqCountry;

    private String status;
    private String website;
}