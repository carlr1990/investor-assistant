package com.equitie.assistant.repository;

import com.equitie.assistant.model.PortfolioCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioCompanyRepository extends JpaRepository<PortfolioCompany, String> {

    Optional<PortfolioCompany> findByCompanyId(String companyId);

    Optional<PortfolioCompany> findByCompanyName(String companyName);

    List<PortfolioCompany> findBySector(String sector);

    List<PortfolioCompany> findByStatus(String status);

    List<PortfolioCompany> findByHqCountry(String hqCountry);

    @Query("SELECT c FROM PortfolioCompany c WHERE c.status = 'Active'")
    List<PortfolioCompany> findActiveCompanies();

    @Query("SELECT c FROM PortfolioCompany c WHERE c.sector = :sector AND c.status = 'Active'")
    List<PortfolioCompany> findActiveCompaniesBySector(@Param("sector") String sector);

    @Query("SELECT DISTINCT c.sector FROM PortfolioCompany c")
    List<String> findAllSectors();

    @Query("SELECT c.sector, COUNT(c) FROM PortfolioCompany c GROUP BY c.sector")
    List<Object[]> countCompaniesBySector();
}