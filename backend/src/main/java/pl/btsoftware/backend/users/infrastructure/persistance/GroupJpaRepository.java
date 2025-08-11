package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupJpaRepository extends JpaRepository<GroupEntity, UUID> {
}