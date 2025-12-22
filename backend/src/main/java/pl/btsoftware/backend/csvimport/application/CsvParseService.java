package pl.btsoftware.backend.csvimport.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.csvimport.domain.CategorySuggestion;
import pl.btsoftware.backend.csvimport.domain.CategorySuggestionService;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.csvimport.domain.TransactionProposal;
import pl.btsoftware.backend.users.UsersModuleFacade;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvParseService {
    private final TransactionCsvParser parser;
    private final AccountModuleFacade accountFacade;
    private final UsersModuleFacade usersFacade;
    private final CategorySuggestionService categorySuggestionService;

    public CsvParseResult parse(ParseCsvCommand command) {
        var user = usersFacade.findUserOrThrow(command.userId());
        var account = accountFacade.getAccount(command.accountId(), user.groupId());
        var parseResult = parser.parse(command.csvFile(), account.balance().currency());

        var categorizedProposals = applyCategorySuggestions(parseResult.proposals(), user.groupId());

        return new CsvParseResult(
                categorizedProposals,
                parseResult.errors(),
                parseResult.totalRows(),
                parseResult.successCount(),
                parseResult.errorCount()
        );
    }

    private List<TransactionProposal> applyCategorySuggestions(List<TransactionProposal> proposals, pl.btsoftware.backend.users.domain.GroupId groupId) {
        if (proposals.isEmpty()) {
            return proposals;
        }

        var suggestions = categorySuggestionService.suggestCategories(proposals, groupId);

        if (suggestions == null) {
            log.warn("AI categorization failed, continuing with null categories");
            return proposals;
        }

        if (suggestions.isEmpty()) {
            return proposals;
        }

        return applySuggestions(proposals, suggestions);
    }

    private List<TransactionProposal> applySuggestions(List<TransactionProposal> proposals, List<CategorySuggestion> suggestions) {
        var suggestionMap = new HashMap<Integer, CategorySuggestion>();
        for (int i = 0; i < suggestions.size(); i++) {
            suggestionMap.put(i, suggestions.get(i));
        }

        return proposals.stream()
                .map(proposal -> {
                    var index = proposals.indexOf(proposal);
                    var suggestion = suggestionMap.get(index);
                    if (suggestion != null) {
                        return new TransactionProposal(
                                proposal.transactionDate(),
                                proposal.description(),
                                proposal.amount(),
                                proposal.currency(),
                                proposal.type(),
                                suggestion.categoryId()
                        );
                    }
                    return proposal;
                })
                .toList();
    }
}
