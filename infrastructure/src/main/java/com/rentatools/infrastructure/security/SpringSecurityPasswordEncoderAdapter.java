package com.rentatools.infrastructure.security;

import com.rentatools.application.auth.port.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityPasswordEncoderAdapter implements PasswordEncoderPort {
    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
