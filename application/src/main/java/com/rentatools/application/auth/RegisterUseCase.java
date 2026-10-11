package com.rentatools.application.auth;

import com.rentatools.application.auth.dto.AuthResponse;
import com.rentatools.application.auth.dto.RegisterRequest;
import com.rentatools.application.auth.port.PasswordEncoderPort;
import com.rentatools.application.auth.port.TokenGeneratorPort;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.user.ClientProfile;
import com.rentatools.domain.user.ProviderProfile;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;

public class RegisterUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public RegisterUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public AuthResponse execute(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException("Datos de registro requeridos");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es obligatorio");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException("El correo electrónico es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("El nombre de usuario ya está registrado");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("El correo electrónico ya está registrado");
        }

        Role role = Role.ROLE_CLIENTE;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            role = Role.fromString(request.getRole());
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : username);
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(true);

        if (role == Role.ROLE_CLIENTE) {
            ClientProfile cp = new ClientProfile();
            cp.setCompanyName(request.getCompanyName());
            cp.setTaxId(request.getTaxId());
            cp.setAddress(request.getAddress());
            cp.setCity(request.getCity());
            user.setClientProfile(cp);
        } else if (role == Role.ROLE_PROVEEDOR) {
            ProviderProfile pp = new ProviderProfile();
            pp.setBusinessName(request.getBusinessName() != null ? request.getBusinessName() : user.getFullName());
            pp.setTaxId(request.getTaxId());
            pp.setCommercialRegistry(request.getCommercialRegistry());
            pp.setAddress(request.getAddress());
            pp.setCity(request.getCity());
            pp.setRating(5.0);
            pp.setVerified(true);
            user.setProviderProfile(pp);
        }

        User savedUser = userRepository.save(user);
        String token = tokenGenerator.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                "Usuario registrado exitosamente"
        );
    }
}
