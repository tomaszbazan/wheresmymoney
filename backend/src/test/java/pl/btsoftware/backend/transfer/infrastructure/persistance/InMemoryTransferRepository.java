package pl.btsoftware.backend.transfer.infrastructure.persistance;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("test")
public class InMemoryTransferRepository implements TransferRepository {
    private final HashMap<TransferId, Transfer> database = new HashMap<>();

    @Override
    public void store(Transfer transfer) {
        database.put(transfer.id(), transfer);
    }

    @Override
    public Optional<Transfer> findById(TransferId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id))
                .filter(transfer -> transfer.ownedBy().equals(groupId));
    }

    @Override
    public List<Transfer> findAll(GroupId groupId) {
        return database.values().stream()
                .filter(transfer -> transfer.ownedBy().equals(groupId))
                .toList();
    }
}
