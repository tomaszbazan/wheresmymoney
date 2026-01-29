package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.TransactionId;

public record UpdateTransactionCommand(TransactionId transactionId, BillCommand bill) {}
