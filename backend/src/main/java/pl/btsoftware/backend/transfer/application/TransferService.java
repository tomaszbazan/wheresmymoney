package pl.btsoftware.backend.transfer.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.ExchangeRate;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransferId;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.transfer.domain.error.TransferToSameAccountException;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@Service
@AllArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountModuleFacade accountModuleFacade;
    private final UsersModuleFacade usersModuleFacade;

    @Transactional
    public Transfer createTransfer(CreateTransferCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());

        var sourceAccount = accountModuleFacade.getAccount(command.sourceAccountId(), user.groupId());
        var targetAccount = accountModuleFacade.getAccount(command.targetAccountId(), user.groupId());

        if (sourceAccount.id().equals(targetAccount.id())) {
            throw new TransferToSameAccountException();
        }

        var sourceAmount = Money.of(command.sourceAmount(), sourceAccount.balance().currency());
        var targetAmount = Money.of(command.targetAmount(), targetAccount.balance().currency());
        var exchangeRate = calculateExchangeRate(sourceAmount, targetAmount);

        var auditInfo = AuditInfo.create(command.userId(), user.groupId());

        var transfer = Transfer.create(
                command.sourceAccountId(),
                command.targetAccountId(),
                sourceAmount,
                targetAmount,
                exchangeRate,
                command.description(),
                auditInfo);

        transferRepository.store(transfer);

        accountModuleFacade.withdraw(
                sourceAccount.id(),
                sourceAmount,
                command.userId());

        accountModuleFacade.deposit(
                targetAccount.id(),
                targetAmount,
                command.userId());

        return transfer;
    }

    public Transfer getTransfer(TransferId id, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transferRepository.findById(id, user.groupId())
                .orElseThrow(pl.btsoftware.backend.transfer.domain.error.TransferNotFoundException::new);
    }

    public List<Transfer> getTransfers(UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transferRepository.findAll(user.groupId());
    }

    private ExchangeRate calculateExchangeRate(Money sourceAmount, Money targetAmount) {
        var sourceCurrency = sourceAmount.currency();
        var targetCurrency = targetAmount.currency();

        if (sourceCurrency.equals(targetCurrency)) {
            return ExchangeRate.identity(sourceCurrency);
        }

        return ExchangeRate.calculate(sourceAmount, targetAmount);
    }
}
