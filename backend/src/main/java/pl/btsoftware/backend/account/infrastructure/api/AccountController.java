package pl.btsoftware.backend.account.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.error.*;

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
    public AccountView createAccount(@RequestBody CreateAccountRequest request) {
        log.info("Received request to create account with name: {} and currency: {}", request.name(), request.currency());
        return AccountView.from(accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(request.name(), request.currency())));
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

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("Account not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

//    @ExceptionHandler({AccountNameEmptyException.class, AccountNameTooLongException.class, AccountNameInvalidCharactersException.class, InvalidCurrencyException.class})
//    public ResponseEntity<String> handleValidationExceptions(BusinessException ex) {
//        log.error("Validation error: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<String> handleAccountNameAlreadyExistsException(AccountAlreadyExistsException ex) {
        log.error("Account name already exists: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
