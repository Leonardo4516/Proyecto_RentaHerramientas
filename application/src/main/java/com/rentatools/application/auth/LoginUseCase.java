package com.rentatools.application.auth;

import com.rentatools.application.auth.dto.AuthResponse;
import com.rentatools.application.auth.dto.LoginRequest;
import com.rentatools.application.auth.port.PasswordEncoderPort;
import com.rentatools.application.auth.port.TokenGeneratorPort;
import com.rentatools.domain.common.exception.UnauthorizedException;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;

public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public LoginUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public AuthResponse execute(LoginRequest request) {
        if (request == null || request.getUsernameOrEmail() == null || request.getPassword() == null) {
            throw new UnauthorizedException("Credenciales no proporcionadas");
        }

        String identifier = request.getUsernameOrEmail().trim();
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UnauthorizedException("Usuario o contraseña incorrectos"));

        if (!user.isActive()) {
            throw new UnauthorizedException("La cuenta de usuario está desactivada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Usuario o contraseña incorrectos");
        }

        String token = tokenGenerator.generateToken(user);
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                "Autenticación exitosa"
        );
    }
}
