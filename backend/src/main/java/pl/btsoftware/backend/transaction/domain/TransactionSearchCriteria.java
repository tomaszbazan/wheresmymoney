package pl.btsoftware.backend.transaction.domain;

import static pl.btsoftware.backend.shared.TransactionType.INCOME;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionType;

public record TransactionSearchCriteria(
        Set<TransactionType> types,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Set<AccountId> accountIds,
        Set<CategoryId> categoryIds,
        String description) {

    public TransactionSearchCriteria {
        types = types == null ? Set.of() : Set.copyOf(types);
        accountIds = accountIds == null ? Set.of() : Set.copyOf(accountIds);
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
    }

    @Override
    public Set<TransactionType> types() {
        return Set.copyOf(types);
    }

    @Override
    public Set<AccountId> accountIds() {
        return Set.copyOf(accountIds);
    }

    @Override
    public Set<CategoryId> categoryIds() {
        return Set.copyOf(categoryIds);
    }

    public static TransactionSearchCriteria empty() {
        return new TransactionSearchCriteria(Set.of(), null, null, null, null, Set.of(), Set.of(), null);
    }

    public static TransactionSearchCriteria incomes() {
        return new TransactionSearchCriteria(Set.of(INCOME), null, null, null, null, Set.of(), Set.of(), null);
    }

    public static TransactionSearchCriteria from(
            Set<TransactionType> types,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Set<UUID> accountIds,
            Set<UUID> categoryIds,
            String description) {
        var accountIdSet = accountIds != null
                ? accountIds.stream().map(AccountId::from).collect(Collectors.toSet())
                : Set.<AccountId>of();

        var categoryIdSet = categoryIds != null
                ? categoryIds.stream().map(CategoryId::of).collect(Collectors.toSet())
                : Set.<CategoryId>of();

        return new TransactionSearchCriteria(
                types, dateFrom, dateTo, minAmount, maxAmount, accountIdSet, categoryIdSet, description);
    }
}
