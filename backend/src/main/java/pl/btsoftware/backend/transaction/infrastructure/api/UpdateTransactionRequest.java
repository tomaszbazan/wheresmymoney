package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;

public record UpdateTransactionRequest(BillRequest bill) {

    public UpdateTransactionCommand toCommand(TransactionId transactionId) {
        var billCommand = bill != null ? bill.toCommand() : null;
        return new UpdateTransactionCommand(transactionId, billCommand);
    }
}
