package com.helpflow.support.infrastructure.adapter.in.rest;

import com.helpflow.support.application.port.out.UserRepositoryPort;
import com.helpflow.support.domain.exception.UserNotFoundException;
import com.helpflow.support.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepositoryPort userRepo;

    public UserController(UserRepositoryPort userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (Admin only)")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userRepo.findById(id).map(ResponseEntity::ok)
            .orElseThrow(() -> new UserNotFoundException(String.valueOf(id)));
    }
}
