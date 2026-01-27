package pl.btsoftware.backend.account.infrastructure.persistance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.*;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class JpaAccountRepositoryTest {

    @Autowired private AccountRepository accountRepository;

    @Autowired private AccountJpaRepository accountJpaRepository;

    @Autowired private EntityManager entityManager;

    @Test
    void shouldStoreAndRetrieveAccount() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var balance = Money.of(new BigDecimal("100.50"), EUR);
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        balance,
                        auditInfo,
                        auditInfo,
                        Tombstone.active());

        // when
        accountRepository.store(account);
        var retrievedAccount = accountRepository.findById(account.id(), groupId);

        // then
        assertThat(retrievedAccount).isPresent();
        assertThat(retrievedAccount.get().id()).isEqualTo(account.id());
        assertThat(retrievedAccount.get().name()).isEqualTo("Test Account");
        assertThat(retrievedAccount.get().balance()).isEqualTo(balance);
        assertThat(retrievedAccount.get().createdBy()).isEqualTo(userId);
        assertThat(retrievedAccount.get().ownedBy()).isEqualTo(groupId);
    }

    @Test
    void shouldReturnEmptyWhenAccountNotFound() {
        // given
        var nonExistingId = AccountId.generate();
        var nonExistingGroup = GroupId.generate();

        // when
        var result = accountRepository.findById(nonExistingId, nonExistingGroup);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllAccounts() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());

        var account1 =
                new Account(
                        AccountId.generate(),
                        "Account 1",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        var account2 =
                new Account(
                        AccountId.generate(),
                        "Account 2",
                        Money.of(BigDecimal.ZERO, USD),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());

        accountRepository.store(account1);
        accountRepository.store(account2);

        // when
        var accounts = accountRepository.findAllBy(groupId);

        // then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting("name").containsExactlyInAnyOrder("Account 1", "Account 2");
    }

    @Test
    void shouldDeleteAccount() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var account =
                new Account(
                        AccountId.generate(),
                        "To Delete",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(account);

        // when
        accountRepository.deleteById(account.id());

        // then
        var retrievedAccount = accountRepository.findById(account.id(), groupId);
        assertThat(retrievedAccount).isEmpty();
    }

    @Test
    void shouldFindAccountByNameAndCurrency() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var account =
                new Account(
                        AccountId.generate(),
                        "Unique Account",
                        Money.of(BigDecimal.ZERO, GBP),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(account);

        // when
        var foundAccount = accountRepository.findByNameAndCurrency("Unique Account", GBP, groupId);

        // then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().name()).isEqualTo("Unique Account");
        assertThat(foundAccount.get().balance().currency()).isEqualTo(GBP);
    }

    @Test
    void shouldReturnEmptyWhenAccountWithNameAndCurrencyNotFound() {
        // when
        var result =
                accountRepository.findByNameAndCurrency("Non-existent", EUR, GroupId.generate());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllAccountsByGroup() {
        // given
        var groupX = GroupId.generate();
        var groupY = GroupId.generate();
        var userA = UserId.generate();
        var userC = UserId.generate();

        var auditInfoGroupX = AuditInfo.create(userA.value(), groupX.value());
        var auditInfoGroupY = AuditInfo.create(userC.value(), groupY.value());

        var accountGroupX1 =
                new Account(
                        AccountId.generate(),
                        "Group X Account 1",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfoGroupX,
                        auditInfoGroupX,
                        Tombstone.active());
        var accountGroupX2 =
                new Account(
                        AccountId.generate(),
                        "Group X Account 2",
                        Money.of(BigDecimal.ZERO, EUR),
                        auditInfoGroupX,
                        auditInfoGroupX,
                        Tombstone.active());
        var accountGroupY =
                new Account(
                        AccountId.generate(),
                        "Group Y Account",
                        Money.of(BigDecimal.ZERO, USD),
                        auditInfoGroupY,
                        auditInfoGroupY,
                        Tombstone.active());

        accountRepository.store(accountGroupX1);
        accountRepository.store(accountGroupX2);
        accountRepository.store(accountGroupY);

        // when
        var groupXAccounts = accountRepository.findAllBy(groupX);
        var groupYAccounts = accountRepository.findAllBy(groupY);

        // then
        assertThat(groupXAccounts).hasSize(2);
        assertThat(groupXAccounts)
                .extracting("name")
                .containsExactlyInAnyOrder("Group X Account 1", "Group X Account 2");
        assertThat(groupXAccounts).allMatch(account -> account.ownedBy().equals(groupX));

        assertThat(groupYAccounts).hasSize(1);
        assertThat(groupYAccounts).extracting("name").containsOnly("Group Y Account");
        assertThat(groupYAccounts).allMatch(account -> account.ownedBy().equals(groupY));
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsForGroup() {
        // given
        var nonExistentGroup = GroupId.generate();

        // when
        var accounts = accountRepository.findAllBy(nonExistentGroup);

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldUpdateAccount() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var originalAccount =
                new Account(
                        AccountId.generate(),
                        "Original Name",
                        Money.of(new BigDecimal("50.00"), PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(originalAccount);

        // when
        var updatedAccount = originalAccount.changeName("Updated Name");
        accountRepository.store(updatedAccount);

        // then
        var retrievedAccount = accountRepository.findById(originalAccount.id(), groupId);
        assertThat(retrievedAccount).isPresent();
        assertThat(retrievedAccount.get().name()).isEqualTo("Updated Name");
        assertThat(retrievedAccount.get().balance().value())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(retrievedAccount.get().lastUpdatedAt())
                .isAfter(retrievedAccount.get().createdAt());
    }

    @Test
    @Transactional
    void shouldThrowOptimisticLockExceptionOnConcurrentUpdate() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var account =
                new Account(
                        AccountId.generate(),
                        "Original Name",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(account);
        entityManager.flush();
        entityManager.clear();

        // when
        var entity1 = accountJpaRepository.findById(account.id().value()).orElseThrow();
        entityManager.detach(entity1);
        var entity2 = accountJpaRepository.findById(account.id().value()).orElseThrow();
        entityManager.detach(entity2);

        var updatedEntity1 =
                new AccountEntity(
                        entity1.getId(),
                        "Updated by First",
                        entity1.getBalance(),
                        entity1.getCurrency(),
                        entity1.getCreatedAt(),
                        entity1.getCreatedBy(),
                        entity1.getOwnedByGroup(),
                        entity1.getUpdatedAt(),
                        entity1.getUpdatedBy(),
                        entity1.getVersion());
        accountJpaRepository.save(updatedEntity1);
        entityManager.flush();
        entityManager.clear();

        var updatedEntity2 =
                new AccountEntity(
                        entity2.getId(),
                        "Updated by Second",
                        entity2.getBalance(),
                        entity2.getCurrency(),
                        entity2.getCreatedAt(),
                        entity2.getCreatedBy(),
                        entity2.getOwnedByGroup(),
                        entity2.getUpdatedAt(),
                        entity2.getUpdatedBy(),
                        entity2.getVersion());

        // then
        assertThatThrownBy(
                        () -> {
                            accountJpaRepository.save(updatedEntity2);
                            entityManager.flush();
                        })
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    @Transactional
    void shouldIncrementVersionOnUpdate() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(account);
        entityManager.flush();
        entityManager.clear();

        var initialEntity = accountJpaRepository.findById(account.id().value()).orElseThrow();
        var initialVersion = initialEntity.getVersion();

        // when
        var updatedAccount = account.changeName("Updated Name");
        accountRepository.store(updatedAccount);
        entityManager.flush();
        entityManager.clear();

        // then
        var updatedEntity = accountJpaRepository.findById(account.id().value()).orElseThrow();
        assertThat(updatedEntity.getVersion()).isGreaterThan(initialVersion);
    }

    @Test
    @Transactional
    void shouldAllowUpdateWithCorrectVersion() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value());
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        Money.of(BigDecimal.ZERO, PLN),
                        auditInfo,
                        auditInfo,
                        Tombstone.active());
        accountRepository.store(account);
        entityManager.flush();
        entityManager.clear();

        // when
        var entity = accountJpaRepository.findById(account.id().value()).orElseThrow();
        var updatedEntity =
                new AccountEntity(
                        entity.getId(),
                        "Updated Name",
                        entity.getBalance(),
                        entity.getCurrency(),
                        entity.getCreatedAt(),
                        entity.getCreatedBy(),
                        entity.getOwnedByGroup(),
                        entity.getUpdatedAt(),
                        entity.getUpdatedBy(),
                        entity.getVersion());
        accountJpaRepository.save(updatedEntity);
        entityManager.flush();

        // then
        var retrievedEntity = accountJpaRepository.findById(account.id().value()).orElseThrow();
        assertThat(retrievedEntity.getName()).isEqualTo("Updated Name");
    }
}
