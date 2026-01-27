package pl.btsoftware.backend.users.infrastructure.persistance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.users.domain.InvitationStatus;

@Repository
public interface GroupInvitationJpaRepository extends JpaRepository<GroupInvitationEntity, UUID> {
    Optional<GroupInvitationEntity> findByInvitationToken(String token);

    List<GroupInvitationEntity> findByGroupIdAndStatus(UUID groupId, InvitationStatus status);

    List<GroupInvitationEntity> findByInviteeEmailAndStatus(String email, InvitationStatus status);

    @Modifying
    @Query("DELETE FROM GroupInvitationEntity g WHERE g.expiresAt < :now OR g.status = 'EXPIRED'")
    void deleteExpired(@Param("now") Instant now);
}
