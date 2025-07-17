package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountModuleFacade accountModuleFacade;

    @Transactional // TODO: Verify if transactional works correctly in integration tests
    public Transaction createTransaction(CreateTransactionCommand command) {
        // Validate account exists before creating transaction
        accountModuleFacade.getAccount(command.accountId().value());

        Transaction transaction = command.toDomain();
        transactionRepository.store(transaction);
        accountModuleFacade.addTransaction(command.accountId().value(), command.amount(), command.type().name());

        return transaction;
    }
}