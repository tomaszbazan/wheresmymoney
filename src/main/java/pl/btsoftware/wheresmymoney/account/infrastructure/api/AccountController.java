package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.domain.error.BusinessException;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountView createAccount(@RequestBody CreateAccountRequest request) {
        log.info("Received request to create account with name: {}", request.name());
        return AccountView.from(accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(request.name())));
    }

    @PutMapping("/{id}")
    public AccountView updateAccount(@PathVariable UUID id, @RequestBody UpdateAccountRequest request) {
        log.info("Received request to update account with id: {} and new name: {}", id, request.name());
        return AccountView.from(accountModuleFacade.updateAccount(new AccountModuleFacade.UpdateAccountCommand(id, request.name())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable UUID id) {
        log.info("Received request to delete account with id: {}", id);
        accountModuleFacade.deleteAccount(id);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        log.error("Business exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
