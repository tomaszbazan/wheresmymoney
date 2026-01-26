package pl.btsoftware.backend.transfer.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaTransferRepository implements TransferRepository {

    private final TransferJpaRepository repository;

    @Override
    public void store(Transfer transfer) {
        repository.save(TransferEntity.fromDomain(transfer));
    }

    @Override
    public Optional<Transfer> findById(TransferId id, GroupId groupId) {
        return repository.findByIdAndCreatedByGroup(id.value(), groupId.value())
                .map(TransferEntity::toDomain);
    }

    @Override
    public List<Transfer> findAll(GroupId groupId) {
        return repository.findAllByCreatedByGroup(groupId.value()).stream()
                .map(TransferEntity::toDomain)
                .toList();
    }
}
