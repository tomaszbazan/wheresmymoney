package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByExternalAuthId(String externalAuthId);
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByGroupId(UUID groupId);
    boolean existsByExternalAuthId(String externalAuthId);
}