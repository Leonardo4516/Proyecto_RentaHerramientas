package com.rentatools.infrastructure.persistence.user;

import com.rentatools.domain.user.ClientProfile;
import com.rentatools.domain.user.ProviderProfile;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserAdapter implements UserRepository {

    private final SpringDataUserRepository userRepo;
    private final SpringDataRoleRepository roleRepo;
    private final SpringDataClientRepository clientRepo;
    private final SpringDataProviderRepository providerRepo;

    public UserAdapter(SpringDataUserRepository userRepo,
                       SpringDataRoleRepository roleRepo,
                       SpringDataClientRepository clientRepo,
                       SpringDataProviderRepository providerRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.clientRepo = clientRepo;
        this.providerRepo = providerRepo;
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user == null) return null;

        UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
        user.setId(userId);

        UserJpaEntity entity = userRepo.findById(userId).orElse(new UserJpaEntity());
        entity.setId(userId);
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPassword());
        entity.setFullName(user.getFullName() != null ? user.getFullName() : (user.getUsername() != null ? user.getUsername() : user.getEmail()));
        entity.setPhone(user.getPhone());
        entity.setActive(user.isActive());

        Role targetRole = user.getRole() != null ? user.getRole() : Role.ROLE_CLIENTE;
        Optional<RoleJpaEntity> roleOpt = roleRepo.findByName(targetRole.name());
        roleOpt.ifPresent(entity::addRole);

        UserJpaEntity savedUser = userRepo.save(entity);

        // Guardar o actualizar perfil de cliente
        if (targetRole == Role.ROLE_CLIENTE) {
            ClientProfile cp = user.getClientProfile();
            ClientJpaEntity clientEntity = clientRepo.findByUserId(userId).orElse(new ClientJpaEntity());
            if (clientEntity.getId() == null) {
                clientEntity.setId(cp != null && cp.getId() != null ? cp.getId() : UUID.randomUUID());
            }
            clientEntity.setUserId(userId);
            clientEntity.setDocumentType(cp != null && cp.getDocumentType() != null ? cp.getDocumentType() : "NIT");
            clientEntity.setDocumentNumber(cp != null && cp.getDocumentNumber() != null ? cp.getDocumentNumber() : "CLI-" + userId.toString().substring(0, 8));
            clientEntity.setAddress(cp != null ? cp.getAddress() : "Dirección General");
            clientEntity.setLat(cp != null ? cp.getLat() : 4.6097);
            clientEntity.setLng(cp != null ? cp.getLng() : -74.0817);
            clientRepo.save(clientEntity);
        }

        // Guardar o actualizar perfil de proveedor
        if (targetRole == Role.ROLE_PROVEEDOR) {
            ProviderProfile pp = user.getProviderProfile();
            ProviderJpaEntity providerEntity = providerRepo.findByUserId(userId).orElse(new ProviderJpaEntity());
            if (providerEntity.getId() == null) {
                providerEntity.setId(pp != null && pp.getId() != null ? pp.getId() : UUID.randomUUID());
            }
            providerEntity.setUserId(userId);
            providerEntity.setCompanyName(pp != null && pp.getCompanyName() != null ? pp.getCompanyName() : user.getFullName());
            providerEntity.setTaxId(pp != null && pp.getTaxId() != null ? pp.getTaxId() : "PRV-" + userId.toString().substring(0, 8));
            providerEntity.setCommercialAddress(pp != null ? pp.getCommercialAddress() : "Sede Principal");
            providerEntity.setLat(pp != null ? pp.getLat() : 4.6097);
            providerEntity.setLng(pp != null ? pp.getLng() : -74.0817);
            providerEntity.setRating(pp != null && pp.getRating() != null ? pp.getRating() : 5.0);
            providerRepo.save(providerEntity);
        }

        return toDomain(savedUser);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        String u = username.trim();
        if (u.contains("@")) {
            return findByEmail(u);
        }
        List<UserJpaEntity> matches = userRepo.findByIdentifierMatches(u);
        if (!matches.isEmpty()) {
            return Optional.of(toDomain(matches.get(0)));
        }
        return userRepo.findByEmail(u).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepo.findByEmail(email.trim().toLowerCase()).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return userRepo.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public List<User> findAll() {
        return userRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        if (role == null) return List.of();
        return userRepo.findByRoleName(role.name()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return userRepo.count();
    }

    private User toDomain(UserJpaEntity e) {
        if (e == null) return null;
        User user = new User();
        user.setId(e.getId());
        user.setEmail(e.getEmail());
        String username = e.getEmail().contains("@") ? e.getEmail().split("@")[0] : e.getEmail();
        user.setUsername(username);
        user.setPassword(e.getPasswordHash());
        user.setFullName(e.getFullName());
        user.setPhone(e.getPhone());
        user.setActive(e.isActive());
        if (e.getCreatedAt() != null) {
            user.setCreatedAt(LocalDateTime.ofInstant(e.getCreatedAt(), ZoneId.systemDefault()));
        }

        Role detectedRole = Role.ROLE_CLIENTE;
        if (e.getRoles() != null && !e.getRoles().isEmpty()) {
            for (RoleJpaEntity r : e.getRoles()) {
                try {
                    detectedRole = Role.fromString(r.getName());
                    break;
                } catch (IllegalArgumentException ignored) {}
            }
        }
        user.setRole(detectedRole);

        // Adjuntar perfil cliente si existe
        clientRepo.findByUserId(e.getId()).ifPresent(c -> {
            ClientProfile cp = new ClientProfile();
            cp.setId(c.getId());
            cp.setUserId(c.getUserId());
            cp.setDocumentType(c.getDocumentType());
            cp.setDocumentNumber(c.getDocumentNumber());
            cp.setAddress(c.getAddress());
            cp.setLat(c.getLat());
            cp.setLng(c.getLng());
            if (c.getCreatedAt() != null) {
                cp.setCreatedAt(LocalDateTime.ofInstant(c.getCreatedAt(), ZoneId.systemDefault()));
            }
            cp.setCompanyName(user.getFullName());
            user.setClientProfile(cp);
        });

        // Adjuntar perfil proveedor si existe
        providerRepo.findByUserId(e.getId()).ifPresent(p -> {
            ProviderProfile pp = new ProviderProfile();
            pp.setId(p.getId());
            pp.setUserId(p.getUserId());
            pp.setCompanyName(p.getCompanyName());
            pp.setTaxId(p.getTaxId());
            pp.setCommercialAddress(p.getCommercialAddress());
            pp.setLat(p.getLat());
            pp.setLng(p.getLng());
            pp.setRating(p.getRating());
            if (p.getCreatedAt() != null) {
                pp.setCreatedAt(LocalDateTime.ofInstant(p.getCreatedAt(), ZoneId.systemDefault()));
            }
            user.setProviderProfile(pp);
        });

        return user;
    }
}
