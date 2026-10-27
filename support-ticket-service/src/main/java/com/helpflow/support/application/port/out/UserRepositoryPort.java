package com.helpflow.support.application.port.out;

import com.helpflow.support.domain.model.User;
import java.util.Optional;
import java.util.List;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByDocument(String document);
    boolean existsByEmail(String email);
    boolean existsByDocument(String document);
    List<User> findAll();
}
