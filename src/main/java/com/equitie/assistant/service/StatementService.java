package com.equitie.assistant.service;

import com.equitie.assistant.model.AccountStatement;
import com.equitie.assistant.model.Investor;
import com.equitie.assistant.model.StatementLine;
import com.equitie.assistant.repository.FxRateRepository;
import com.equitie.assistant.repository.InvestorRepository;
import com.equitie.assistant.repository.StatementLineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatementService {

    @Autowired
    private StatementLineRepository statementLineRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private FxRateRepository fxRateRepository;

    public AccountStatement generateAccountStatement(String investorId, int year) {
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        List<StatementLine> lines = statementLineRepository.findStatementByYear(investorId, year);

        // Calculate totals
        BigDecimal totalOutgoing = BigDecimal.ZERO;
        BigDecimal totalIncoming = BigDecimal.ZERO;
        Map<String, BigDecimal> totalsByType = new HashMap<>();
        Map<String, BigDecimal> totalsByCurrency = new HashMap<>();

        for (StatementLine line : lines) {
            if (line.isOutgoing()) {
                totalOutgoing = totalOutgoing.add(line.getAmount().abs());
            } else {
                totalIncoming = totalIncoming.add(line.getAmount());
            }

            totalsByType.merge(line.getType(), line.getAmount(), BigDecimal::add);
            totalsByCurrency.merge(line.getCurrency(), line.getAmount(), BigDecimal::add);
        }

        AccountStatement statement = new AccountStatement();
        statement.setInvestorId(investorId);
        statement.setInvestorName(investor.getInvestorName());
        statement.setReportingCurrency(investor.getReportingCurrency());
        statement.setYear(year);
        statement.setLines(lines);
        statement.setTotalOutgoing(totalOutgoing);
        statement.setTotalIncoming(totalIncoming);
        statement.setNetPosition(totalIncoming.subtract(totalOutgoing));
        statement.setTotalsByType(totalsByType);
        statement.setTotalsByCurrency(totalsByCurrency);

        return statement;
    }
}