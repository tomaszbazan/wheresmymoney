package pl.btsoftware.backend.account.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.Currency;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByNameAndCurrencyAndOwnedByGroup(String name, Currency currency, UUID groupId);
    Optional<AccountEntity> findByIdAndOwnedByGroup(UUID id, UUID groupId);
    List<AccountEntity> findAllByOwnedByGroup(UUID groupId);
}
