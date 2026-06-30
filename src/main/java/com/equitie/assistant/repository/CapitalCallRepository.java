package com.equitie.assistant.repository;

import com.equitie.assistant.model.CapitalCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CapitalCallRepository extends JpaRepository<CapitalCall, String> {
    List<CapitalCall> findByInvestorId(String investorId);

    @Query("SELECT c FROM CapitalCall c WHERE c.investorId = :investorId AND c.status IN ('Upcoming', 'Overdue')")
    List<CapitalCall> findUpcomingCallsByInvestorId(@Param("investorId") String investorId);
}
