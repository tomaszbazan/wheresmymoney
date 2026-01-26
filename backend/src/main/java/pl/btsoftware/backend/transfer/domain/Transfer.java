package pl.btsoftware.backend.transfer.domain;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.shared.validation.NameValidationRules;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionInvalidCharactersException;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionTooLongException;
import pl.btsoftware.backend.users.domain.GroupId;

public record Transfer(
        TransferId id,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money sourceAmount,
        Money targetAmount,
        ExchangeRate exchangeRate,
        String description,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone) {
    public static Transfer create(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money sourceAmount,
            Money targetAmount,
            ExchangeRate exchangeRate,
            String description,
            AuditInfo createdInfo) {
        NameValidationRules.validate(
                description,
                null,
                TransferDescriptionTooLongException::new,
                TransferDescriptionInvalidCharactersException::new);

        return new Transfer(
                TransferId.generate(),
                sourceAccountId,
                targetAccountId,
                sourceAmount,
                targetAmount,
                exchangeRate,
                description,
                createdInfo,
                createdInfo,
                Tombstone.active());
    }

    public GroupId ownedBy() {
        return createdInfo.fromGroup();
    }
}
