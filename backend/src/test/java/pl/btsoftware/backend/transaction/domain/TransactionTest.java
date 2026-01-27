package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionInvalidCharactersException;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionTooLongException;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class TransactionTest {

    @Test
    void shouldCreateTransactionWithGeneratedId() {
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var description = "Test transaction";
        var categoryId = CategoryId.generate();
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        var transaction =
                Transaction.create(
                        accountId,
                        amount,
                        description,
                        EXPENSE,
                        categoryId,
                        transactionDate,
                        transactionHash,
                        auditInfo);

        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(accountId);
        assertThat(transaction.amount()).isEqualTo(amount);
        assertThat(transaction.description()).isEqualTo(description);
        assertThat(transaction.type()).isEqualTo(EXPENSE);
        assertThat(transaction.categoryId()).isEqualTo(categoryId);
        assertThat(transaction.transactionDate()).isEqualTo(transactionDate);
        assertThat(transaction.transactionHash()).isEqualTo(transactionHash);
        assertThat(transaction.createdInfo()).isEqualTo(auditInfo);
        assertThat(transaction.updatedInfo()).isEqualTo(auditInfo);
        assertThat(transaction.tombstone().isDeleted()).isFalse();
        assertThat(transaction.isDeleted()).isFalse();
    }

    @Test
    void shouldTrimDescriptionInConstructor() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::description), "  Test description  ")
                        .create();

        assertThat(transaction.description()).isEqualTo("Test description");
    }

    @Test
    void shouldAcceptNullDescription() {
        var transaction =
                Instancio.of(Transaction.class).set(field(Transaction::description), null).create();

        assertThat(transaction.description()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var tooLongDescription = "a".repeat(201);
        var categoryId = CategoryId.generate();
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        assertThatThrownBy(
                        () ->
                                new Transaction(
                                        TransactionId.generate(),
                                        accountId,
                                        amount,
                                        EXPENSE,
                                        tooLongDescription,
                                        categoryId,
                                        transactionDate,
                                        transactionHash,
                                        auditInfo,
                                        auditInfo,
                                        Tombstone.active()))
                .isInstanceOf(TransactionDescriptionTooLongException.class);
    }

    @Test
    void shouldRejectDescriptionWithInvalidCharacters() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var categoryId = CategoryId.generate();
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        // when & then
        assertThatThrownBy(
                        () ->
                                new Transaction(
                                        TransactionId.generate(),
                                        accountId,
                                        amount,
                                        EXPENSE,
                                        "test$description",
                                        categoryId,
                                        transactionDate,
                                        transactionHash,
                                        auditInfo,
                                        auditInfo,
                                        Tombstone.active()))
                .isInstanceOf(TransactionDescriptionInvalidCharactersException.class);
    }

    @Test
    void shouldAcceptDescriptionWithMaximumLength() {
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var maxLengthDescription = "a".repeat(100);
        var categoryId = CategoryId.generate();
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        var transaction =
                new Transaction(
                        TransactionId.generate(),
                        accountId,
                        amount,
                        EXPENSE,
                        maxLengthDescription,
                        categoryId,
                        transactionDate,
                        transactionHash,
                        auditInfo,
                        auditInfo,
                        Tombstone.active());

        assertThat(transaction.description()).hasSize(100);
    }

    @Test
    void shouldReturnCreatedBy() {
        var userId = UserId.generate();
        var auditInfo = Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.createdBy()).isEqualTo(userId);
    }

    @Test
    void shouldReturnLastUpdatedBy() {
        var userId = UserId.generate();
        var auditInfo = Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::updatedInfo), auditInfo)
                        .create();

        assertThat(transaction.lastUpdatedBy()).isEqualTo(userId);
    }

    @Test
    void shouldReturnOwnedBy() {
        var groupId = GroupId.generate();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::fromGroup), groupId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.ownedBy()).isEqualTo(groupId);
    }

    @Test
    void shouldReturnCreatedAt() {
        var timestamp = OffsetDateTime.now();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.createdAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldReturnLastUpdatedAt() {
        var timestamp = OffsetDateTime.now();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::updatedInfo), auditInfo)
                        .create();

        assertThat(transaction.lastUpdatedAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldUpdateAmount() {
        var originalAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(200), PLN);
        var userId = UserId.generate();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::amount), originalAmount)
                        .create();

        var updatedTransaction = transaction.updateAmount(newAmount, userId);

        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.amount()).isEqualTo(originalAmount);
    }

    @Test
    void shouldUpdateDescription() {
        var originalDescription = "Original";
        var newDescription = "Updated";
        var userId = UserId.generate();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::description), originalDescription)
                        .create();

        var updatedTransaction = transaction.updateDescription(newDescription, userId);

        assertThat(updatedTransaction.description()).isEqualTo(newDescription);
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.description()).isEqualTo(originalDescription);
    }

    @Test
    void shouldUpdateCategory() {
        var originalCategoryId = CategoryId.generate();
        var newCategoryId = CategoryId.generate();
        var userId = UserId.generate();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::categoryId), originalCategoryId)
                        .create();

        var updatedTransaction = transaction.updateCategory(newCategoryId, userId);

        assertThat(updatedTransaction.categoryId()).isEqualTo(newCategoryId);
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.categoryId()).isEqualTo(originalCategoryId);
    }

    @Test
    void shouldDeleteTransaction() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.active())
                        .create();

        var deletedTransaction = transaction.delete();

        assertThat(deletedTransaction.isDeleted()).isTrue();
        assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();
        assertThat(deletedTransaction.tombstone().deletedAt()).isNotNull();
        assertThat(deletedTransaction.id()).isEqualTo(transaction.id());
        assertThat(transaction.isDeleted()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTransactionIsDeleted() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.deleted())
                        .create();

        assertThat(transaction.isDeleted()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTransactionIsNotDeleted() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.active())
                        .create();

        assertThat(transaction.isDeleted()).isFalse();
    }
}
