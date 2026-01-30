package pl.btsoftware.backend.transaction.infrastructure.api;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import pl.btsoftware.backend.transaction.application.BillCommand;

public record BillRequest(@NotEmpty List<BillItemRequest> billItems) {
    public BillRequest {
        billItems = List.copyOf(billItems);
    }

    public BillCommand toCommand() {
        return new BillCommand(
                billItems.stream().map(BillItemRequest::toCommand).toList());
    }
}
