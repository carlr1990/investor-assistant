package com.equitie.assistant.config;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.equitie.assistant.model.*;
import com.equitie.assistant.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataLoader {

    @Autowired
    private InvestorRepository investorRepository;
    @Autowired
    private PortfolioCompanyRepository companyRepository;
    @Autowired
    private DealRepository dealRepository;
    @Autowired
    private AllocationRepository allocationRepository;
    @Autowired
    private ValuationRepository valuationRepository;
    @Autowired
    private CapitalCallRepository capitalCallRepository;
    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private DistributionRepository distributionRepository;
    @Autowired
    private StatementLineRepository statementLineRepository;
    @Autowired
    private FxRateRepository fxRateRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostConstruct
    @Transactional
    public void loadData() throws Exception {
        loadInvestors();
        loadPortfolioCompanies();
        loadDeals();
        loadAllocations();
        loadValuations();
        loadCapitalCalls();
        loadFees();
        loadDistributions();
        loadStatementLines();
        loadFxRates();
        System.out.println("Data loaded successfully!");
        System.out.println("FX Rates loaded: " + fxRateRepository.count());
    }

    private void loadInvestors() throws Exception {
        List<Investor> investors = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/investors.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Investor investor = new Investor();
                investor.setInvestorId(line[0]);
                investor.setInvestorName(line[1]);
                investor.setInvestorType(line[2]);
                investor.setCountry(line[3]);
                investor.setReportingCurrency(line[4]);
                investor.setAge(line[5].isEmpty() ? null : Integer.parseInt(line[5]));
                investor.setTechSavviness(line[6]);
                investor.setKycStatus(line[7]);
                investor.setOnboardedDate(LocalDate.parse(line[8], dateFormatter));
                investor.setEmail(line[9]);
                investors.add(investor);
            }
        }
        investorRepository.saveAll(investors);
        System.out.println("Loaded " + investors.size() + " investors");
    }

    private void loadPortfolioCompanies() throws Exception {
        List<PortfolioCompany> companies = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/portfolio_companies.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                PortfolioCompany company = new PortfolioCompany();
                company.setCompanyId(line[0]);
                company.setCompanyName(line[1]);
                company.setSector(line[2]);
                company.setHqCountry(line[3]);
                company.setStatus(line[4]);
                company.setWebsite(line[5]);
                companies.add(company);
            }
        }
        companyRepository.saveAll(companies);
        System.out.println("Loaded " + companies.size() + " portfolio companies");
    }

    private void loadDeals() throws Exception {
        List<Deal> deals = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/deals.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Deal deal = new Deal();
                deal.setDealId(line[0]);
                deal.setCompanyId(line[1]);
                deal.setCompanyName(line[2]);
                deal.setRound(line[3]);
                deal.setInstrument(line[4]);
                deal.setSpvName(line[5]);
                deal.setDealCurrency(line[6]);
                deal.setDealDate(LocalDate.parse(line[7], dateFormatter));
                deal.setPreMoneyValuationM(new BigDecimal(line[8]));
                deal.setPostMoneyValuationM(new BigDecimal(line[9]));
                deal.setRoundSizeM(new BigDecimal(line[10]));
                deal.setEquitieAllocationM(new BigDecimal(line[11]));
                deal.setEntrySharePrice(new BigDecimal(line[12]));
                deal.setContributedPct(new BigDecimal(line[13]));
                deal.setStdMgmtFeePct(new BigDecimal(line[14]));
                deal.setStdPerformanceFeePct(new BigDecimal(line[15]));
                deal.setStdStructuringFeePct(new BigDecimal(line[16]));
                deal.setStdAdminFeeUsd(new BigDecimal(line[17]));
                deal.setStatus(line[18]);
                deals.add(deal);
            }
        }
        dealRepository.saveAll(deals);
        System.out.println("Loaded " + deals.size() + " deals");
    }

    private void loadAllocations() throws Exception {
        List<Allocation> allocations = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/allocations.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Allocation allocation = new Allocation();
                allocation.setAllocationId(line[0]);
                allocation.setDealId(line[1]);
                allocation.setInvestorId(line[2]);
                allocation.setDealCurrency(line[3]);
                allocation.setCommitmentAmount(new BigDecimal(line[4]));
                allocation.setPriceDiscountPct(new BigDecimal(line[5]));
                allocation.setEffectiveSharePrice(new BigDecimal(line[6]));
                allocation.setUnits(new BigDecimal(line[7]));
                allocation.setContributedAmount(new BigDecimal(line[8]));
                allocation.setOutstandingCommitment(new BigDecimal(line[9]));
                allocation.setMgmtFeePct(new BigDecimal(line[10]));
                allocation.setPerformanceFeePct(new BigDecimal(line[11]));
                allocation.setStructuringFeePct(new BigDecimal(line[12]));
                allocation.setAdminFeeUsd(new BigDecimal(line[13]));
                allocation.setFeeDiscount(line[14]);
                allocation.setAllocationStatus(line[15]);
                allocation.setAllocationDate(LocalDate.parse(line[16], dateFormatter));
                allocations.add(allocation);
            }
        }
        allocationRepository.saveAll(allocations);
        System.out.println("Loaded " + allocations.size() + " allocations");
    }

    private void loadValuations() throws Exception {
        List<Valuation> valuations = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/valuations.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Valuation valuation = new Valuation();
                valuation.setValuationId(line[0]);
                valuation.setDealId(line[1]);
                valuation.setValuationDate(LocalDate.parse(line[2], dateFormatter));
                valuation.setSharePrice(new BigDecimal(line[3]));
                valuation.setCompanyValuationM(new BigDecimal(line[4]));
                valuation.setMarkSource(line[5]);
                valuation.setMultipleVsEntry(new BigDecimal(line[6]));
                valuations.add(valuation);
            }
        }
        valuationRepository.saveAll(valuations);
        System.out.println("Loaded " + valuations.size() + " valuations");
    }

    private void loadCapitalCalls() throws Exception {
        List<CapitalCall> capitalCalls = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/capital_calls.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                CapitalCall call = new CapitalCall();
                call.setCallId(line[0]);
                call.setAllocationId(line[1]);
                call.setInvestorId(line[2]);
                call.setDealId(line[3]);
                call.setCallNumber(Integer.parseInt(line[4]));
                call.setCallDate(LocalDate.parse(line[5], dateFormatter));
                call.setAmount(new BigDecimal(line[6]));
                call.setCurrency(line[7]);
                call.setDueDate(LocalDate.parse(line[8], dateFormatter));
                call.setStatus(line[9]);
                capitalCalls.add(call);
            }
        }
        capitalCallRepository.saveAll(capitalCalls);
        System.out.println("Loaded " + capitalCalls.size() + " capital calls");
    }

    private void loadFees() throws Exception {
        List<Fee> fees = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/fees.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Fee fee = new Fee();
                fee.setFeeId(line[0]);
                fee.setAllocationId(line[1]);
                fee.setInvestorId(line[2]);
                fee.setDealId(line[3]);
                fee.setFeeType(line[4]);
                fee.setPeriod(line[5].isEmpty() ? null : Integer.parseInt(line[5]));
                fee.setFeeRatePct(line[6].isEmpty() ? null : new BigDecimal(line[6]));
                fee.setBasis(line[7]);
                fee.setAmount(new BigDecimal(line[8]));
                fee.setCurrency(line[9]);
                fee.setDueDate(LocalDate.parse(line[10], dateFormatter));
                fee.setStatus(line[11]);
                fees.add(fee);
            }
        }
        feeRepository.saveAll(fees);
        System.out.println("Loaded " + fees.size() + " fees");
    }

    private void loadDistributions() throws Exception {
        List<Distribution> distributions = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/distributions.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Distribution distribution = new Distribution();
                distribution.setDistributionId(line[0]);
                distribution.setDealId(line[1]);
                distribution.setAllocationId(line[2]);
                distribution.setInvestorId(line[3]);
                distribution.setDistributionDate(LocalDate.parse(line[4], dateFormatter));
                distribution.setDistributionType(line[5]);
                distribution.setGrossAmount(new BigDecimal(line[6]));
                distribution.setPerformanceFeePct(new BigDecimal(line[7]));
                distribution.setPerformanceFeeAmount(new BigDecimal(line[8]));
                distribution.setNetAmount(new BigDecimal(line[9]));
                distribution.setCurrency(line[10]);
                distribution.setFractionOfUnits(new BigDecimal(line[11]));
                distributions.add(distribution);
            }
        }
        distributionRepository.saveAll(distributions);
        System.out.println("Loaded " + distributions.size() + " distributions");
    }

    private void loadStatementLines() throws Exception {
        List<StatementLine> statementLines = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/statement_lines.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                StatementLine statementLine = new StatementLine();
                statementLine.setLineId(line[0]);
                statementLine.setInvestorId(line[1]);
                statementLine.setDate(LocalDate.parse(line[2], dateFormatter));
                statementLine.setType(line[3]);
                statementLine.setDealId(line[4]);
                statementLine.setAmount(new BigDecimal(line[5]));
                statementLine.setCurrency(line[6]);
                statementLine.setReferenceId(line[7]);
                statementLines.add(statementLine);
            }
        }
        statementLineRepository.saveAll(statementLines);
        System.out.println("Loaded " + statementLines.size() + " statement lines");
    }

    private void loadFxRates() throws Exception {
        List<FxRate> fxRates = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/fx_rates.csv").getInputStream()))
                .withSkipLines(1)
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                FxRate fxRate = new FxRate();
                fxRate.setCurrency(line[0]);
                fxRate.setToUsd(new BigDecimal(line[1]));
                fxRate.setAsOf(LocalDate.parse(line[2], dateFormatter));
                fxRates.add(fxRate);
            }
        }
        fxRateRepository.saveAll(fxRates);
        System.out.println("Loaded " + fxRates.size() + " FX rates");
        System.out.println("FX Rates: " + fxRates);
    }
}