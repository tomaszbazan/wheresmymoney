package pl.btsoftware.backend.account.infrastructure.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.account.domain.error.ExpenseNotFoundException;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({AccountNotFoundException.class, ExpenseNotFoundException.class})
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<String> handleAccountAlreadyExistsException(AccountAlreadyExistsException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(CONFLICT).body(ex.getMessage());
    }
}