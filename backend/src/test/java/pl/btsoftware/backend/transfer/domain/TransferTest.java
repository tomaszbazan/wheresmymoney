package pl.btsoftware.backend.transfer.domain;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.ExchangeRate;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionInvalidCharactersException;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionTooLongException;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;

class TransferTest {

    private static Stream<Arguments> invalidDescriptions() {
        return Stream.of(
                arguments("a".repeat(101)),
                arguments("a".repeat(200))
        );
    }

    @Test
    void shouldCreateTransferWithSameCurrency() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var description = "Transfer description";
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                description,
                auditInfo
        );

        // then
        assertThat(transfer).isNotNull();
        assertThat(transfer.id()).isNotNull();
        assertThat(transfer.sourceAccountId()).isEqualTo(sourceAccountId);
        assertThat(transfer.targetAccountId()).isEqualTo(targetAccountId);
        assertThat(transfer.sourceAmount()).isEqualTo(sourceAmount);
        assertThat(transfer.targetAmount()).isEqualTo(targetAmount);
        assertThat(transfer.exchangeRate()).isEqualTo(exchangeRate);
        assertThat(transfer.description()).isEqualTo(description);
        assertThat(transfer.createdInfo()).isEqualTo(auditInfo);
        assertThat(transfer.updatedInfo()).isEqualTo(auditInfo);
        assertThat(transfer.tombstone().isActive()).isTrue();
    }

    @Test
    void shouldCreateTransferWithDifferentCurrencies() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("85.50"), EUR);
        var exchangeRate = ExchangeRate.calculate(sourceAmount, targetAmount);
        var description = "Cross-currency transfer";
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                description,
                auditInfo
        );

        // then
        assertThat(transfer).isNotNull();
        assertThat(transfer.sourceAmount().currency()).isEqualTo(PLN);
        assertThat(transfer.targetAmount().currency()).isEqualTo(EUR);
        assertThat(transfer.exchangeRate().fromCurrency()).isEqualTo(PLN);
        assertThat(transfer.exchangeRate().toCurrency()).isEqualTo(EUR);
    }

    @Test
    void shouldCreateTransferWithNullDescription() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                null,
                auditInfo
        );

        // then
        assertThat(transfer.description()).isNull();
    }

    @Test
    void shouldCreateTransferWithEmptyDescription() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                "",
                auditInfo
        );

        // then
        assertThat(transfer.description()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    void shouldRejectDescriptionTooLong(String description) {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when & then
        assertThatThrownBy(() -> Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                description,
                auditInfo
        )).isInstanceOf(TransferDescriptionTooLongException.class);
    }

    @Test
    void shouldRejectDescriptionWithInvalidCharacters() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when & then
        assertThatThrownBy(() -> Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                "test$description",
                auditInfo
        )).isInstanceOf(TransferDescriptionInvalidCharactersException.class);
    }

    @Test
    void shouldAcceptMaxLengthDescription() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var description = "a".repeat(100);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                description,
                auditInfo
        );

        // then
        assertThat(transfer.description()).hasSize(100);
    }

    @Test
    void shouldGenerateUniqueTransferIds() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("100.00"), PLN);
        var exchangeRate = ExchangeRate.identity(PLN);
        var auditInfo = Instancio.create(AuditInfo.class);

        // when
        var transfer1 = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                "Transfer 1",
                auditInfo
        );

        var transfer2 = Transfer.create(
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                "Transfer 2",
                auditInfo
        );

        // then
        assertThat(transfer1.id()).isNotEqualTo(transfer2.id());
    }
}
