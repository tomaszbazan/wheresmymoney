package pl.btsoftware.backend.users.infrastructure.api;

import static org.springframework.http.HttpStatus.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;
import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;

@ControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        log.error("User business exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<String> handleInvitationNotFoundException(InvitationNotFoundException ex) {
        log.error("Invitation not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvitationTokenExpiredException.class)
    public ResponseEntity<String> handleInvitationTokenExpiredException(InvitationTokenExpiredException ex) {
        log.error("Invitation token expired: {}", ex.getMessage(), ex);
        return ResponseEntity.status(GONE).body(ex.getMessage());
    }
}
