package pl.btsoftware.backend.account.infrastructure.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.category.domain.error.NoCategoriesAvailableException;
import pl.btsoftware.backend.csvimport.domain.CsvImportException;
import pl.btsoftware.backend.csvimport.infrastructure.api.ErrorResponse;
import pl.btsoftware.backend.shared.error.InvalidExchangeRateException;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionTooLongException;
import pl.btsoftware.backend.transfer.domain.error.TransferNotFoundException;
import pl.btsoftware.backend.transfer.domain.error.TransferToSameAccountException;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<String> handleAccountAlreadyExistsException(AccountAlreadyExistsException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<String> handleInvalidFormatException(InvalidFormatException ex) {
        return new ResponseEntity<>(ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        var message = "Invalid category type: " + ex.getValue();
        log.error(message, ex);
        return new ResponseEntity<>(message, BAD_REQUEST);
    }

    @ExceptionHandler(NoCategoriesAvailableException.class)
    public ResponseEntity<String> handleNoCategoriesAvailableException(NoCategoriesAvailableException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(PRECONDITION_FAILED).body(ex.getMessage());
    }

    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<ErrorResponse> handleCsvParsingException(CsvImportException ex) {
        log.error("{}", ex.getMessage(), ex);
        var errorResponse = new ErrorResponse(ex.getErrorType(), ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({
            TransferToSameAccountException.class,
            InvalidExchangeRateException.class,
            TransferDescriptionTooLongException.class
    })
    public ResponseEntity<String> handleTransferBadRequest(RuntimeException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<String> handleTransferNotFoundException(TransferNotFoundException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }
}
