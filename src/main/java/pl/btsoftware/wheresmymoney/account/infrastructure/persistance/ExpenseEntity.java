package pl.btsoftware.wheresmymoney.account.infrastructure.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import pl.btsoftware.wheresmymoney.account.domain.Expense;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense")
@Getter
public class ExpenseEntity {
    @Id
    private UUID id;
    
//    @Column(name = "account_id")
//    private UUID accountId;
//
//    private BigDecimal amount;
//
//    private String description;
//
//    private LocalDateTime date;
    
    // JPA requires a default constructor
    protected ExpenseEntity() {
    }
    
    public ExpenseEntity(UUID id, UUID accountId, BigDecimal amount, String description, LocalDateTime date) {
        this.id = id;
//        this.accountId = accountId;
//        this.amount = amount;
//        this.description = description;
//        this.date = date;
    }
    
    public static ExpenseEntity fromDomain(Expense expense) {
        return new ExpenseEntity(
            expense.id().value(),
            expense.accountId().value(),
            expense.amount(),
            expense.description(),
            expense.date()
        );
    }
    
    public Expense toDomain() {
        return new Expense(
            ExpenseId.from(id),
            null, null, null, null
//            AccountId.from(accountId),
//            amount,
//            description,
//            date
        );
    }

}