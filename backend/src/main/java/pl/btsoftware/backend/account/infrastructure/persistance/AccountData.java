package pl.btsoftware.backend.account.infrastructure.persistance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import pl.btsoftware.backend.shared.TransactionId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
public class AccountData {
    List<UUID> transactionIds;

    @JsonCreator
    public AccountData(@JsonProperty("transactionIds") List<UUID> transactionIds) {
        this.transactionIds = transactionIds != null ? new ArrayList<>(transactionIds) : new ArrayList<>();
    }

    public static AccountData from(List<TransactionId> transactionIds) {
        List<UUID> uuids = transactionIds.stream()
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