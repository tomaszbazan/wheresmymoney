package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
public class AccountController {
    private final AccountModuleFacade accountModuleFacade;

    @GetMapping
    public AccountsView getAccounts() {
        return AccountsView.from(accountModuleFacade.getAccounts());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody CreateAccountRequest request) {
        accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(request.name()));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
