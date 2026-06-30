package com.equitie.assistant.repository;

import com.equitie.assistant.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, String> {
    List<Deal> findByCompanyId(String companyId);
}
