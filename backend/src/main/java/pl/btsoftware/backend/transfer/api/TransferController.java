package pl.btsoftware.backend.transfer.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.TransferModuleFacade;
import pl.btsoftware.backend.transfer.api.dto.CreateTransferRequest;
import pl.btsoftware.backend.transfer.api.dto.TransferView;
import pl.btsoftware.backend.transfer.api.dto.TransfersView;
import pl.btsoftware.backend.transfer.application.CreateTransferCommand;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
class TransferController {
    private final TransferModuleFacade transferModuleFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TransferView createTransfer(@RequestBody @Valid CreateTransferRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());

        var targetAmount = request.targetAmount() != null ? request.targetAmount() : request.sourceAmount();

        var command = new CreateTransferCommand(
                new AccountId(request.sourceAccountId()),
                new AccountId(request.targetAccountId()),
                request.sourceAmount(),
                targetAmount,
                request.description(),
                userId);

        var transfer = transferModuleFacade.createTransfer(command);
        return toView(transfer);
    }

    @GetMapping
    TransfersView getTransfers(@AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        var transfers = transferModuleFacade.getTransfers(userId);
        return new TransfersView(transfers.stream().map(this::toView).toList());
    }

    @GetMapping("/{id}")
    TransferView getTransfer(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        var transfer = transferModuleFacade.getTransfer(new TransferId(id), userId);
        return toView(transfer);
    }

    private TransferView toView(Transfer transfer) {
        return new TransferView(
                transfer.id().value(),
                transfer.sourceAccountId().value(),
                transfer.targetAccountId().value(),
                transfer.sourceAmount().value(),
                transfer.sourceAmount().currency(),
                transfer.targetAmount().value(),
                transfer.targetAmount().currency(),
                transfer.exchangeRate().rate(),
                transfer.description(),
                transfer.createdInfo().when().toInstant());
    }
}
