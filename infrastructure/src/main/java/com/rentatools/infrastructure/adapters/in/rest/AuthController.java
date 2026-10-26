package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.auth.GetCurrentUserUseCase;
import com.rentatools.application.auth.LoginUseCase;
import com.rentatools.application.auth.RegisterUseCase;
import com.rentatools.application.auth.dto.AuthResponse;
import com.rentatools.application.auth.dto.LoginRequest;
import com.rentatools.application.auth.dto.RegisterRequest;
import com.rentatools.application.auth.dto.UserResponse;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.port.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación & Seguridad", description = "Endpoints de registro, inicio de sesión y gestión de usuarios")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UserRepository userRepository;

    public AuthController(LoginUseCase loginUseCase,
                          RegisterUseCase registerUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          UserRepository userRepository) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y retorna un token JWT Bearer")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Registra un usuario como Cliente o Proveedor y retorna su token JWT inicial")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = registerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener usuario autenticado", description = "Retorna la información del perfil del usuario extraída del token JWT")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = getCurrentUserUseCase.execute(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios", description = "Retorna la lista completa de usuarios registrados sin exponer contraseñas")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Actualizar rol de usuario", description = "Permite cambiar el rol de un usuario (ROLE_ADMIN, ROLE_CLIENTE, ROLE_PROVEEDOR)")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            com.rentatools.domain.user.User saved = userRepository.save(user);
            return ResponseEntity.ok(UserResponse.fromDomain(saved));
        }).orElse(ResponseEntity.notFound().build());
    }
}
