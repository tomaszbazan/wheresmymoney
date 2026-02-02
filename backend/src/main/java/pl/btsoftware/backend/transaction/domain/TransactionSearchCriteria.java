package pl.btsoftware.backend.transaction.domain;

import java.util.Set;
import pl.btsoftware.backend.shared.TransactionType;

public record TransactionSearchCriteria(Set<TransactionType> types) {

    public TransactionSearchCriteria {
        types = types == null ? Set.of() : Set.copyOf(types);
    }

    @Override
    public Set<TransactionType> types() {
        return Set.copyOf(types);
    }

    public static TransactionSearchCriteria empty() {
        return new TransactionSearchCriteria(Set.of());
    }
}
