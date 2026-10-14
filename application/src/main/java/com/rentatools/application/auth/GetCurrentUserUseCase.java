package com.rentatools.application.auth;

import com.rentatools.application.auth.dto.UserResponse;
import com.rentatools.domain.common.exception.ResourceNotFoundException;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;

public class GetCurrentUserUseCase {
    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse execute(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new ResourceNotFoundException("Identificador de usuario inválido");
        }
        String id = usernameOrEmail.trim();
        User user = userRepository.findByUsername(id)
                .or(() -> userRepository.findByEmail(id))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con identificador: " + id));
        return UserResponse.fromDomain(user);
    }

    public UserResponse execute(java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return UserResponse.fromDomain(user);
    }
}
