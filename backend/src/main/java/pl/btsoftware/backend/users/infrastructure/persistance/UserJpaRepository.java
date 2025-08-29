package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    List<UserEntity> findByGroupId(UUID groupId);
}