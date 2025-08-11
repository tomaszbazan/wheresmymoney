package pl.btsoftware.backend.users.infrastructure.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.btsoftware.backend.users.domain.error.*;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserBusinessException.class)
    public ResponseEntity<String> handleUserBusinessException(UserBusinessException ex) {
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