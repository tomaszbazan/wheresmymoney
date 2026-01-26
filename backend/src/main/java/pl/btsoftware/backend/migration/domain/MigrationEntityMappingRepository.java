package pl.btsoftware.backend.migration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MigrationEntityMappingRepository extends JpaRepository<MigrationEntityMapping, UUID> {

    Optional<MigrationEntityMapping> findByEntityTypeAndOldId(String entityType, Integer oldId);

    Optional<MigrationEntityMapping> findByEntityTypeAndOldName(String entityType, String oldName);

    boolean existsByEntityTypeAndOldId(String entityType, Integer oldId);
}
