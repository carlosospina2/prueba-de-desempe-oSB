package com.helpflow.support.infrastructure.adapter.in.rest;

import com.helpflow.support.application.port.in.RegisterUserUseCase;
import com.helpflow.support.domain.model.User;
import com.helpflow.support.domain.model.enums.Role;
import com.helpflow.support.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(RegisterUserUseCase registerUser,
                          AuthenticationManager authManager,
                          JwtService jwtService) {
        this.registerUser = registerUser;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public record RegisterRequest(
        @NotBlank String document,
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String sede,
        @NotBlank String area,
        Role role
    ) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, String tokenType, String email, String role) {}

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = registerUser.register(req.document(), req.name(), req.email(),
            req.password(), req.sede(), req.area(), req.role());
        String token = jwtService.generate(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var userDetails = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String token = jwtService.generate(userDetails.getUsername(), role);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", userDetails.getUsername(), role));
    }
}
