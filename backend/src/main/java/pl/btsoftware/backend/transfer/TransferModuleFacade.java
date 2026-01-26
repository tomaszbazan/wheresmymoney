package pl.btsoftware.backend.transfer;

import lombok.AllArgsConstructor;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.application.CreateTransferCommand;
import pl.btsoftware.backend.transfer.application.TransferService;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@AllArgsConstructor
public class TransferModuleFacade {
    private final TransferService transferService;

    public Transfer createTransfer(CreateTransferCommand command) {
        return transferService.createTransfer(command);
    }

    public Transfer getTransfer(TransferId transferId, UserId userId) {
        return transferService.getTransfer(transferId, userId);
    }

    public List<Transfer> getTransfers(UserId userId) {
        return transferService.getTransfers(userId);
    }
}
