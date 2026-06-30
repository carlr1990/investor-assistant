package com.equitie.assistant.repository;

import com.equitie.assistant.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AllocationRepository extends JpaRepository<Allocation, String> {
    List<Allocation> findByInvestorId(String investorId);

    @Query("SELECT a FROM Allocation a WHERE a.investorId = :investorId AND a.allocationStatus = 'Active'")
    List<Allocation> findActiveAllocationsByInvestorId(@Param("investorId") String investorId);
}