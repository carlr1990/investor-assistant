package com.equitie.assistant.repository;

import com.equitie.assistant.model.StatementLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface StatementLineRepository extends JpaRepository<StatementLine, String> {

    List<StatementLine> findByInvestorId(String investorId);

    List<StatementLine> findByInvestorIdAndDealId(String investorId, String dealId);

    List<StatementLine> findByInvestorIdAndType(String investorId, String type);

    List<StatementLine> findByInvestorIdAndDateBetween(String investorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId ORDER BY s.date DESC")
    List<StatementLine> findLatestByInvestorId(@Param("investorId") String investorId);

    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId AND s.currency = :currency")
    List<StatementLine> findByInvestorIdAndCurrency(@Param("investorId") String investorId, @Param("currency") String currency);

    @Query("SELECT SUM(s.amount) FROM StatementLine s WHERE s.investorId = :investorId AND s.type = :type")
    BigDecimal sumByInvestorIdAndType(@Param("investorId") String investorId, @Param("type") String type);

    @Query("SELECT s.type, SUM(s.amount) FROM StatementLine s WHERE s.investorId = :investorId GROUP BY s.type")
    List<Object[]> sumByTypeForInvestor(@Param("investorId") String investorId);

    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId AND s.amount < 0")
    List<StatementLine> findOutgoingTransactions(@Param("investorId") String investorId);

    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId AND s.amount > 0")
    List<StatementLine> findIncomingTransactions(@Param("investorId") String investorId);

    @Query("SELECT SUM(s.amount) FROM StatementLine s WHERE s.investorId = :investorId")
    BigDecimal getTotalBalance(@Param("investorId") String investorId);

    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId AND s.date = :date")
    List<StatementLine> findByInvestorIdAndDate(@Param("investorId") String investorId, @Param("date") LocalDate date);

    @Query("SELECT s.referenceId, COUNT(s) FROM StatementLine s WHERE s.investorId = :investorId GROUP BY s.referenceId")
    List<Object[]> countByReferenceId(@Param("investorId") String investorId);

    // For generating account statements
    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId " +
            "AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC")
    List<StatementLine> findStatementBetweenDates(@Param("investorId") String investorId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // Get statement lines by year
    @Query("SELECT s FROM StatementLine s WHERE s.investorId = :investorId " +
            "AND YEAR(s.date) = :year ORDER BY s.date ASC")
    List<StatementLine> findStatementByYear(@Param("investorId") String investorId, @Param("year") int year);
}