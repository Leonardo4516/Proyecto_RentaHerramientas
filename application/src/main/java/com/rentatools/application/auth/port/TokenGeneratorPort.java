package com.rentatools.application.auth.port;

import com.rentatools.domain.user.User;

public interface TokenGeneratorPort {
    String generateToken(User user);
    String extractUsername(String token);
    boolean validateToken(String token);
}
