package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.users.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepository implements UserRepository {
    private final UserJpaRepository jpaRepository;

    public JpaUserRepository(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.from(user);
        UserEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepository.findById(userId.getValue())
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByExternalAuthId(String externalAuthId) {
        return jpaRepository.findByExternalAuthId(externalAuthId)
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(UserEntity::toDomain);
    }

    @Override
    public List<User> findByGroupId(GroupId groupId) {
        return jpaRepository.findByGroupId(groupId.getValue())
            .stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId userId) {
        jpaRepository.deleteById(userId.getValue());
    }

    @Override
    public boolean existsByExternalAuthId(String externalAuthId) {
        return jpaRepository.existsByExternalAuthId(externalAuthId);
    }
}