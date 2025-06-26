package pl.btsoftware.backend.account.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.account.AccountModuleFacade;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Slf4j
public class AccountController {
    private final AccountModuleFacade accountModuleFacade;

    @GetMapping
    public AccountsView getAccounts() {
        log.info("Received request to get all accounts");
        return AccountsView.from(accountModuleFacade.getAccounts());
    }

    @GetMapping("/{id}")
    public AccountView getAccount(@PathVariable UUID id) {
        log.info("Received request to get account with id: {}", id);
        return AccountView.from(accountModuleFacade.getAccount(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountView createAccount(@Validated @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account with name: {} and currency: {}", request.name(), request.currency());
        return AccountView.from(accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(request.name(), request.currency())));
    }

    @PutMapping("/{id}")
    public AccountView updateAccount(@PathVariable UUID id, @Validated @RequestBody UpdateAccountRequest request) {
        log.info("Received request to update account with id: {} and new name: {}", id, request.name());
        return AccountView.from(accountModuleFacade.updateAccount(new AccountModuleFacade.UpdateAccountCommand(id, request.name())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable UUID id) {
        log.info("Received request to delete account with id: {}", id);
        accountModuleFacade.deleteAccount(id);
    }

}
