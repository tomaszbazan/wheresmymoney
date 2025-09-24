package pl.btsoftware.backend.account.infrastructure.persistance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.TransactionId;

import java.util.List;
import java.util.UUID;

public record AccountData(List<UUID> transactionIds) {

    @JsonCreator
    public AccountData(@JsonProperty("transactionIds") List<UUID> transactionIds) {
        this.transactionIds = transactionIds != null ? List.copyOf(transactionIds) : List.of();
    }

    public static AccountData from(Account account) {
        List<UUID> uuids = account.transactionIds().stream()
                .map(TransactionId::value)
                .toList();
        return new AccountData(uuids);
    }

    public List<TransactionId> toTransactionIds() {
        return transactionIds.stream()
                .map(TransactionId::of)
                .toList();
    }
}
