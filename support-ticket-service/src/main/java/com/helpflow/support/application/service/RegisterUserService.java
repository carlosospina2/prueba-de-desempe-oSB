package com.helpflow.support.application.service;

import com.helpflow.support.application.port.in.RegisterUserUseCase;
import com.helpflow.support.application.port.out.UserRepositoryPort;
import com.helpflow.support.domain.exception.BusinessRuleException;
import com.helpflow.support.domain.model.User;
import com.helpflow.support.domain.model.enums.Role;
import com.helpflow.support.domain.model.enums.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepo;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(UserRepositoryPort userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(String document, String name, String email,
                         String password, String sede, String area, Role role) {
        if (userRepo.existsByEmail(email)) {
            throw new BusinessRuleException("Email ya registrado: " + email);
        }
        if (userRepo.existsByDocument(document)) {
            throw new BusinessRuleException("Documento ya registrado: " + document);
        }
        User user = new User();
        user.setDocument(document);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setSede(sede);
        user.setArea(area);
        user.setStatus(UserStatus.ACTIVO);
        user.setRole(role == null ? Role.ROLE_USUARIO : role);
        return userRepo.save(user);
    }
}
