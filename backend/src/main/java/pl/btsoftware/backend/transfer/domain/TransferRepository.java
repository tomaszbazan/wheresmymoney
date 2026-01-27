package pl.btsoftware.backend.transfer.domain;

import java.util.List;
import java.util.Optional;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.users.domain.GroupId;

public interface TransferRepository {
    void store(Transfer transfer);

    Optional<Transfer> findById(TransferId id, GroupId groupId);

    List<Transfer> findAll(GroupId groupId);
}
