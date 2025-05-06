package pl.btsoftware.wheresmymoney.account.infrastructure.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.domain.AccountId;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseId;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

@Entity
@Table(name = "account")
@Getter
public class AccountEntity {
    @Id
    private UUID id;
    
    private String name;
//
//    @ElementCollection
//    @CollectionTable(name = "account_expenses", joinColumns = @JoinColumn(name = "account_id"))
//    @Column(name = "expense_id")
//    private List<UUID> expenseIds = new ArrayList<>();
//
    // JPA requires a default constructor
    protected AccountEntity() {
    }
    
    public AccountEntity(UUID id, String name, List<UUID> expenseIds) {
        this.id = id;
        this.name = name;
//        this.expenseIds = expenseIds != null ? new ArrayList<>(expenseIds) : new ArrayList<>();
    }
    
    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
            account.id().value(),
            account.name(),
            account.getExpenseIds().stream()
                .map(ExpenseId::value)
                .collect(Collectors.toList())
        );
    }
    
    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
//        List<ExpenseId> expenses = expenseIds.stream()
//            .map(ExpenseId::from)
//            .collect(Collectors.toList());
        return new Account(accountId, name, EMPTY_LIST);
    }
}