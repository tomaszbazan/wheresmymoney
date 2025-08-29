package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.domain.UserRepository;

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
        var entity = UserEntity.from(user);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepository.findById(userId.value())
            .map(UserEntity::toDomain);
    }

    @Override
    public List<User> findByGroupId(GroupId groupId) {
        return jpaRepository.findByGroupId(groupId.value())
            .stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId userId) {
        jpaRepository.deleteById(userId.value());
    }
}