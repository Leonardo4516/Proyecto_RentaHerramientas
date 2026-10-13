package com.rentatools.infrastructure.persistence.user;

import com.rentatools.domain.user.ClientProfile;
import com.rentatools.domain.user.ProviderProfile;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserAdapter implements UserRepository {
    private final SpringDataUserRepository userRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataProviderRepository providerRepository;

    public UserAdapter(SpringDataUserRepository userRepository,
                       SpringDataClientRepository clientRepository,
                       SpringDataProviderRepository providerRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity savedUser = userRepository.save(entity);

        if (user.getRole() == Role.ROLE_CLIENTE && user.getClientProfile() != null) {
            ClientProfile cp = user.getClientProfile();
            ClientJpaEntity clientEntity = clientRepository.findByUserId(savedUser.getId())
                    .orElse(new ClientJpaEntity());
            clientEntity.setUserId(savedUser.getId());
            clientEntity.setDocumentType(cp.getTaxId() != null && cp.getTaxId().contains("-") ? "NIT" : "CC");
            clientEntity.setDocumentNumber(cp.getTaxId() != null ? cp.getTaxId() : "DOC-" + savedUser.getId());
            clientEntity.setAddress(cp.getAddress());
            clientEntity.setCity(cp.getCity());
            if (cp.getPreferredPaymentMethod() != null) {
                clientEntity.setPreferredPaymentMethod(cp.getPreferredPaymentMethod());
            }
            clientRepository.save(clientEntity);
        } else if (user.getRole() == Role.ROLE_PROVEEDOR && user.getProviderProfile() != null) {
            ProviderProfile pp = user.getProviderProfile();
            ProviderJpaEntity providerEntity = providerRepository.findByUserId(savedUser.getId())
                    .orElse(new ProviderJpaEntity());
            providerEntity.setUserId(savedUser.getId());
            providerEntity.setCompanyName(pp.getBusinessName() != null ? pp.getBusinessName() : savedUser.getFullName());
            providerEntity.setTaxId(pp.getTaxId() != null ? pp.getTaxId() : "NIT-" + savedUser.getId());
            providerEntity.setCommercialRegistry(pp.getCommercialRegistry());
            providerEntity.setCommercialAddress(pp.getAddress());
            providerEntity.setCity(pp.getCity());
            providerEntity.setRating(pp.getRating() != null ? java.math.BigDecimal.valueOf(pp.getRating()) : new java.math.BigDecimal("5.00"));
            providerEntity.setVerified(pp.getVerified() != null ? pp.getVerified() : false);
            providerRepository.save(providerEntity);
        }

        return toDomain(savedUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return userRepository.count();
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
            clientRepository.findByUserId(e.getId()).ifPresent(c -> {
                ClientProfile cp = new ClientProfile();
                cp.setTaxId(c.getDocumentNumber());
                cp.setAddress(c.getAddress());
                cp.setCity(c.getCity());
                cp.setPreferredPaymentMethod(c.getPreferredPaymentMethod());
                user.setClientProfile(cp);
            });
        } else if (e.getRole() == Role.ROLE_PROVEEDOR) {
            providerRepository.findByUserId(e.getId()).ifPresent(p -> {
                ProviderProfile pp = new ProviderProfile();
                pp.setBusinessName(p.getCompanyName());
                pp.setTaxId(p.getTaxId());
                pp.setCommercialRegistry(p.getCommercialRegistry());
                pp.setAddress(p.getCommercialAddress());
                pp.setCity(p.getCity());
                pp.setRating(p.getRating() != null ? p.getRating().doubleValue() : 5.0);
                pp.setVerified(p.getVerified());
                user.setProviderProfile(pp);
            });
        }

        return user;
    }
}
