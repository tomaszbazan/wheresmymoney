package pl.btsoftware.backend.account.application;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.btsoftware.backend.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.account.domain.error.*;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static pl.btsoftware.backend.account.domain.Currency.*;

public class AccountServiceTest {
    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = new InMemoryAccountRepository();
        this.accountService = new AccountService(accountRepository);
    }

    @Nested
    class CreateAccount {

        private static CreateAccountCommand createAccountCommand(String name, Currency currency) {
            return new CreateAccountCommand(name, currency);

        }

        @ParameterizedTest
        @EnumSource(Currency.class)
        void shouldCreateAccountWithDifferentSupportedCurrencies(Currency currency) {
            // given
            var accountName = currency + " Account";
            var command = createAccountCommand(accountName, currency);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(accountName);
            assertThat(account.balance().currency()).isEqualTo(currency);
            assertThat(account.balance().value()).isZero();
        }

        @Test
        void shouldCreateAccountWithMinimalData() {
            // given
            var accountName = "Minimal Account";
            var command = new CreateAccountCommand(accountName, null); // null currency defaults to "PLN"

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(accountName);
            assertThat(account.balance().value()).isZero();
            assertThat(account.balance().currency()).isEqualTo(PLN); // default currency
            assertThat(account.id()).isNotNull();
        }

        @Test
        void shouldRejectAccountCreationWithEmptyName() {
            // given
            var command = createAccountCommand("", PLN);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithNullName() {
            // given
            var command = createAccountCommand(null, PLN);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithBlankName() {
            // given
            var command = createAccountCommand("   ", PLN);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithTooLongName() {
            // given
            var longName = "a".repeat(101);
            var command = createAccountCommand(longName, PLN);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameTooLongException.class);
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"Invalid\nName", "Invalid\tName"})
        void shouldRejectAccountCreationWithInvalidCharacters(String invalidName) {
            // given
            var command = createAccountCommand(invalidName, PLN);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameInvalidCharactersException.class);
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldCreateAccountWithValidSpecialCharacters() {
            // given
            var validName = "Valid Name-123 O'Connor's";
            var command = createAccountCommand(validName, PLN);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(validName);
            assertThat(accountRepository.findAll()).hasSize(1).containsOnly(account);
        }

        @Test
        void shouldRejectDuplicateAccountNamesAndCurrencies() {
            // given
            var accountName = "Duplicate Account";
            var currency = PLN;
            var command1 = createAccountCommand(accountName, currency);
            var command2 = createAccountCommand(accountName, currency);

            // when
            accountService.createAccount(command1);

            // then
            assertThatThrownBy(() -> accountService.createAccount(command2))
                    .isInstanceOf(AccountAlreadyExistsException.class)
                    .hasMessageContaining("Account with provided name and currency already exists");
            assertThat(accountRepository.findAll()).hasSize(1);
        }

        @Test
        void shouldAllowSameNameWithDifferentCurrencies() {
            // given
            var accountName = "Multi-Currency Account";
            var command1 = createAccountCommand(accountName, PLN);
            var command2 = createAccountCommand(accountName, EUR);

            // when
            var account1 = accountService.createAccount(command1);
            var account2 = accountService.createAccount(command2);

            // then
            assertThat(account1.name()).isEqualTo(accountName);
            assertThat(account1.balance().currency()).isEqualTo(PLN);
            assertThat(account2.name()).isEqualTo(accountName);
            assertThat(account2.balance().currency()).isEqualTo(EUR);
            assertThat(accountRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    class AccountRetrieval {

        @Test
        void shouldReturnAllActiveAccounts() {
            // given
            var account1 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Checking Account")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create());
            var account2 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Savings Account")
                    .set(field(CreateAccountCommand::currency), EUR)
                    .create());
            var account3 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Investment Account")
                    .set(field(CreateAccountCommand::currency), USD)
                    .create());

            // when
            var result = accountService.getAccounts();

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(account1, account2, account3);

            // verify each account has required fields
            result.forEach(account -> {
                assertThat(account.id()).isNotNull();
                assertThat(account.name()).isNotEmpty();
                assertThat(account.balance().currency()).isNotNull();
                assertThat(account.balance().value()).isNotNull();
                assertThat(account.createdAt()).isNotNull();
                assertThat(account.updatedAt()).isNotNull();
            });
        }

        @Test
        void shouldReturnEmptyListWhenNoAccountsExist() {
            // when
            var result = accountService.getAccounts();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnSpecificAccountById() {
            // given
            var accountName = "Test Account";
            var currency = EUR;
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), accountName)
                    .set(field(CreateAccountCommand::currency), currency)
                    .create());

            // when
            var result = accountService.getById(account.id().value());

            // then
            assertThat(result).isEqualTo(account);
            assertThat(result.id()).isEqualTo(account.id());
            assertThat(result.name()).isEqualTo(accountName);
            assertThat(result.balance().currency()).isEqualTo(currency);
            assertThat(result.balance().value()).isZero();
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            // given
            var nonExistentAccountId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> accountService.getById(nonExistentAccountId))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        void shouldReturnAccountsWithCompleteFieldStructure() {
            // given
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Complete Account")
                    .set(field(CreateAccountCommand::currency), GBP)
                    .create());

            // when
            var retrievedAccount = accountService.getById(account.id().value());

            // then
            assertThat(retrievedAccount.id()).isNotNull();
            assertThat(retrievedAccount.name()).isEqualTo("Complete Account");
            assertThat(retrievedAccount.balance().currency()).isEqualTo(GBP);
            assertThat(retrievedAccount.balance().value()).isZero();
            assertThat(retrievedAccount.createdAt()).isNotNull();
            assertThat(retrievedAccount.updatedAt()).isNotNull();
        }
    }

    @Nested
    class AccountUpdate {

        @Test
        void shouldUpdateAccountName() {
            // given
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Original Name")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create());
            var newName = "Updated Name";

            // when
            var updatedAccount = accountService.updateAccount(account.id().value(), newName);

            // then
            assertThat(updatedAccount.name()).isEqualTo(newName);
            var retrievedAccount = accountService.getById(account.id().value());
            assertThat(retrievedAccount.name()).isEqualTo(newName);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistentAccount() {
            // given
            var nonExistentAccountId = UUID.randomUUID();
            var newName = "Updated Name";

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(nonExistentAccountId, newName))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingAccountWithInvalidName() {
            // given
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Original Name")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create());
            var emptyName = "";

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(account.id().value(), emptyName))
                    .isInstanceOf(AccountNameEmptyException.class);

            // and
            var retrievedAccount = accountService.getById(account.id().value());
            assertThat(retrievedAccount.name()).isEqualTo("Original Name");
        }

        @Test
        void shouldThrowExceptionWhenUpdatingWithDuplicateName() {
            // given
            var account1 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Account One")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create());
            var account2 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Account Two")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create());

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(account2.id().value(), "Account One"))
                    .isInstanceOf(AccountAlreadyExistsException.class);

            // verify original names are preserved
            var retrievedAccount1 = accountService.getById(account1.id().value());
            var retrievedAccount2 = accountService.getById(account2.id().value());
            assertThat(retrievedAccount1.name()).isEqualTo("Account One");
            assertThat(retrievedAccount2.name()).isEqualTo("Account Two");
        }
    }

    @Nested
    class AccountDeletion {

        @Test
        void shouldDeleteAccountWithZeroBalance() {
            // given
            var account = accountService.createAccount(new CreateAccountCommand("Test Account", PLN));

            // when
            accountService.deleteAccount(account.id().value());

            // then
            assertThat(accountService.getAccounts()).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistentAccount() {
            // given
            var nonExistentAccountId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(nonExistentAccountId))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }
}