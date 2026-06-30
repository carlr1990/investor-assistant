package com.equitie.assistant.repository;

import com.equitie.assistant.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, String> {
    List<Fee> findByInvestorId(String investorId);

    @Query("SELECT f FROM Fee f WHERE f.investorId = :investorId AND f.status IN ('Upcoming', 'Overdue')")
    List<Fee> findUpcomingFeesByInvestorId(@Param("investorId") String investorId);
}
