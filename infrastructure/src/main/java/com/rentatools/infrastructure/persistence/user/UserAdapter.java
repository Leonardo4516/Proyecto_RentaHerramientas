package com.rentatools.infrastructure.persistence.user;

import com.rentatools.domain.user.ClientProfile;
import com.rentatools.domain.user.ProviderProfile;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserAdapter implements UserRepository {
    private final SpringDataUserRepository springRepository;

    public UserAdapter(SpringDataUserRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = springRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        return springRepository.findByRole(role).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepository.count();
    }

    private UserJpaEntity toEntity(User user) {
        if (user == null) return null;
        UserJpaEntity e = new UserJpaEntity();
        e.setId(user.getId());
        e.setUsername(user.getUsername());
        e.setEmail(user.getEmail());
        e.setPassword(user.getPassword());
        e.setFullName(user.getFullName());
        e.setPhone(user.getPhone());
        e.setRole(user.getRole());
        e.setActive(user.isActive());
        if (user.getCreatedAt() != null) e.setCreatedAt(user.getCreatedAt());

        if (user.getClientProfile() != null) {
            ClientProfile cp = user.getClientProfile();
            e.setCompanyName(cp.getCompanyName());
            e.setTaxId(cp.getTaxId());
            e.setAddress(cp.getAddress());
            e.setCity(cp.getCity());
            e.setPreferredPaymentMethod(cp.getPreferredPaymentMethod());
        }

        if (user.getProviderProfile() != null) {
            ProviderProfile pp = user.getProviderProfile();
            e.setBusinessName(pp.getBusinessName());
            if (e.getTaxId() == null) e.setTaxId(pp.getTaxId());
            if (e.getAddress() == null) e.setAddress(pp.getAddress());
            if (e.getCity() == null) e.setCity(pp.getCity());
            e.setCommercialRegistry(pp.getCommercialRegistry());
            e.setRating(pp.getRating());
            e.setVerified(pp.getVerified());
        }

        return e;
    }

    private User toDomain(UserJpaEntity e) {
        if (e == null) return null;
        User user = new User();
        user.setId(e.getId());
        user.setUsername(e.getUsername());
        user.setEmail(e.getEmail());
        user.setPassword(e.getPassword());
        user.setFullName(e.getFullName());
        user.setPhone(e.getPhone());
        user.setRole(e.getRole());
        user.setActive(e.isActive());
        user.setCreatedAt(e.getCreatedAt());

        if (e.getRole() == Role.ROLE_CLIENTE) {
            ClientProfile cp = new ClientProfile();
            cp.setCompanyName(e.getCompanyName());
            cp.setTaxId(e.getTaxId());
            cp.setAddress(e.getAddress());
            cp.setCity(e.getCity());
            cp.setPreferredPaymentMethod(e.getPreferredPaymentMethod());
            user.setClientProfile(cp);
        } else if (e.getRole() == Role.ROLE_PROVEEDOR) {
            ProviderProfile pp = new ProviderProfile();
            pp.setBusinessName(e.getBusinessName());
            pp.setTaxId(e.getTaxId());
            pp.setCommercialRegistry(e.getCommercialRegistry());
            pp.setAddress(e.getAddress());
            pp.setCity(e.getCity());
            pp.setRating(e.getRating());
            pp.setVerified(e.getVerified());
            user.setProviderProfile(pp);
        }

        return user;
    }
}
