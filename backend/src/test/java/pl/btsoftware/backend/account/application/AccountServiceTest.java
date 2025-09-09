package pl.btsoftware.backend.account.application;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.error.*;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;
import static pl.btsoftware.backend.shared.Currency.*;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;

public class AccountServiceTest {
    private AccountRepository accountRepository;
    private UsersModuleFacade usersModuleFacade;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = new InMemoryAccountRepository();
        this.usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        this.accountService = new AccountService(accountRepository, usersModuleFacade);
    }

    private void userExistsInGroup(UserId userAId, GroupId groupId) {
        var user = Instancio.of(User.class).set(field(User::id), userAId).set(field(User::groupId), groupId).create();
        when(usersModuleFacade.findUserOrThrow(userAId)).thenReturn(user);
    }

    private User userExists(CreateAccountCommand command) {
        var user = Instancio.of(User.class).set(field(User::id), command.userId()).create();
        when(usersModuleFacade.findUserOrThrow(command.userId())).thenReturn(user);
        return user;
    }

    @Nested
    class CreateAccount {
        @ParameterizedTest
        @EnumSource(Currency.class)
        void shouldCreateAccountWithDifferentSupportedCurrencies(Currency currency) {
            // given
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::currency), currency).create();
            userExists(command);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(command.name());
            assertThat(account.balance().currency()).isEqualTo(currency);
            assertThat(account.balance().value()).isZero();
            assertThat(account.id()).isNotNull();
        }

        @Test
        void shouldCreateAccountWithoutCurrency() {
            // given
            var command = Instancio.of(CreateAccountCommand.class).setBlank(field(CreateAccountCommand::currency)).create();
            userExists(command);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(command.name());
            assertThat(account.balance().value()).isZero();
            assertThat(account.balance().currency()).isEqualTo(PLN); // default currency
            assertThat(account.id()).isNotNull();
        }

        @Test
        void shouldRejectAccountCreationWithEmptyName() {
            // given
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::name), "").create();
            var user = userExists(command);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAllBy(user.groupId())).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithNullName() {
            // given
            var command = Instancio.of(CreateAccountCommand.class).setBlank(field(CreateAccountCommand::name)).create();
            var user = userExists(command);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAllBy(user.groupId())).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithBlankName() {
            // given
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::name), "    ").create();
            var user = userExists(command);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameEmptyException.class);
            assertThat(accountRepository.findAllBy(user.groupId())).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithTooLongName() {
            // given
            var longName = "a".repeat(101);
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::name), longName).create();
            var user = userExists(command);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameTooLongException.class);
            assertThat(accountRepository.findAllBy(user.groupId())).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"Invalid\nName", "Invalid\tName"})
        void shouldRejectAccountCreationWithInvalidCharacters(String invalidName) {
            // given
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::name), invalidName).create();
            var user = userExists(command);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountNameInvalidCharactersException.class);
            assertThat(accountRepository.findAllBy(user.groupId())).isEmpty();
        }

        @Test
        void shouldCreateAccountWithValidSpecialCharacters() {
            // given
            var validName = "Valid Name-123 O'Connor's";
            var command = Instancio.of(CreateAccountCommand.class).set(field(CreateAccountCommand::name), validName).create();
            var user = userExists(command);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(validName);
            assertThat(accountRepository.findAllBy(user.groupId())).hasSize(1).containsOnly(account);
        }

        @Test
        void shouldRejectDuplicateAccountNamesAndCurrencies() {
            // given
            var accountName = "Duplicate Account";
            var command = Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), accountName)
                    .set(field(CreateAccountCommand::currency), PLN).create();
            var user = userExists(command);

            // when
            accountService.createAccount(command);

            // then
            assertThatThrownBy(() -> accountService.createAccount(command))
                    .isInstanceOf(AccountAlreadyExistsException.class)
                    .hasMessageContaining("Account with provided name and currency already exists");
            assertThat(accountRepository.findAllBy(user.groupId())).hasSize(1);
        }

        @Test
        void shouldAllowSameNameWithDifferentCurrencies() {
            // given
            var accountName = "Multi-Currency Account";
            var command1 = Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), accountName)
                    .set(field(CreateAccountCommand::currency), PLN)
                    .create();
            var command2 = Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), accountName)
                    .set(field(CreateAccountCommand::currency), EUR)
                    .create();
            var user1 = userExists(command1);
            var user2 = userExists(command2);

            // when
            var account1 = accountService.createAccount(command1);
            var account2 = accountService.createAccount(command2);

            // then
            assertThat(account1.name()).isEqualTo(accountName);
            assertThat(account1.balance().currency()).isEqualTo(PLN);
            assertThat(account2.name()).isEqualTo(accountName);
            assertThat(account2.balance().currency()).isEqualTo(EUR);
            assertThat(accountRepository.findAllBy(user1.groupId())).hasSize(1);
            assertThat(accountRepository.findAllBy(user2.groupId())).hasSize(1);
        }
    }

    @Nested
    class AccountRetrieval {
        @Test
        void shouldReturnAllActiveAccounts() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account1 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Checking Account")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());
            var account2 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Savings Account")
                    .set(field(CreateAccountCommand::currency), EUR)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());
            var account3 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Investment Account")
                    .set(field(CreateAccountCommand::currency), USD)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());

            // when
            var result = accountService.getAccounts(userId);

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
                assertThat(account.lastUpdatedAt()).isNotNull();
            });
        }

        @Test
        void shouldReturnEmptyListWhenNoAccountsExist() {
            // given
            var userId = UserId.generate();
            userExistsInGroup(userId, GroupId.generate());

            // when
            var result = accountService.getAccounts(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnSpecificAccountById() {
            // given
            var accountName = "Test Account";
            var currency = EUR;
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), accountName)
                    .set(field(CreateAccountCommand::currency), currency)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());

            // when
            var result = accountService.getById(account.id(), userId);

            // then
            assertThat(result).isEqualTo(account);
            assertThat(result.id()).isEqualTo(account.id());
            assertThat(result.name()).isEqualTo(accountName);
            assertThat(result.balance().currency()).isEqualTo(currency);
            assertThat(result.balance().value()).isZero();
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.lastUpdatedAt()).isNotNull();
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            // given
            var nonExistentAccountId = AccountId.generate();
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);

            // when & then
            assertThatThrownBy(() -> accountService.getById(nonExistentAccountId, userId))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        void shouldReturnAccountsWithCompleteFieldStructure() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Complete Account")
                    .set(field(CreateAccountCommand::currency), GBP)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());

            // when
            var retrievedAccount = accountService.getById(account.id(), userId);

            // then
            assertThat(retrievedAccount.id()).isNotNull();
            assertThat(retrievedAccount.name()).isEqualTo("Complete Account");
            assertThat(retrievedAccount.balance().currency()).isEqualTo(GBP);
            assertThat(retrievedAccount.balance().value()).isZero();
            assertThat(retrievedAccount.createdAt()).isNotNull();
            assertThat(retrievedAccount.lastUpdatedAt()).isNotNull();
        }

        @Test
        void shouldReturnAccountWhenUserBelongsToSameGroup() {
            // given
            var groupId = GroupId.generate();
            var accountOwnerUserId = UserId.generate();
            var requestingUserId = UserId.generate();
            userExistsInGroup(accountOwnerUserId, groupId);
            userExistsInGroup(requestingUserId, groupId);
            
            var account = accountService.createAccount(new CreateAccountCommand("Shared Account", PLN, accountOwnerUserId));

            // when
            var retrievedAccount = accountService.getById(account.id(), requestingUserId);

            // then
            assertThat(retrievedAccount).isEqualTo(account);
            assertThat(retrievedAccount.id()).isEqualTo(account.id());
            assertThat(retrievedAccount.name()).isEqualTo("Shared Account");
        }

        @Test
        void shouldThrowExceptionWhenUserBelongsToDifferentGroup() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);
            
            var requestingUserId = UserId.generate();
            var requestingUserGroupId = GroupId.generate();
            userExistsInGroup(requestingUserId, requestingUserGroupId);
            
            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", EUR, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.getById(account.id(), requestingUserId))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFoundWithAuthorization() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var nonExistentAccountId = AccountId.generate();

            // when & then
            assertThatThrownBy(() -> accountService.getById(nonExistentAccountId, userId))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        void shouldReturnAccountWhenGroupIdMatches() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(new CreateAccountCommand("Group Account", PLN, userId));

            // when
            var retrievedAccount = accountService.getById(account.id(), groupId);

            // then
            assertThat(retrievedAccount).isEqualTo(account);
            assertThat(retrievedAccount.id()).isEqualTo(account.id());
            assertThat(retrievedAccount.name()).isEqualTo("Group Account");
            assertThat(retrievedAccount.ownedBy()).isEqualTo(groupId);
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFoundWithGroupId() {
            // given
            var groupId = GroupId.generate();
            var nonExistentAccountId = AccountId.generate();

            // when & then
            assertThatThrownBy(() -> accountService.getById(nonExistentAccountId, groupId))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        void shouldThrowExceptionWhenGroupIdDoesNotMatchAccountGroup() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            var differentGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);
            
            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", EUR, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.getById(account.id(), differentGroupId))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    class AccountUpdate {
        @Test
        void shouldUpdateAccountName() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Original Name")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());
            var newName = "Updated Name";

            // when
            var updatedAccount = accountService.updateAccount(account.id(), newName, groupId);

            // then
            assertThat(updatedAccount.name()).isEqualTo(newName);
            var retrievedAccount = accountService.getById(account.id(), userId);
            assertThat(retrievedAccount.name()).isEqualTo(newName);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistentAccount() {
            // given
            var nonExistentAccountId = AccountId.generate();
            var newName = "Updated Name";

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(nonExistentAccountId, newName, GroupId.generate()))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingAccountWithInvalidName() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Original Name")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());
            var emptyName = "";

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(account.id(), emptyName, groupId))
                    .isInstanceOf(AccountNameEmptyException.class);

            // and
            var retrievedAccount = accountService.getById(account.id(), userId);
            assertThat(retrievedAccount.name()).isEqualTo("Original Name");
        }

        @Test
        void shouldThrowExceptionWhenUpdatingWithDuplicateName() {
            // given
            var userId = UserId.generate();
            var groupId = GroupId.generate();
            userExistsInGroup(userId, groupId);
            var account1 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Account One")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());
            var account2 = accountService.createAccount(Instancio.of(CreateAccountCommand.class)
                    .set(field(CreateAccountCommand::name), "Account Two")
                    .set(field(CreateAccountCommand::currency), PLN)
                    .set(field(CreateAccountCommand::userId), userId)
                    .create());

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(account2.id(), "Account One", groupId))
                    .isInstanceOf(AccountAlreadyExistsException.class);

            // verify original names are preserved
            var retrievedAccount1 = accountService.getById(account1.id(), userId);
            var retrievedAccount2 = accountService.getById(account2.id(), userId);
            assertThat(retrievedAccount1.name()).isEqualTo("Account One");
            assertThat(retrievedAccount2.name()).isEqualTo("Account Two");
        }
    }

    @Nested
    class AccountDeletion {

        @Test
        void shouldDeleteAccountWithZeroBalance() {
            // given
            var userId = UserId.generate();
            userExistsInGroup(userId, GroupId.generate());
            var account = accountService.createAccount(new CreateAccountCommand("Test Account", PLN, userId));

            // when
            accountService.deleteAccount(account.id(), userId);

            // then
            assertThat(accountService.getAccounts(userId)).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistentAccount() {
            // given
            var nonExistentAccountId = AccountId.generate();
            var userId = UserId.generate();
            userExistsInGroup(userId, GroupId.generate());

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(nonExistentAccountId, userId))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldRejectDeletionOfAccountWithTransactionHistory() {
            // given
            var userId = UserId.generate();
            userExistsInGroup(userId, GroupId.generate());
            var account = accountService.createAccount(new CreateAccountCommand("Account With Transactions", PLN, userId));
            accountService.addTransaction(account.id(), TransactionId.generate(), Money.of(new BigDecimal("100.00"), PLN), EXPENSE, userId);

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(account.id(), userId))
                    .isInstanceOf(CannotDeleteAccountWithTransactionsException.class);
            assertThat(accountService.getAccounts(userId)).hasSize(1);
        }

        @Test
        void shouldAllowDeletionWhenUserBelongsToSameGroupAsAccount() {
            // given
            var groupId = GroupId.generate();
            var userId = UserId.generate();
            userExistsInGroup(userId, groupId);
            var account = accountService.createAccount(new CreateAccountCommand("Test Account", PLN, userId));

            // when
            accountService.deleteAccount(account.id(), userId);

            // then
            assertThat(accountService.getAccounts(userId)).isEmpty();
        }

        @Test
        void shouldRejectDeletionWhenUserBelongsToDifferentGroupThanAccount() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);

            var attemptingUserId = UserId.generate();
            var attemptingUserGroupId = GroupId.generate();
            userExistsInGroup(attemptingUserId, attemptingUserGroupId);

            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", PLN, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(account.id(), attemptingUserId))
                    .isInstanceOf(AccountNotFoundException.class);

            // verify account still exists
            assertThat(accountService.getAccounts(accountOwnerUserId)).hasSize(1);
        }
    }

    @Nested
    class GroupAccess {

        @Test
        void shouldAllowSameGroupUsersToAccessAccounts() {
            // given
            var groupId = GroupId.generate();
            var user1Id = UserId.generate();
            var user2Id = UserId.generate();
            userExistsInGroup(user1Id, groupId);
            userExistsInGroup(user2Id, groupId);

            var accountUserA = accountService.createAccount(new CreateAccountCommand("Personal PLN", PLN, user1Id));
            var accountUserB = accountService.createAccount(new CreateAccountCommand("Savings USD", USD, user2Id));

            // when
            var accountsForUserA = accountService.getAccounts(user1Id);
            var accountsForUserB = accountService.getAccounts(user2Id);

            // then
            assertThat(accountsForUserA).hasSize(2).containsExactlyInAnyOrder(accountUserA, accountUserB);
            assertThat(accountsForUserB).hasSize(2).containsExactlyInAnyOrder(accountUserA, accountUserB);
        }

        @Test
        void shouldAllowSameGroupUsersToModifyAccounts() {
            // given
            var groupId = GroupId.generate();
            var user1Id = UserId.generate();
            var user2Id = UserId.generate();
            userExistsInGroup(user1Id, groupId);
            userExistsInGroup(user2Id, groupId);

            var businessAccount = accountService.createAccount(new CreateAccountCommand("Business Account", EUR, user1Id));

            // when
            var updatedAccount = accountService.updateAccount(businessAccount.id(), "Updated Business Account", groupId);

            // then
            assertThat(updatedAccount.name()).isEqualTo("Updated Business Account");
            var retrievedAccount = accountService.getById(businessAccount.id(), user2Id);
            assertThat(retrievedAccount.name()).isEqualTo("Updated Business Account");
            assertThat(retrievedAccount.lastUpdatedAt()).isAfter(businessAccount.lastUpdatedAt());
        }

        @Test
        void shouldRestrictAccessBetweenDifferentGroups() {
            // given
            var user1Id = UserId.generate();
            var user2Id = UserId.generate();
            userExistsInGroup(user1Id, GroupId.generate());
            userExistsInGroup(user2Id, GroupId.generate());

            var accountUserA = accountService.createAccount(new CreateAccountCommand("Private Account", PLN, user1Id));
            var accountUserB = accountService.createAccount(new CreateAccountCommand("Group Y Account", EUR, user2Id));

            // when
            var accountsForUserB = accountService.getAccounts(user2Id);

            // then
            assertThat(accountsForUserB).hasSize(1).containsOnly(accountUserB).doesNotContain(accountUserA);
        }

        @Test
        void shouldRejectCrossGroupAccountAccess() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);
            
            var requestingUserId = UserId.generate();
            var requestingUserGroupId = GroupId.generate();
            userExistsInGroup(requestingUserId, requestingUserGroupId);
            
            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", PLN, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.getById(account.id(), requestingUserId))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldRejectCrossGroupAccountModification() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);
            
            var attemptingGroupId = GroupId.generate();
            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", PLN, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.updateAccount(account.id(), "New Name", attemptingGroupId))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        void shouldRejectCrossGroupAccountDeletion() {
            // given
            var accountOwnerUserId = UserId.generate();
            var accountOwnerGroupId = GroupId.generate();
            userExistsInGroup(accountOwnerUserId, accountOwnerGroupId);

            var attemptingUserId = UserId.generate();
            var attemptingUserGroupId = GroupId.generate();
            userExistsInGroup(attemptingUserId, attemptingUserGroupId);

            var account = accountService.createAccount(new CreateAccountCommand("Protected Account", PLN, accountOwnerUserId));

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(account.id(), attemptingUserId))
                    .isInstanceOf(AccountNotFoundException.class);

            // verify account still exists
            assertThat(accountService.getAccounts(accountOwnerUserId)).hasSize(1);
        }
    }
}