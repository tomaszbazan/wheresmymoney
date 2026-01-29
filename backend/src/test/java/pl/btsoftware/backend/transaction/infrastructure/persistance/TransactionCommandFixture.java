package pl.btsoftware.backend.transaction.infrastructure.persistance;

import java.time.LocalDate;
import java.util.List;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.BillCommand;
import pl.btsoftware.backend.transaction.application.BillItemCommand;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

public final class TransactionCommandFixture {
    public static CreateTransactionCommand createCommand(
            AccountId accountId,
            Money amount,
            String description,
            LocalDate transactionDate,
            TransactionType type,
            CategoryId categoryId,
            UserId userId) {
        var billItem = new BillItemCommand(categoryId, amount, description);
        var billCommand = new BillCommand(List.of(billItem));
        return new CreateTransactionCommand(accountId, transactionDate, type, billCommand, userId);
    }
}
