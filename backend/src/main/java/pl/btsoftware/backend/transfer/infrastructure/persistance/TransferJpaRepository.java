package pl.btsoftware.backend.transfer.infrastructure.persistance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferJpaRepository extends JpaRepository<TransferEntity, UUID> {
    Optional<TransferEntity> findByIdAndCreatedByGroup(UUID id, UUID groupId);

    List<TransferEntity> findAllByCreatedByGroup(UUID groupId);
}
