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

        // Validate description length
        validateDescriptionLength(command.description());

        Transaction transaction = command.toDomain();
        transactionRepository.store(transaction);
        accountModuleFacade.addTransaction(command.accountId().value(), command.amount(), command.type().name());

        return transaction;
    }

    private void validateDescriptionLength(String description) {
        if (description == null || description.trim().isEmpty() || description.length() > 200) {
            throw new IllegalArgumentException("Description must be between 1 and 200 characters");
        }
    }
}