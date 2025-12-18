package pl.btsoftware.backend.csvimport.infrastructure.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.config.WebConfig;
import pl.btsoftware.backend.csvimport.application.CsvParseService;
import pl.btsoftware.backend.csvimport.domain.CsvParseResult;
import pl.btsoftware.backend.csvimport.domain.CsvValidationException;
import pl.btsoftware.backend.csvimport.domain.ParseError;
import pl.btsoftware.backend.csvimport.domain.TransactionProposal;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.btsoftware.backend.shared.JwtTokenFixture.createTokenFor;

@WebMvcTest(controllers = TransactionsImportController.class)
@Import(WebConfig.class)
public class TransactionsImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvParseService csvParseService;

    @Test
    void shouldParseCsvAndReturnProposals() throws Exception {
        var proposal1 = new TransactionProposal(
                LocalDate.of(2025, 12, 17),
                "Wpływy - inne / FRANCISZEK BELA",
                new BigDecimal("1100.00"),
                Currency.PLN,
                TransactionType.INCOME,
                null
        );

        var proposal2 = new TransactionProposal(
                LocalDate.of(2025, 12, 17),
                "Zdrowie i uroda / APTEKARIUS SPOLKA",
                new BigDecimal("-239.22"),
                Currency.PLN,
                TransactionType.EXPENSE,
                null
        );

        var parseResult = new CsvParseResult(
                List.of(proposal1, proposal2),
                List.of(),
                2,
                2,
                0
        );

        when(csvParseService.parse(any())).thenReturn(parseResult);

        var csvFile = new MockMultipartFile(
                "csvFile",
                "test.csv",
                "text/csv",
                "csv content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/transactions/import")
                        .file(csvFile)
                        .param("accountId", "550e8400-e29b-41d4-a716-446655440000")
                        .with(createTokenFor("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposals", hasSize(2)))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.errorCount").value(0))
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.proposals[0].categoryId").doesNotExist())
                .andExpect(jsonPath("$.proposals[0].description").value("Wpływy - inne / FRANCISZEK BELA"))
                .andExpect(jsonPath("$.proposals[0].currency").value("PLN"))
                .andExpect(jsonPath("$.proposals[1].categoryId").doesNotExist())
                .andExpect(jsonPath("$.proposals[1].description").value("Zdrowie i uroda / APTEKARIUS SPOLKA"))
                .andExpect(jsonPath("$.proposals[1].currency").value("PLN"));
    }

    @Test
    void shouldHandleInvalidCsv() throws Exception {
        var parseError = new ParseError(1, "Invalid date format");
        var parseResult = new CsvParseResult(
                List.of(),
                List.of(parseError),
                1,
                0,
                1
        );

        when(csvParseService.parse(any())).thenReturn(parseResult);

        var csvFile = new MockMultipartFile(
                "csvFile",
                "invalid.csv",
                "text/csv",
                "invalid,csv,data\n1,2,3".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/transactions/import")
                        .file(csvFile)
                        .param("accountId", "550e8400-e29b-41d4-a716-446655440000")
                        .with(createTokenFor("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposals", hasSize(0)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].lineNumber").value(1))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid date format"))
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.errorCount").value(1));
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {
        var emptyFile = new MockMultipartFile(
                "csvFile",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/transactions/import")
                        .file(emptyFile)
                        .param("accountId", "550e8400-e29b-41d4-a716-446655440000")
                        .with(createTokenFor("test-user")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForInvalidFileFormat() throws Exception {
        when(csvParseService.parse(any())).thenThrow(new CsvValidationException("CSV file must have at least 28 lines (mBank format header + column headers)"));

        var invalidFile = new MockMultipartFile(
                "csvFile",
                "invalid.csv",
                "text/csv",
                "invalid content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/transactions/import")
                        .file(invalidFile)
                        .param("accountId", "550e8400-e29b-41d4-a716-446655440000")
                        .with(createTokenFor("test-user")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("at least 28 lines")));
    }

    @Test
    void shouldReturnValidationErrorInResponse() throws Exception {
        when(csvParseService.parse(any())).thenThrow(new CsvValidationException("Expected mBank column headers at line 27"));

        var invalidFile = new MockMultipartFile(
                "csvFile",
                "wrong_headers.csv",
                "text/csv",
                "wrong headers content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/transactions/import")
                        .file(invalidFile)
                        .param("accountId", "550e8400-e29b-41d4-a716-446655440000")
                        .with(createTokenFor("test-user")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Expected mBank column headers")));
    }
}
