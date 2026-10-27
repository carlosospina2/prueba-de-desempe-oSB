package com.helpflow.support.infrastructure.adapter.out.persistence.repository;

import com.helpflow.support.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByDocument(String document);
    boolean existsByEmail(String email);
    boolean existsByDocument(String document);
}
