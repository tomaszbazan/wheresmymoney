package pl.btsoftware.backend.migration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MigrationSyncLogRepository extends JpaRepository<MigrationSyncLog, UUID> {
}
