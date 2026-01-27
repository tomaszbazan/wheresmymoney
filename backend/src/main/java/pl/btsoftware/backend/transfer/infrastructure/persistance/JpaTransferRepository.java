package pl.btsoftware.backend.transfer.infrastructure.persistance;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaTransferRepository implements TransferRepository {

    private final TransferJpaRepository repository;

    @Override
    public void store(Transfer transfer) {
        repository
                .findById(transfer.id().value())
                .ifPresentOrElse(
                        existing -> {
                            var updated =
                                    new TransferEntity(
                                            existing.getId(),
                                            transfer.sourceAccountId().value(),
                                            transfer.targetAccountId().value(),
                                            transfer.sourceAmount().value(),
                                            transfer.sourceAmount().currency(),
                                            transfer.targetAmount().value(),
                                            transfer.targetAmount().currency(),
                                            transfer.exchangeRate().rate(),
                                            transfer.description(),
                                            existing.getCreatedAt(),
                                            existing.getCreatedBy(),
                                            existing.getCreatedByGroup(),
                                            transfer.updatedInfo().when(),
                                            transfer.updatedInfo().who().value(),
                                            transfer.tombstone().isDeleted(),
                                            transfer.tombstone().deletedAt(),
                                            existing.getVersion());
                            repository.save(updated);
                        },
                        () -> repository.save(TransferEntity.fromDomain(transfer)));
    }

    @Override
    public Optional<Transfer> findById(TransferId id, GroupId groupId) {
        return repository
                .findByIdAndCreatedByGroup(id.value(), groupId.value())
                .map(TransferEntity::toDomain);
    }

    @Override
    public List<Transfer> findAll(GroupId groupId) {
        return repository.findAllByCreatedByGroup(groupId.value()).stream()
                .map(TransferEntity::toDomain)
                .toList();
    }
}
