package pl.btsoftware.backend.csvimport.application;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import pl.btsoftware.backend.csvimport.domain.*;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static pl.btsoftware.backend.csvimport.domain.ErrorType.*;

@Component
public class MbankCsvParser implements TransactionCsvParser {
    private static final int HEADER_END_LINE = 27;
    private static final int DATE_COLUMN = 0;
    private static final int DESCRIPTION_COLUMN = 1;
    private static final int CATEGORY_COLUMN = 3;
    private static final int AMOUNT_COLUMN = 4;
    private static final int MINIMUM_LINE_COUNT = 28;
    private static final int COLUMN_HEADER_LINE_INDEX = 26;
    private static final String EXPECTED_COLUMN_HEADERS = "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;";

    public CsvParseResult parse(InputStream csvStream, Currency accountCurrency) {
        var csvBytes = readAllBytes(csvStream);
        validate(new ByteArrayInputStream(csvBytes));

        var proposals = new ArrayList<TransactionProposal>();
        var errors = new ArrayList<ParseError>();
        var totalRows = 0;

        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8); var parser = createCsvParser(reader)) {

            for (CSVRecord record : parser) {
                if (isDataRow(record)) {
                    totalRows++;
                    parseRow(record, proposals, errors, accountCurrency, totalRows);
                }
            }

            return new CsvParseResult(proposals, errors, totalRows, proposals.size(), errors.size());

        } catch (IOException e) {
            throw new CsvImportException(FAILED_TO_PARSE_CSV, e.getMessage());
        }
    }

    private CSVParser createCsvParser(InputStreamReader reader) throws IOException {
        var format = CSVFormat.Builder.create().setDelimiter(';').setQuote('"').setIgnoreEmptyLines(false).setTrim(false).build();

        var parser = format.parse(reader);

        if (!parser.iterator().hasNext()) {
            throw new CsvImportException(ErrorType.EMPTY_FILE, "CSV file is empty");
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
                errors.add(new ParseError(ErrorType.CURRENCY_MISMATCH, rowNumber, "Currency mismatch: CSV contains " + proposal.currency() + " but account uses " + accountCurrency));
                return;
            }

            proposals.add(proposal);
        } catch (CsvImportException e) {
            errors.add(new ParseError(e.getErrorType(), rowNumber, e.getMessage()));
        } catch (Exception e) {
            errors.add(new ParseError(ErrorType.UNKNOWN_ERROR, rowNumber, e.getMessage()));
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
            throw new CsvImportException(ErrorType.INVALID_DATE_FORMAT, "Invalid date format: " + dateString);
        }
    }

    private BigDecimal parseAmount(String amountString) {
        try {
            var cleanAmount = amountString.replaceAll("[A-Z]{3}$", "").replace(" ", "").replace(",", ".").trim();

            return new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            throw new CsvImportException(ErrorType.INVALID_AMOUNT_FORMAT, "Invalid amount format: " + amountString);
        }
    }

    private Currency extractCurrency(String amountString) {
        try {
            var currencyCode = amountString.trim().replaceAll("^[\\d\\s,.-]+", "").trim();

            return Currency.valueOf(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new CsvImportException(ErrorType.INVALID_CURRENCY, "Unsupported currency in amount: " + amountString);
        }
    }

    private TransactionType determineType(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    private byte[] readAllBytes(InputStream stream) {
        try {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new CsvImportException(FAILED_TO_PARSE_CSV, e.getMessage());
        }
    }

    private void validate(InputStream csvStream) {
        var lines = readLines(csvStream);

        validateNotEmpty(lines);
        validateMinimumLineCount(lines);
        validateColumnHeaders(lines);
    }

    private ArrayList<String> readLines(InputStream csvStream) {
        var lines = new ArrayList<String>();
        try (var reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && lines.size() < MINIMUM_LINE_COUNT) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new CsvImportException(FAILED_TO_PARSE_CSV, e.getMessage());
        }
        return lines;
    }

    private void validateNotEmpty(ArrayList<String> lines) {
        if (lines.isEmpty()) {
            throw new CsvImportException(EMPTY_FILE, "CSV file is empty");
        }
    }

    private void validateMinimumLineCount(ArrayList<String> lines) {
        if (lines.size() < MINIMUM_LINE_COUNT) {
            throw new CsvImportException(INVALID_FILE, "CSV file must have at least 28 lines (mBank format header + column headers)");
        }
    }

    private void validateColumnHeaders(ArrayList<String> lines) {
        var columnHeaderLine = lines.get(COLUMN_HEADER_LINE_INDEX);

        if (!columnHeaderLine.startsWith(EXPECTED_COLUMN_HEADERS)) {
            throw new CsvImportException(INVALID_FILE, "Expected mBank column headers at line 27: " + EXPECTED_COLUMN_HEADERS);
        }
    }
}
