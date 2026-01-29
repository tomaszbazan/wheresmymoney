package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.validation.NameValidationRules;
import pl.btsoftware.backend.transaction.domain.error.BillItemDescriptionInvalidCharactersException;
import pl.btsoftware.backend.transaction.domain.error.BillItemDescriptionTooLongException;

public record BillItem(BillItemId id, CategoryId categoryId, Money amount, String description) {
    public BillItem(BillItemId id, CategoryId categoryId, Money amount, String description) {
        NameValidationRules.validate(
                description,
                null,
                BillItemDescriptionTooLongException::new,
                BillItemDescriptionInvalidCharactersException::new);
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description != null ? description.trim() : null;
    }
}
