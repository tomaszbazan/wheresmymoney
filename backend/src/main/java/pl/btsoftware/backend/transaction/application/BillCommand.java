package pl.btsoftware.backend.transaction.application;

import java.util.List;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.transaction.domain.Bill;
import pl.btsoftware.backend.transaction.domain.BillId;

public record BillCommand(List<BillItemCommand> billItems) {
    public BillCommand {
        billItems = List.copyOf(billItems);
    }

    public Bill toDomain(Currency currency) {
        return new Bill(
                BillId.generate(),
                billItems.stream().map(item -> item.toDomain(currency)).toList());
    }

    public List<String> billItemsDescription() {
        return billItems.stream().map(BillItemCommand::description).toList();
    }
}
