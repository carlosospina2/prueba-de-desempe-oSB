package com.helpflow.support.infrastructure.adapter.out.persistence.repository;

import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.UserStatus;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JpaTechnicianRepository extends JpaRepository<TechnicianEntity, Long> {
    Optional<TechnicianEntity> findByDocument(String document);
    boolean existsByDocument(String document);

    @Query("SELECT t FROM TechnicianEntity t JOIN t.specialties s WHERE s = :category AND t.status = :status")
    List<TechnicianEntity> findBySpecialtiesContainingAndStatus(
        @Param("category") TicketCategory category,
        @Param("status") UserStatus status);
}
