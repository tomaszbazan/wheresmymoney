package pl.btsoftware.backend.csvimport.application;

import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.shared.Currency;

import java.io.InputStream;

public interface TransactionCsvParser {
    CsvParseResult parse(InputStream csvStream, Currency accountCurrency);
}
