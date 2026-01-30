package pl.btsoftware.backend.transfer.infrastructure.persistance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.ExchangeRate;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class JpaTransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransferJpaRepository transferJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldStoreAndRetrieveTransfer() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId, groupId);
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var transfer = Transfer.create(
                sourceAccountId, targetAccountId, sourceAmount, targetAmount, exchangeRate, "Test transfer", auditInfo);

        transferRepository.store(transfer);
        var retrievedTransfer = transferRepository.findById(transfer.id(), groupId);

        assertThat(retrievedTransfer).isPresent();
        assertThat(retrievedTransfer.get().id()).isEqualTo(transfer.id());
        assertThat(retrievedTransfer.get().sourceAccountId()).isEqualTo(sourceAccountId);
        assertThat(retrievedTransfer.get().targetAccountId()).isEqualTo(targetAccountId);
        assertThat(retrievedTransfer.get().sourceAmount()).isEqualTo(sourceAmount);
        assertThat(retrievedTransfer.get().targetAmount()).isEqualTo(targetAmount);
        assertThat(retrievedTransfer.get().exchangeRate().rate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(retrievedTransfer.get().description()).isEqualTo("Test transfer");
        assertThat(retrievedTransfer.get().ownedBy()).isEqualTo(groupId);
    }

    @Test
    void shouldStoreTransferWithDifferentCurrencies() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId, groupId);
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("23.50"), EUR);
        var exchangeRate = ExchangeRate.calculate(sourceAmount, targetAmount);
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                "Cross-currency transfer",
                auditInfo);

        transferRepository.store(transfer);
        var retrievedTransfer = transferRepository.findById(transfer.id(), groupId);

        assertThat(retrievedTransfer).isPresent();
        assertThat(retrievedTransfer.get().sourceAmount().currency()).isEqualTo(PLN);
        assertThat(retrievedTransfer.get().targetAmount().currency()).isEqualTo(EUR);
        assertThat(retrievedTransfer.get().exchangeRate().rate()).isEqualByComparingTo(new BigDecimal("0.235000"));
    }

    @Test
    void shouldReturnEmptyWhenTransferNotFound() {
        var nonExistingId = TransferId.generate();
        var nonExistingGroup = GroupId.generate();

        var result = transferRepository.findById(nonExistingId, nonExistingGroup);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllTransfersByGroup() {
        var groupX = GroupId.generate();
        var groupY = GroupId.generate();
        var userA = UserId.generate();
        var userB = UserId.generate();

        var auditInfoGroupX = AuditInfo.create(userA, groupX);
        var auditInfoGroupY = AuditInfo.create(userB, groupY);

        var sourceAccountX1 = AccountId.generate();
        var targetAccountX1 = AccountId.generate();
        var sourceAccountX2 = AccountId.generate();
        var targetAccountX2 = AccountId.generate();
        var sourceAccountY = AccountId.generate();
        var targetAccountY = AccountId.generate();

        var transferGroupX1 = Transfer.create(
                sourceAccountX1,
                targetAccountX1,
                Money.of(new BigDecimal("100.00"), PLN),
                Money.of(new BigDecimal("100.00"), PLN),
                ExchangeRate.identity(PLN),
                "Group X Transfer 1",
                auditInfoGroupX);
        var transferGroupX2 = Transfer.create(
                sourceAccountX2,
                targetAccountX2,
                Money.of(new BigDecimal("200.00"), PLN),
                Money.of(new BigDecimal("200.00"), PLN),
                ExchangeRate.identity(PLN),
                "Group X Transfer 2",
                auditInfoGroupX);
        var transferGroupY = Transfer.create(
                sourceAccountY,
                targetAccountY,
                Money.of(new BigDecimal("150.00"), EUR),
                Money.of(new BigDecimal("150.00"), EUR),
                ExchangeRate.identity(EUR),
                "Group Y Transfer",
                auditInfoGroupY);

        transferRepository.store(transferGroupX1);
        transferRepository.store(transferGroupX2);
        transferRepository.store(transferGroupY);

        var groupXTransfers = transferRepository.findAll(groupX);
        var groupYTransfers = transferRepository.findAll(groupY);

        assertThat(groupXTransfers).hasSize(2);
        assertThat(groupXTransfers)
                .extracting("description")
                .containsExactlyInAnyOrder("Group X Transfer 1", "Group X Transfer 2");
        assertThat(groupXTransfers).allMatch(transfer -> transfer.ownedBy().equals(groupX));

        assertThat(groupYTransfers).hasSize(1);
        assertThat(groupYTransfers).extracting("description").containsOnly("Group Y Transfer");
        assertThat(groupYTransfers).allMatch(transfer -> transfer.ownedBy().equals(groupY));
    }

    @Test
    void shouldReturnEmptyListWhenNoTransfersForGroup() {
        var nonExistentGroup = GroupId.generate();

        var transfers = transferRepository.findAll(nonExistentGroup);

        assertThat(transfers).isEmpty();
    }

    @Test
    @Transactional
    void shouldThrowOptimisticLockExceptionOnConcurrentUpdate() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId, groupId);
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                Money.of(new BigDecimal("100.00"), PLN),
                Money.of(new BigDecimal("100.00"), PLN),
                ExchangeRate.identity(PLN),
                "Original description",
                auditInfo);
        transferRepository.store(transfer);
        entityManager.flush();
        entityManager.clear();

        var entity1 = transferJpaRepository.findById(transfer.id().value()).orElseThrow();
        entityManager.detach(entity1);
        var entity2 = transferJpaRepository.findById(transfer.id().value()).orElseThrow();
        entityManager.detach(entity2);

        var updatedEntity1 = new TransferEntity(
                entity1.getId(),
                entity1.getSourceAccountId(),
                entity1.getTargetAccountId(),
                entity1.getSourceAmount(),
                entity1.getSourceCurrency(),
                entity1.getTargetAmount(),
                entity1.getTargetCurrency(),
                entity1.getExchangeRate(),
                "Updated by First",
                entity1.getCreatedAt(),
                entity1.getCreatedBy(),
                entity1.getCreatedByGroup(),
                entity1.getUpdatedAt(),
                entity1.getUpdatedBy(),
                entity1.isDeleted(),
                entity1.getDeletedAt(),
                entity1.getVersion());
        transferJpaRepository.save(updatedEntity1);
        entityManager.flush();
        entityManager.clear();

        var updatedEntity2 = new TransferEntity(
                entity2.getId(),
                entity2.getSourceAccountId(),
                entity2.getTargetAccountId(),
                entity2.getSourceAmount(),
                entity2.getSourceCurrency(),
                entity2.getTargetAmount(),
                entity2.getTargetCurrency(),
                entity2.getExchangeRate(),
                "Updated by Second",
                entity2.getCreatedAt(),
                entity2.getCreatedBy(),
                entity2.getCreatedByGroup(),
                entity2.getUpdatedAt(),
                entity2.getUpdatedBy(),
                entity2.isDeleted(),
                entity2.getDeletedAt(),
                entity2.getVersion());

        assertThatThrownBy(() -> {
                    transferJpaRepository.save(updatedEntity2);
                    entityManager.flush();
                })
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    @Transactional
    void shouldIncrementVersionOnUpdate() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId, groupId);
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                Money.of(new BigDecimal("100.00"), PLN),
                Money.of(new BigDecimal("100.00"), PLN),
                ExchangeRate.identity(PLN),
                "Test transfer",
                auditInfo);
        transferRepository.store(transfer);
        entityManager.flush();
        entityManager.clear();

        var initialEntity =
                transferJpaRepository.findById(transfer.id().value()).orElseThrow();
        var initialVersion = initialEntity.getVersion();
        entityManager.clear();

        var updatedTransfer = new Transfer(
                transfer.id(),
                transfer.sourceAccountId(),
                transfer.targetAccountId(),
                transfer.sourceAmount(),
                transfer.targetAmount(),
                transfer.exchangeRate(),
                "Updated description",
                transfer.createdInfo(),
                transfer.updatedInfo().updateTimestamp(),
                transfer.tombstone());
        transferRepository.store(updatedTransfer);
        entityManager.flush();
        entityManager.clear();

        var updatedEntity =
                transferJpaRepository.findById(transfer.id().value()).orElseThrow();
        assertThat(updatedEntity.getVersion()).isGreaterThan(initialVersion);
    }
}
