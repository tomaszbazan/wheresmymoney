package pl.btsoftware.backend.transaction.infrastructure.api;

import java.util.List;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;

public record UpdateTransactionRequest(Money amount, List<BillItemRequest> billItems) {
    public UpdateTransactionRequest {
        billItems = billItems != null ? List.copyOf(billItems) : null;
    }

    public UpdateTransactionCommand toCommand(TransactionId transactionId) {
        var billItemCommands =
                billItems != null
                        ? billItems.stream().map(BillItemRequest::toCommand).toList()
                        : null;
        return new UpdateTransactionCommand(transactionId, amount, billItemCommands);
    }
}
