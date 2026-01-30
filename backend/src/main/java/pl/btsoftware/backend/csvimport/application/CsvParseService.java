package pl.btsoftware.backend.csvimport.application;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.csvimport.domain.*;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;

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
                parseResult.errorCount());
    }

    private List<TransactionProposal> applyCategorySuggestions(List<TransactionProposal> proposals, GroupId groupId) {
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

    private List<TransactionProposal> applySuggestions(
            List<TransactionProposal> proposals, List<CategorySuggestion> suggestions) {
        var suggestionMap = new HashMap<TransactionProposalId, CategorySuggestion>();
        for (var suggestion : suggestions) {
            suggestionMap.put(suggestion.transactionProposalId(), suggestion);
        }

        return proposals.stream()
                .map(proposal -> {
                    var suggestion = suggestionMap.get(proposal.transactionId());
                    if (suggestion != null) {
                        return new TransactionProposal(
                                proposal.transactionId(),
                                proposal.transactionDate(),
                                proposal.description(),
                                proposal.amount(),
                                proposal.currency(),
                                proposal.type(),
                                suggestion.categoryId());
                    }
                    return proposal;
                })
                .toList();
    }
}
