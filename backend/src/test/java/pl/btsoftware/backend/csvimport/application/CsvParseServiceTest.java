package pl.btsoftware.backend.csvimport.application;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.csvimport.domain.CategorySuggestion;
import pl.btsoftware.backend.csvimport.domain.CategorySuggestionService;
import pl.btsoftware.backend.csvimport.domain.CsvImportException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static pl.btsoftware.backend.csvimport.domain.ErrorType.*;
import static pl.btsoftware.backend.csvimport.domain.TransactionProposalId.generate;

class CsvParseServiceTest {

    private CsvParseService service;
    private UserId userId;
    private AccountId accountId;
    private GroupId groupId;
    private CategorySuggestionService categorySuggestionService;

    @BeforeEach
    void setUp() {
        var accountFacade = Mockito.mock(AccountModuleFacade.class);
        var usersFacade = Mockito.mock(UsersModuleFacade.class);
        categorySuggestionService = Mockito.mock(CategorySuggestionService.class);
        groupId = GroupId.generate();

        userId = UserId.generate();
        accountId = AccountId.generate();

        var user = Instancio.of(User.class).set(field(User::id), userId).set(field(User::groupId), groupId).create();

        var account = Instancio.of(Account.class).set(field(Account::id), accountId).set(field(Account::balance), Money.of(BigDecimal.ZERO, Currency.PLN)).create();

        when(usersFacade.findUserOrThrow(userId)).thenReturn(user);
        when(accountFacade.getAccount(accountId, groupId)).thenReturn(account);

        var parser = new MbankCsvParser();
        service = new CsvParseService(parser, accountFacade, usersFacade, categorySuggestionService);
    }

    @Test
    void shouldParseValidCsv() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                2025-12-18;"Expense description";"mKonto";"Another Category";-100,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.errorCount()).isZero();

        var firstRow = result.proposals().getFirst();
        assertThat(firstRow.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 17));
        assertThat(firstRow.description()).isEqualTo("Category / Income description");
        assertThat(firstRow.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(firstRow.currency()).isEqualTo(Currency.PLN);
        assertThat(firstRow.type()).isEqualTo(TransactionType.INCOME);
        assertThat(firstRow.categoryId()).isNull();

        var secondRow = result.proposals().getLast();
        assertThat(secondRow.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 18));
        assertThat(secondRow.description()).isEqualTo("Another Category / Expense description");
        assertThat(secondRow.amount()).isEqualTo(new BigDecimal("-100.00"));
        assertThat(secondRow.currency()).isEqualTo(Currency.PLN);
        assertThat(secondRow.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(secondRow.categoryId()).isNull();
    }

    @Test
    void shouldHandlePartialParse() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                invalid-date;"Invalid";"mKonto";"Another Category";-100,00 PLN;;
                """);

        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.errors()).hasSize(1);
        var error = result.errors().getFirst();
        assertThat(error.type()).isEqualTo(INVALID_DATE_FORMAT);
        assertThat(error.details()).isEqualTo("Invalid date format: invalid-date");
        assertThat(error.lineNumber()).isEqualTo(2);
    }

    @Test
    void shouldRejectWhenCurrencyMismatch() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                2025-12-18;"Expense description";"mKonto";"Another Category";-100,00 EUR;;
                """);

        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.errors()).hasSize(1);
        var error = result.errors().getFirst();
        assertThat(error.type()).isEqualTo(CURRENCY_MISMATCH);
        assertThat(error.details()).isEqualTo("Currency mismatch: CSV contains EUR but account uses PLN");
        assertThat(error.lineNumber()).isEqualTo(2);
    }

    @Test
    void shouldParsePolishNumberFormat() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test\";\"mKonto\";\"Category\";1 234,56 PLN;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().amount()).isEqualTo(new BigDecimal("1234.56"));
    }

    @Test
    void shouldParseNegativeAmount() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test\";\"mKonto\";\"Category\";-123,45 PLN;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().amount()).isEqualTo(new BigDecimal("-123.45"));
        assertThat(result.proposals().getFirst().type()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void shouldParsePositiveAmount() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test\";\"mKonto\";\"Category\";100,00 PLN;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.proposals().getFirst().type()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    void shouldHandleInvalidDateFormat() {
        // given
        var csv = createMbankTransactionListCsv("invalid-date;\"Test\";\"mKonto\";\"Category\";100,00 PLN;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        var error = result.errors().getFirst();
        assertThat(error.type()).isEqualTo(INVALID_DATE_FORMAT);
        assertThat(error.details()).containsIgnoringCase("date");
    }

    @Test
    void shouldHandleInvalidAmountFormat() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test\";\"mKonto\";\"Category\";invalid;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        var error = result.errors().getFirst();
        assertThat(error.type()).isEqualTo(INVALID_CURRENCY);
        assertThat(error.details()).isEqualTo("Unsupported currency in amount: invalid");
        assertThat(error.lineNumber()).isEqualTo(1);
    }

    @Test
    void shouldHandleMissingRequiredField() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test\";\"mKonto\";;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        var error = result.errors().getFirst();
        assertThat(error.type()).isEqualTo(INVALID_CURRENCY);
        assertThat(error.details()).isEqualTo("Unsupported currency in amount: ");
        assertThat(error.lineNumber()).isEqualTo(1);
    }

    @Test
    void shouldSkipEmptyRows() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Test";"mKonto";"Category";100,00 PLN;;
                ;;;;;
                2025-12-18;"Test2";"mKonto";"Category2";50,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldHandleEmptyFile() {
        // given
        var csv = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when & then
        assertThatThrownBy(() -> service.parse(command)).isInstanceOf(CsvImportException.class);
    }

    @Test
    void shouldRejectHeaderOnlyFile() {
        // given
        var csv = createMbankTransactionListCsv("");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when & then
        assertThatThrownBy(() -> service.parse(command)).isInstanceOf(CsvImportException.class).hasMessageContaining("at least 28 lines");
    }

    @Test
    void shouldHandleQuotedDescriptionWithSemicolon() {
        // given
        var csv = createMbankTransactionListCsv("2025-12-17;\"Test; with; semicolons\";\"mKonto\";\"Category\";100,00 PLN;;");
        var command = new ParseCsvCommand(csv, userId, accountId);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().description()).isEqualTo("Category / Test; with; semicolons");
    }

    @Test
    void shouldRejectInvalidFileFormat() {
        // given
        var invalidCsv = new ByteArrayInputStream("Invalid CSV content".getBytes(StandardCharsets.UTF_8));
        var command = new ParseCsvCommand(invalidCsv, userId, accountId);

        // when & then
        assertThatThrownBy(() -> service.parse(command)).isInstanceOf(CsvImportException.class).hasMessageContaining("at least 28 lines");
    }

    @Test
    void shouldProvideValidationErrorMessage() {
        // given
        var invalidCsv = getClass().getClassLoader().getResourceAsStream("invalid_headers.csv");
        var command = new ParseCsvCommand(invalidCsv, userId, accountId);

        // when & then
        assertThatThrownBy(() -> service.parse(command)).isInstanceOf(CsvImportException.class).hasMessageContaining("Expected mBank column headers");
    }

    @Test
    void shouldValidateBeforeParsing() {
        // given
        var tooShortCsv = getClass().getClassLoader().getResourceAsStream("too_short.csv");
        var command = new ParseCsvCommand(tooShortCsv, userId, accountId);

        // when & then
        assertThatThrownBy(() -> service.parse(command)).isInstanceOf(CsvImportException.class).hasMessageContaining("at least 28 lines");
    }

    @Test
    @Disabled("This test needs to be rewritten as system test")
    void shouldApplyCategorySuggestionsWhenAvailable() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                2025-12-18;"Expense description";"mKonto";"Another Category";-100,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();
        var suggestions = List.of(new CategorySuggestion(generate(), categoryId1, 0.95), new CategorySuggestion(generate(), categoryId2, 0.90));

        when(categorySuggestionService.suggestCategories(any(), eq(groupId))).thenReturn(suggestions);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.proposals().get(0).categoryId()).isEqualTo(categoryId1);
        assertThat(result.proposals().get(1).categoryId()).isEqualTo(categoryId2);
    }

    @Test
    void shouldContinueWithoutCategoriesWhenAiFails() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                2025-12-18;"Expense description";"mKonto";"Another Category";-100,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        when(categorySuggestionService.suggestCategories(any(), eq(groupId))).thenReturn(null);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.proposals().get(0).categoryId()).isNull();
        assertThat(result.proposals().get(1).categoryId()).isNull();
    }

    @Test
    void shouldContinueWithoutCategoriesWhenNoCategoriesExist() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        when(categorySuggestionService.suggestCategories(any(), eq(groupId))).thenReturn(List.of());

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().categoryId()).isNull();
    }

    @Test
    @Disabled("This test needs to be rewritten as system test")
    void shouldHandleMixedIncomeAndExpenseTransactions() {
        // given
        var csv = createMbankTransactionListCsv("""
                2025-12-17;"Income description";"mKonto";"Category";100,00 PLN;;
                2025-12-18;"Expense description";"mKonto";"Another Category";-50,00 PLN;;
                """);
        var command = new ParseCsvCommand(csv, userId, accountId);

        var incomeCategoryId = CategoryId.generate();
        var expenseCategoryId = CategoryId.generate();
        var suggestions = List.of(new CategorySuggestion(generate(), incomeCategoryId, 0.95), new CategorySuggestion(generate(), expenseCategoryId, 0.90));

        when(categorySuggestionService.suggestCategories(any(), eq(groupId))).thenReturn(suggestions);

        // when
        var result = service.parse(command);

        // then
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.proposals().get(0).type()).isEqualTo(TransactionType.INCOME);
        assertThat(result.proposals().get(0).categoryId()).isEqualTo(incomeCategoryId);
        assertThat(result.proposals().get(1).type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.proposals().get(1).categoryId()).isEqualTo(expenseCategoryId);
    }

    private InputStream createMbankTransactionListCsv(String dataRows) {
        var header = """
                ﻿mBank S.A. Bankowość Detaliczna;
                		Skrytka Pocztowa 2108;
                		90-959 Łódź 2;
                		www.mBank.pl;
                		mLinia: 801 300 800;
                		+48 (42) 6 300 800;
                
                
                #Klient;
                JAN MARIAN KOWALSKI;
                
                Lista operacji;
                
                #Za okres:;
                17.12.2024;17.12.2025;
                
                #zgodnie z wybranymi filtrami wyszukiwania;
                      #dla rachunków:;
                      mKonto Intensive - 12345678901234567890123456;
                      rachunek kredytowy PLN - 12345678901234567890123456;
                
                      #Lista nie jest dokumentem w rozumieniu art. 7 Ustawy Prawo Bankowe (Dz. U. Nr 140 z 1997 roku, poz.939 z późniejszymi zmianami), ponieważ operacje można samodzielnie edytować.;
                
                      #Waluta;#Wpływy;#Wydatki;
                PLN;891 199,60;-897 548,47;
                
                #Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;
                """;

        var csv = header + dataRows;
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
