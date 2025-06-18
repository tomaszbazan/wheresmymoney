package pl.btsoftware.backend.account.infrastructure.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.Expense;
import pl.btsoftware.backend.account.domain.ExpenseId;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ExpenseEntity {
    @Id
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private OffsetDateTime createdAt;

    public static ExpenseEntity fromDomain(Expense expense) {
        return new ExpenseEntity(
                expense.id().value(),
                expense.accountId().value(),
                expense.amount().amount(),
                expense.amount().currency(),
                expense.description(),
                expense.createdAt()
        );
    }

    public Expense toDomain() {
        return new Expense(
                ExpenseId.from(id),
                pl.btsoftware.backend.account.domain.AccountId.from(accountId),
                new pl.btsoftware.backend.account.domain.Money(amount, currency),
                description,
                createdAt
        );
    }
}
