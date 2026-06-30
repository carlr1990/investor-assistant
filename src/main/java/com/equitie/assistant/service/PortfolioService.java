package com.equitie.assistant.service;

import com.equitie.assistant.model.*;
import com.equitie.assistant.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortfolioService {

    @Autowired
    private AllocationRepository allocationRepository;
    @Autowired
    private DealRepository dealRepository;
    @Autowired
    private PortfolioCompanyRepository companyRepository;
    @Autowired
    private ValuationRepository valuationRepository;
    @Autowired
    private DistributionRepository distributionRepository;
    @Autowired
    private CapitalCallRepository capitalCallRepository;
    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private FxRateRepository fxRateRepository;
    @Autowired
    private InvestorRepository investorRepository;

    private static final LocalDate REPORT_DATE = LocalDate.of(2026, 6, 25);
    private Map<String, BigDecimal> fxRateCache;

    @PostConstruct
    public void initFxCache() {
        refreshFxCache();
    }

    public void refreshFxCache() {
        fxRateCache = new HashMap<>();
        List<FxRate> rates = fxRateRepository.findAll();
        for (FxRate rate : rates) {
            fxRateCache.put(rate.getCurrency(), rate.getToUsd());
        }
        log.info("FX Cache loaded with {} currencies", fxRateCache.size());
    }

    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(String investorId) {
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found: " + investorId));

        List<Allocation> allocations = allocationRepository.findActiveAllocationsByInvestorId(investorId);

        BigDecimal totalCommitted = BigDecimal.ZERO;
        BigDecimal totalContributed = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalDistributions = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        List<PositionDetail> positions = new ArrayList<>();

        for (Allocation allocation : allocations) {
            Deal deal = dealRepository.findById(allocation.getDealId()).orElse(null);
            if (deal == null) continue;

            // Get latest valuation
            List<Valuation> valuations = valuationRepository.findLatestValuationByDealId(deal.getDealId());
            Valuation latestValuation = valuations.isEmpty() ? null : valuations.get(0);

            // Get distributions for this deal
            List<Distribution> distributions = distributionRepository.findByInvestorId(investorId)
                    .stream()
                    .filter(d -> d.getDealId().equals(deal.getDealId()))
                    .toList();

            BigDecimal totalDistForDeal = distributions.stream()
                    .map(Distribution::getNetAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate current value
            BigDecimal currentValue = BigDecimal.ZERO;
            BigDecimal realizedFraction = BigDecimal.ZERO;

            if (!"Exited".equals(deal.getStatus()) && !"Written Off".equals(deal.getStatus())) {
                realizedFraction = distributions.stream()
                        .map(Distribution::getFractionOfUnits)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal remainingUnits = allocation.getUnits()
                        .multiply(BigDecimal.ONE.subtract(realizedFraction));

                if (latestValuation != null) {
                    currentValue = remainingUnits.multiply(latestValuation.getSharePrice());
                }
            }

            // Convert to reporting currency
            String reportingCurrency = investor.getReportingCurrency();
            BigDecimal fxRate = getFxRate(allocation.getDealCurrency(), reportingCurrency);

            BigDecimal currentValueConverted = currentValue.multiply(fxRate);
            BigDecimal distConverted = totalDistForDeal.multiply(fxRate);
            BigDecimal commitmentConverted = allocation.getCommitmentAmount().multiply(fxRate);
            BigDecimal contributedConverted = allocation.getContributedAmount().multiply(fxRate);

            // Calculate cost basis
            BigDecimal costBasis = allocation.getUnits()
                    .multiply(allocation.getEffectiveSharePrice())
                    .multiply(fxRate);

            // Create position detail
            PositionDetail position = new PositionDetail();
            position.setCompanyId(deal.getCompanyId());
            position.setCompanyName(deal.getCompanyName());
            position.setRound(deal.getRound());
            position.setAllocationId(allocation.getAllocationId());
            position.setDealCurrency(allocation.getDealCurrency());
            position.setCommitment(commitmentConverted);
            position.setContributed(contributedConverted);
            position.setCurrentValue(currentValueConverted);
            position.setDistributions(distConverted);
            position.setCostBasis(costBasis);
            position.setUnits(allocation.getUnits());
            position.setEffectiveSharePrice(allocation.getEffectiveSharePrice());
            position.setEntrySharePrice(deal.getEntrySharePrice());
            position.setPriceDiscountPct(allocation.getPriceDiscountPct());
            position.setLatestSharePrice(latestValuation != null ? latestValuation.getSharePrice() : null);
            position.setRealizedFraction(realizedFraction);
            position.setStatus(deal.getStatus());

            // Calculate MOIC
            BigDecimal moic = BigDecimal.ZERO;
            if (position.getContributed().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalReturn = currentValueConverted.add(distConverted);
                moic = totalReturn.divide(position.getContributed(), 2, RoundingMode.HALF_UP);
            }
            position.setMoic(moic);

            // Calculate DPI (Distributions to Paid-In)
            BigDecimal dpi = BigDecimal.ZERO;
            if (position.getContributed().compareTo(BigDecimal.ZERO) > 0) {
                dpi = distConverted.divide(position.getContributed(), 2, RoundingMode.HALF_UP);
            }
            position.setDpi(dpi);

            // Calculate RVPI (Residual Value to Paid-In)
            BigDecimal rvpi = BigDecimal.ZERO;
            if (position.getContributed().compareTo(BigDecimal.ZERO) > 0) {
                rvpi = currentValueConverted.divide(position.getContributed(), 2, RoundingMode.HALF_UP);
            }
            position.setRvpi(rvpi);

            positions.add(position);

            // Aggregate totals
            totalCommitted = totalCommitted.add(commitmentConverted);
            totalContributed = totalContributed.add(contributedConverted);
            totalCurrentValue = totalCurrentValue.add(currentValueConverted);
            totalDistributions = totalDistributions.add(distConverted);
            totalCostBasis = totalCostBasis.add(costBasis);
        }

        // Get portfolio company sectors for personalization
        Map<String, Long> sectorCounts = getSectorCounts(investorId);
        Map<String, BigDecimal> sectorValues = getSectorValues(investorId);

        // Calculate overall MOIC
        BigDecimal overallMoic = BigDecimal.ZERO;
        if (totalContributed.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalReturn = totalCurrentValue.add(totalDistributions);
            overallMoic = totalReturn.divide(totalContributed, 2, RoundingMode.HALF_UP);
        }

        // Calculate overall DPI
        BigDecimal overallDpi = BigDecimal.ZERO;
        if (totalContributed.compareTo(BigDecimal.ZERO) > 0) {
            overallDpi = totalDistributions.divide(totalContributed, 2, RoundingMode.HALF_UP);
        }

        // Calculate overall RVPI
        BigDecimal overallRvpi = BigDecimal.ZERO;
        if (totalContributed.compareTo(BigDecimal.ZERO) > 0) {
            overallRvpi = totalCurrentValue.divide(totalContributed, 2, RoundingMode.HALF_UP);
        }

        PortfolioSummary summary = new PortfolioSummary();
        summary.setInvestorId(investorId);
        summary.setReportingCurrency(investor.getReportingCurrency());
        summary.setTotalCommitted(totalCommitted);
        summary.setTotalContributed(totalContributed);
        summary.setTotalCurrentValue(totalCurrentValue);
        summary.setTotalDistributions(totalDistributions);
        summary.setTotalCostBasis(totalCostBasis);
        summary.setOverallMoic(overallMoic);
        summary.setOverallDpi(overallDpi);
        summary.setOverallRvpi(overallRvpi);
        summary.setPositions(positions);
        summary.setSectorCounts(sectorCounts);
        summary.setSectorValues(sectorValues);
        summary.setDealCount(positions.size());
        summary.setReportDate(REPORT_DATE);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<Obligation> getObligations(String investorId) {
        List<Obligation> obligations = new ArrayList<>();
        Investor investor = investorRepository.findById(investorId).orElse(null);
        if (investor == null) return obligations;

        // Capital calls
        List<CapitalCall> calls = capitalCallRepository.findUpcomingCallsByInvestorId(investorId);
        for (CapitalCall call : calls) {
            Allocation allocation = allocationRepository.findById(call.getAllocationId()).orElse(null);
            Deal deal = allocation != null ? dealRepository.findById(allocation.getDealId()).orElse(null) : null;

            BigDecimal fxRate = getFxRate(call.getCurrency(), investor.getReportingCurrency());

            Obligation obligation = new Obligation();
            obligation.setType("Capital Call");
            obligation.setDueDate(call.getDueDate());
            obligation.setOriginalAmount(call.getAmount());
            obligation.setOriginalCurrency(call.getCurrency());
            obligation.setAmount(call.getAmount().multiply(fxRate));
            obligation.setCurrency(investor.getReportingCurrency());
            obligation.setDealName(deal != null ? deal.getCompanyName() + " (" + deal.getRound() + ")" : "Unknown");
            obligation.setStatus(call.getStatus());
            obligation.setAllocationId(call.getAllocationId());
            obligations.add(obligation);
        }

        // Fees
        List<Fee> fees = feeRepository.findUpcomingFeesByInvestorId(investorId);
        for (Fee fee : fees) {
            Allocation allocation = allocationRepository.findById(fee.getAllocationId()).orElse(null);
            Deal deal = allocation != null ? dealRepository.findById(allocation.getDealId()).orElse(null) : null;

            String feeCurrency = fee.getCurrency() != null ? fee.getCurrency() : "USD";
            BigDecimal fxRate = getFxRate(feeCurrency, investor.getReportingCurrency());

            Obligation obligation = new Obligation();
            obligation.setType(fee.getFeeType());
            obligation.setDueDate(fee.getDueDate());
            obligation.setOriginalAmount(fee.getAmount());
            obligation.setOriginalCurrency(feeCurrency);
            obligation.setAmount(fee.getAmount().multiply(fxRate));
            obligation.setCurrency(investor.getReportingCurrency());
            obligation.setDealName(deal != null ? deal.getCompanyName() + " (" + deal.getRound() + ")" : "Unknown");
            obligation.setStatus(fee.getStatus());
            obligation.setAllocationId(fee.getAllocationId());
            obligations.add(obligation);
        }

        // Sort by due date
        obligations.sort(Comparator.comparing(Obligation::getDueDate));

        return obligations;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSectorCounts(String investorId) {
        List<Allocation> allocations = allocationRepository.findByInvestorId(investorId);
        Map<String, Long> sectorCounts = new HashMap<>();

        for (Allocation allocation : allocations) {
            Deal deal = dealRepository.findById(allocation.getDealId()).orElse(null);
            if (deal != null) {
                PortfolioCompany company = companyRepository.findById(deal.getCompanyId()).orElse(null);
                if (company != null) {
                    sectorCounts.merge(company.getSector(), 1L, Long::sum);
                }
            }
        }

        return sectorCounts;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSectorValues(String investorId) {
        Map<String, BigDecimal> sectorValues = new HashMap<>();
        Investor investor = investorRepository.findById(investorId).orElse(null);
        if (investor == null) return sectorValues;

        List<Allocation> allocations = allocationRepository.findByInvestorId(investorId);

        for (Allocation allocation : allocations) {
            Deal deal = dealRepository.findById(allocation.getDealId()).orElse(null);
            if (deal != null) {
                PortfolioCompany company = companyRepository.findById(deal.getCompanyId()).orElse(null);
                if (company != null) {
                    // Get current value for this allocation
                    List<Valuation> valuations = valuationRepository.findLatestValuationByDealId(deal.getDealId());
                    Valuation latestValuation = valuations.isEmpty() ? null : valuations.get(0);

                    BigDecimal currentValue = BigDecimal.ZERO;
                    if (latestValuation != null) {
                        List<Distribution> distributions = distributionRepository.findByInvestorId(investorId)
                                .stream()
                                .filter(d -> d.getDealId().equals(deal.getDealId()))
                                .toList();

                        BigDecimal realizedFraction = distributions.stream()
                                .map(Distribution::getFractionOfUnits)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal remainingUnits = allocation.getUnits()
                                .multiply(BigDecimal.ONE.subtract(realizedFraction));
                        currentValue = remainingUnits.multiply(latestValuation.getSharePrice());
                    }

                    // Convert to reporting currency
                    BigDecimal fxRate = getFxRate(allocation.getDealCurrency(), investor.getReportingCurrency());
                    currentValue = currentValue.multiply(fxRate);

                    sectorValues.merge(company.getSector(), currentValue, BigDecimal::add);
                }
            }
        }

        return sectorValues;
    }

    @Transactional(readOnly = true)
    public Investor getInvestor(String investorId) {
        return investorRepository.findById(investorId).orElse(null);
    }

    @Transactional(readOnly = true)
    public BigDecimal getFxRate(String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) return BigDecimal.ONE;
        if (fromCurrency.equals(toCurrency)) return BigDecimal.ONE;

        // Get rates from cache
        BigDecimal fromToUsd = fxRateCache.get(fromCurrency);
        BigDecimal toToUsd = fxRateCache.get(toCurrency);

        if (fromToUsd == null || toToUsd == null) {
            log.warn("Missing FX rate for {} or {}", fromCurrency, toCurrency);
            return BigDecimal.ONE;
        }

        // from to to = (from to usd) / (to to usd)
        return fromToUsd.divide(toToUsd, 6, RoundingMode.HALF_UP);
    }

    // Inner classes for response
    @lombok.Data
    public static class PortfolioSummary {
        private String investorId;
        private String reportingCurrency;
        private BigDecimal totalCommitted;
        private BigDecimal totalContributed;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalDistributions;
        private BigDecimal totalCostBasis;
        private BigDecimal overallMoic;
        private BigDecimal overallDpi;
        private BigDecimal overallRvpi;
        private List<PositionDetail> positions;
        private Map<String, Long> sectorCounts;
        private Map<String, BigDecimal> sectorValues;
        private int dealCount;
        private LocalDate reportDate;
    }

    @lombok.Data
    public static class PositionDetail {
        private String companyId;
        private String companyName;
        private String round;
        private String allocationId;
        private String dealCurrency;
        private BigDecimal commitment;
        private BigDecimal contributed;
        private BigDecimal currentValue;
        private BigDecimal distributions;
        private BigDecimal costBasis;
        private BigDecimal moic;
        private BigDecimal dpi;
        private BigDecimal rvpi;
        private BigDecimal units;
        private BigDecimal effectiveSharePrice;
        private BigDecimal entrySharePrice;
        private BigDecimal priceDiscountPct;
        private BigDecimal latestSharePrice;
        private BigDecimal realizedFraction;
        private String status;

        // Helper method to get price change
        public BigDecimal getPriceChangePct() {
            if (entrySharePrice == null || latestSharePrice == null) return BigDecimal.ZERO;
            return latestSharePrice.subtract(entrySharePrice)
                    .divide(entrySharePrice, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
    }

    @lombok.Data
    public static class Obligation {
        private String type;
        private LocalDate dueDate;
        private BigDecimal originalAmount;
        private String originalCurrency;
        private BigDecimal amount;
        private String currency;
        private String dealName;
        private String status;
        private String allocationId;

        public boolean isOverdue() {
            return "Overdue".equals(status);
        }

        public boolean isUpcoming() {
            return "Upcoming".equals(status);
        }
    }
}