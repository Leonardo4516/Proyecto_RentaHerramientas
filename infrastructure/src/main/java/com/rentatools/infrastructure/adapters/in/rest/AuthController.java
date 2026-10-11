package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.auth.GetCurrentUserUseCase;
import com.rentatools.application.auth.LoginUseCase;
import com.rentatools.application.auth.RegisterUseCase;
import com.rentatools.application.auth.dto.AuthResponse;
import com.rentatools.application.auth.dto.LoginRequest;
import com.rentatools.application.auth.dto.RegisterRequest;
import com.rentatools.application.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación & Seguridad", description = "Endpoints de registro, inicio de sesión y consulta del perfil autenticado")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUseCase registerUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
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
}
