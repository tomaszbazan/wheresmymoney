package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.users.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaGroupInvitationRepository implements GroupInvitationRepository {
    private final GroupInvitationJpaRepository jpaRepository;

    public JpaGroupInvitationRepository(GroupInvitationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GroupInvitation save(GroupInvitation invitation) {
        GroupInvitationEntity entity = GroupInvitationEntity.from(invitation);
        GroupInvitationEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<GroupInvitation> findById(GroupInvitationId invitationId) {
        return jpaRepository.findById(invitationId.getValue())
            .map(GroupInvitationEntity::toDomain);
    }

    @Override
    public Optional<GroupInvitation> findByToken(String token) {
        return jpaRepository.findByInvitationToken(token)
            .map(GroupInvitationEntity::toDomain);
    }

    @Override
    public List<GroupInvitation> findPendingByGroupId(GroupId groupId) {
        return jpaRepository.findByGroupIdAndStatus(groupId.value(), InvitationStatus.PENDING)
            .stream()
            .map(GroupInvitationEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<GroupInvitation> findPendingByEmail(String email) {
        return jpaRepository.findByInviteeEmailAndStatus(email, InvitationStatus.PENDING)
            .stream()
            .map(GroupInvitationEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(GroupInvitationId invitationId) {
        jpaRepository.deleteById(invitationId.getValue());
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpaRepository.deleteExpired(Instant.now());
    }
}