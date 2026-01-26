package pl.btsoftware.backend.csvimport.application;

import java.io.InputStream;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.shared.Currency;

public interface TransactionCsvParser {
    CsvParseResult parse(InputStream csvStream, Currency accountCurrency);
}
