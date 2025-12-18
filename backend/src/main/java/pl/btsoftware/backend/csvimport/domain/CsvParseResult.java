package pl.btsoftware.backend.csvimport.domain;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record CsvParseResult(List<TransactionProposal> proposals, List<ParseError> errors, int totalRows,
                             int successCount, int errorCount) {
    public CsvParseResult {
        requireNonNull(proposals, "Proposals list cannot be null");
        requireNonNull(errors, "Errors list cannot be null");
        proposals = List.copyOf(proposals);
        errors = List.copyOf(errors);
    }

    public double successRate() {
        if (totalRows == 0) {
            return 0.0;
        }
        return (double) successCount / totalRows;
    }
}
