package com.equitie.assistant.repository;

import com.equitie.assistant.model.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributionRepository extends JpaRepository<Distribution, String> {
    List<Distribution> findByInvestorId(String investorId);
}
