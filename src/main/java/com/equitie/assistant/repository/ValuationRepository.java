package com.equitie.assistant.repository;

import com.equitie.assistant.model.Valuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValuationRepository extends JpaRepository<Valuation, String> {
    @Query("SELECT v FROM Valuation v WHERE v.dealId = :dealId ORDER BY v.valuationDate DESC")
    List<Valuation> findLatestValuationByDealId(@Param("dealId") String dealId);

    @Query("SELECT v FROM Valuation v WHERE v.dealId = :dealId ORDER BY v.valuationDate ASC")
    List<Valuation> findValuationHistoryByDealId(@Param("dealId") String dealId);
}
