package com.equitie.assistant.repository;

import com.equitie.assistant.model.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, String> {
    Optional<FxRate> findByCurrency(String currency);

    // Optional: Get all rates as a map
    default java.util.Map<String, java.math.BigDecimal> getAllRatesMap() {
        return findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        FxRate::getCurrency,
                        FxRate::getToUsd
                ));
    }
}