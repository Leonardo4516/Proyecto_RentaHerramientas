package com.rentatools.application.auth;

import com.rentatools.application.auth.dto.AuthResponse;
import com.rentatools.application.auth.dto.LoginRequest;
import com.rentatools.application.auth.port.PasswordEncoderPort;
import com.rentatools.application.auth.port.TokenGeneratorPort;
import com.rentatools.domain.common.exception.UnauthorizedException;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGenerator;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Test
    @DisplayName("Debe autenticar correctamente con credenciales válidas")
    void shouldLoginSuccessfully() {
        User user = new User(1L, "cliente1", "cliente1@mail.com", "encodedPass", "Juan Perez", "123", Role.ROLE_CLIENTE);
        user.setActive(true);

        when(userRepository.findByUsername("cliente1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encodedPass")).thenReturn(true);
        when(tokenGenerator.generateToken(user)).thenReturn("mock.jwt.token");

        LoginRequest request = new LoginRequest("cliente1", "secret123");
        AuthResponse response = loginUseCase.execute(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("cliente1", response.getUsername());
        assertEquals("ROLE_CLIENTE", response.getRole());
    }

    @Test
    @DisplayName("Debe fallar al proporcionar contraseña incorrecta")
    void shouldThrowWhenPasswordIncorrect() {
        User user = new User(1L, "cliente1", "cliente1@mail.com", "encodedPass", "Juan Perez", "123", Role.ROLE_CLIENTE);
        user.setActive(true);

        when(userRepository.findByUsername("cliente1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        LoginRequest request = new LoginRequest("cliente1", "wrongPass");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> loginUseCase.execute(request));
        assertTrue(ex.getMessage().contains("Usuario o contraseña incorrectos"));
    }
}
