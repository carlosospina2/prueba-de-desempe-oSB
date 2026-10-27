package com.helpflow.support.infrastructure.adapter.out.persistence;

import com.helpflow.support.application.port.out.UserRepositoryPort;
import com.helpflow.support.domain.model.User;
import com.helpflow.support.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final JpaUserRepository repo;
    private final UserMapper mapper;

    public UserPersistenceAdapter(JpaUserRepository repo, UserMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override public User save(User user) { return mapper.toDomain(repo.save(mapper.toEntity(user))); }
    @Override public Optional<User> findById(Long id) { return repo.findById(id).map(mapper::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return repo.findByEmail(email).map(mapper::toDomain); }
    @Override public Optional<User> findByDocument(String document) { return repo.findByDocument(document).map(mapper::toDomain); }
    @Override public boolean existsByEmail(String email) { return repo.existsByEmail(email); }
    @Override public boolean existsByDocument(String document) { return repo.existsByDocument(document); }
    @Override public List<User> findAll() { return repo.findAll().stream().map(mapper::toDomain).collect(Collectors.toList()); }
}
