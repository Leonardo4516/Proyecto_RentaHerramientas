package com.rentatools.domain.user.port;

import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findAll();
    List<User> findByRole(Role role);
    long count();
}
