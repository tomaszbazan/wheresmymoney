package pl.btsoftware.backend.transaction.application;

import java.util.List;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;

public record UpdateTransactionCommand(
        TransactionId transactionId, Money amount, List<BillItemCommand> billItems) {
    public UpdateTransactionCommand {
        billItems = billItems != null ? List.copyOf(billItems) : null;
    }
}
