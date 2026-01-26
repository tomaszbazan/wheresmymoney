package pl.btsoftware.backend.csvimport.infrastructure.api;

import java.util.List;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;

public record CsvParseResultView(
        List<TransactionProposalView> proposals,
        List<ParseErrorView> errors,
        int totalRows,
        int successCount,
        int errorCount) {
    public CsvParseResultView {
        proposals = List.copyOf(proposals);
        errors = List.copyOf(errors);
    }

    public static CsvParseResultView from(CsvParseResult result) {
        return new CsvParseResultView(
                result.proposals().stream().map(TransactionProposalView::from).toList(),
                result.errors().stream().map(ParseErrorView::from).toList(),
                result.totalRows(),
                result.successCount(),
                result.errorCount());
    }
}
