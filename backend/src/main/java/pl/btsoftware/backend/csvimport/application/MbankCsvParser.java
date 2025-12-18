package pl.btsoftware.backend.csvimport.application;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.csvimport.domain.CsvParsingException;
import pl.btsoftware.backend.csvimport.domain.ParseError;
import pl.btsoftware.backend.csvimport.domain.TransactionProposal;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MbankCsvParser implements TransactionCsvParser {
    private static final int HEADER_END_LINE = 27;
    private static final int DATE_COLUMN = 0;
    private static final int DESCRIPTION_COLUMN = 1;
    private static final int CATEGORY_COLUMN = 3;
    private static final int AMOUNT_COLUMN = 4;

    public CsvParseResult parse(InputStream csvStream) {
        return parse(csvStream, null);
    }

    public CsvParseResult parse(InputStream csvStream, Currency accountCurrency) {
        var proposals = new ArrayList<TransactionProposal>();
        var errors = new ArrayList<ParseError>();
        var totalRows = 0;

        try (var reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8); var parser = createCsvParser(reader)) {

            for (CSVRecord record : parser) {
                if (isDataRow(record)) {
                    totalRows++;
                    parseRow(record, proposals, errors, accountCurrency, totalRows);
                }
            }

            return new CsvParseResult(proposals, errors, totalRows, proposals.size(), errors.size());

        } catch (IOException e) {
            throw new CsvParsingException("Failed to parse CSV file", e);
        }
    }

    private CSVParser createCsvParser(InputStreamReader reader) throws IOException {
        var format = CSVFormat.Builder.create().setDelimiter(';').setQuote('"').setIgnoreEmptyLines(false).setTrim(false).build();

        var parser = format.parse(reader);

        if (!parser.iterator().hasNext()) {
            throw new CsvParsingException("CSV file is empty");
        }

        return parser;
    }

    private boolean isDataRow(CSVRecord record) {
        return record.getRecordNumber() > HEADER_END_LINE && !isEmptyRow(record);
    }

    private boolean isEmptyRow(CSVRecord record) {
        return record.size() < 5 || record.stream().allMatch(String::isBlank);
    }

    private void parseRow(CSVRecord record, List<TransactionProposal> proposals, List<ParseError> errors, Currency accountCurrency, int rowNumber) {
        try {
            var proposal = createProposal(record);

            if (accountCurrency != null && !proposal.currency().equals(accountCurrency)) {
                errors.add(new ParseError(rowNumber, "Currency mismatch: CSV contains " + proposal.currency() + " but account uses " + accountCurrency));
                return;
            }

            proposals.add(proposal);
        } catch (Exception e) {
            errors.add(new ParseError(rowNumber, e.getMessage()));
        }
    }

    private TransactionProposal createProposal(CSVRecord record) {
        var date = parseDate(record.get(DATE_COLUMN));
        var rawDescription = record.get(DESCRIPTION_COLUMN).trim();
        var category = record.get(CATEGORY_COLUMN).trim();
        var amountString = record.get(AMOUNT_COLUMN).trim();

        var descriptionWithCategory = category + " / " + rawDescription;
        var description = truncateDescription(descriptionWithCategory);

        var currency = extractCurrency(amountString);
        var amount = parseAmount(amountString);
        var type = determineType(amount);

        return new TransactionProposal(date, description, amount, currency, type, null);
    }

    private String truncateDescription(String description) {
        if (description.length() > TransactionProposal.MAX_DESCRIPTION_LENGTH) {
            return description.substring(0, TransactionProposal.MAX_DESCRIPTION_LENGTH);
        }
        return description;
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }

    private BigDecimal parseAmount(String amountString) {
        try {
            var cleanAmount = amountString.replaceAll("[A-Z]{3}$", "").replace(" ", "").replace(",", ".").trim();

            return new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amountString);
        }
    }

    private Currency extractCurrency(String amountString) {
        try {
            var currencyCode = amountString.trim().replaceAll("^[\\d\\s,.-]+", "").trim();

            return Currency.valueOf(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency in amount: " + amountString);
        }
    }

    private TransactionType determineType(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.INCOME : TransactionType.EXPENSE;
    }
}
