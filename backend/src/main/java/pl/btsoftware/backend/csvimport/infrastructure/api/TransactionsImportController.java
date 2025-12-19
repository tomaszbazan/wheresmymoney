package pl.btsoftware.backend.csvimport.infrastructure.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.btsoftware.backend.csvimport.application.CsvParseService;
import pl.btsoftware.backend.csvimport.application.ParseCsvCommand;
import pl.btsoftware.backend.csvimport.domain.CsvImportException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.users.domain.UserId;

import java.io.IOException;
import java.util.UUID;

import static pl.btsoftware.backend.csvimport.domain.ErrorType.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionsImportController {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String[] ALLOWED_CONTENT_TYPES = {"text/csv", "application/csv", "text/plain"};

    private final CsvParseService csvParseService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvParseResultView importTransactions(@RequestParam("csvFile") MultipartFile file, @RequestParam("accountId") UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received CSV parse request for account: {} by user: {}", accountId, userId);

        validateFile(file);

        try {
            var command = new ParseCsvCommand(file.getInputStream(), userId, AccountId.from(accountId));
            var result = csvParseService.parse(command);

            log.info("CSV parsing completed: {} proposals, {} errors", result.successCount(), result.errorCount());
            return CsvParseResultView.from(result);
        } catch (IOException e) {
            log.error("Failed to read CSV file", e);
            throw new CsvImportException(FAILED_TO_PARSE_CSV, e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvImportException(EMPTY_FILE, "File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CsvImportException(FILE_TOO_LARGE, "File size exceeds maximum allowed size of 10MB");
        }

        var contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new CsvImportException(INVALID_FILE_TYPE, "Invalid file type. Allowed types: CSV");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (contentType.equals(allowed)) {
                return true;
            }
        }
        return false;
    }
}
