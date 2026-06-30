package com.equitie.assistant.repository;

import com.equitie.assistant.model.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, String> {

    Optional<Investor> findByInvestorId(String investorId);

    Optional<Investor> findByEmail(String email);

    List<Investor> findByKycStatus(String kycStatus);

    List<Investor> findByTechSavviness(String techSavviness);

    @Query("SELECT i FROM Investor i WHERE i.kycStatus = 'Verified' AND i.onboardedDate <= CURRENT_DATE")
    List<Investor> findActiveInvestors();

    @Query("SELECT COUNT(i) FROM Investor i WHERE i.techSavviness = :savviness")
    long countByTechSavviness(@Param("savviness") String savviness);

    @Query("SELECT i FROM Investor i WHERE i.age IS NOT NULL AND i.age > :ageThreshold")
    List<Investor> findInvestorsOlderThan(@Param("ageThreshold") int ageThreshold);
}