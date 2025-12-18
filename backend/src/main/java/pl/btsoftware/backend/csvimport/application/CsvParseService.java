package pl.btsoftware.backend.csvimport.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.csvimport.domain.CsvValidationException;
import pl.btsoftware.backend.users.UsersModuleFacade;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CsvParseService {
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final TransactionCsvParser parser;
    private final AccountModuleFacade accountFacade;
    private final UsersModuleFacade usersFacade;

    public CsvParseResult parse(ParseCsvCommand command) {
        var user = usersFacade.findUserOrThrow(command.userId());
        var account = accountFacade.getAccount(command.accountId(), user.groupId());

        var file = validateAndReadCsv(command);

        return parser.parse(file, account.balance().currency());
    }

    private ByteArrayInputStream validateAndReadCsv(ParseCsvCommand command) {
        try {
            var bytes = command.csvFile().readAllBytes();
            if (bytes.length > MAX_FILE_SIZE) {
                throw new CsvValidationException("File size exceeds maximum allowed size of 10MB");
            }
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new CsvValidationException("Failed to read CSV file: " + e.getMessage());
        }
    }
}
