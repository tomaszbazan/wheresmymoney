package pl.btsoftware.backend.transfer.api.dto;

import pl.btsoftware.backend.shared.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferView(
        UUID id,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal sourceAmount,
        Currency sourceCurrency,
        BigDecimal targetAmount,
        Currency targetCurrency,
        BigDecimal exchangeRate,
        String description,
        Instant createdAt) {
}
