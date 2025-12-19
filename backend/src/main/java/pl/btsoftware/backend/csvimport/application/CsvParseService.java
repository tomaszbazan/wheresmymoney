package pl.btsoftware.backend.csvimport.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.users.UsersModuleFacade;

@Service
@RequiredArgsConstructor
public class CsvParseService {
    private final TransactionCsvParser parser;
    private final AccountModuleFacade accountFacade;
    private final UsersModuleFacade usersFacade;

    public CsvParseResult parse(ParseCsvCommand command) {
        var user = usersFacade.findUserOrThrow(command.userId());
        var account = accountFacade.getAccount(command.accountId(), user.groupId());

        return parser.parse(command.csvFile(), account.balance().currency());
    }
}
