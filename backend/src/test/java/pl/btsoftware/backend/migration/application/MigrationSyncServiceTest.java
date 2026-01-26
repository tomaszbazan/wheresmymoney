package pl.btsoftware.backend.migration.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.migration.api.SyncAccountRequest;
import pl.btsoftware.backend.migration.api.SyncCategoryRequest;
import pl.btsoftware.backend.migration.api.SyncResultResponse;
import pl.btsoftware.backend.migration.api.SyncTransactionRequest;
import pl.btsoftware.backend.migration.domain.MigrationEntityMapping;
import pl.btsoftware.backend.migration.domain.MigrationEntityMappingRepository;
import pl.btsoftware.backend.migration.domain.MigrationSyncLogRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationSyncServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MigrationEntityMappingRepository mappingRepository;

    @Mock
    private MigrationSyncLogRepository syncLogRepository;

    @InjectMocks
    private MigrationSyncService migrationSyncService;

    private UUID testAccountUuid;
    private UUID testCategoryUuid;
    private UUID testGroupUuid;

    @BeforeEach
    void setUp() {
        testAccountUuid = UUID.randomUUID();
        testCategoryUuid = UUID.randomUUID();
        testGroupUuid = UUID.randomUUID();
    }

    @Test
    void shouldSyncNewTransaction() {
        SyncTransactionRequest request = new SyncTransactionRequest(
                1,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(150.00),
                LocalDateTime.now(),
                "Test expense",
                "EXPENSE",
                1
        );

        when(mappingRepository.existsByEntityTypeAndOldId("TRANSACTION_EXPENSE", 1)).thenReturn(false);
        when(mappingRepository.findByEntityTypeAndOldId("ACCOUNT", 100))
                .thenReturn(Optional.of(createMapping("ACCOUNT", 100, testAccountUuid)));
        when(mappingRepository.findByEntityTypeAndOldId("CATEGORY_EXPENSE", 200))
                .thenReturn(Optional.of(createMapping("CATEGORY_EXPENSE", 200, testCategoryUuid)));
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));

        SyncResultResponse response = migrationSyncService.syncTransaction(request);

        assertThat(response.success()).isTrue();
        assertThat(response.newId()).isNotNull();
        assertThat(response.oldId()).isEqualTo(1);

        verify(transactionRepository).store(any(Transaction.class));
        verify(mappingRepository).save(any(MigrationEntityMapping.class));
        verify(syncLogRepository).save(any());
    }

    @Test
    void shouldSkipAlreadySyncedTransaction() {
        UUID existingTransactionId = UUID.randomUUID();
        SyncTransactionRequest request = new SyncTransactionRequest(
                1,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(150.00),
                LocalDateTime.now(),
                "Test expense",
                "EXPENSE",
                1
        );

        when(mappingRepository.existsByEntityTypeAndOldId("TRANSACTION_EXPENSE", 1)).thenReturn(true);
        when(mappingRepository.findByEntityTypeAndOldId("TRANSACTION_EXPENSE", 1))
                .thenReturn(Optional.of(createMapping("TRANSACTION_EXPENSE", 1, existingTransactionId)));

        SyncResultResponse response = migrationSyncService.syncTransaction(request);

        assertThat(response.success()).isTrue();
        assertThat(response.newId()).isEqualTo(existingTransactionId);
        assertThat(response.message()).contains("Already synced");

        verify(transactionRepository, never()).store(any());
        verify(syncLogRepository).save(any());
    }

    @Test
    void shouldFailTransactionSyncWhenAccountMappingMissing() {
        SyncTransactionRequest request = new SyncTransactionRequest(
                1,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(150.00),
                LocalDateTime.now(),
                "Test expense",
                "EXPENSE",
                1
        );

        when(mappingRepository.existsByEntityTypeAndOldId("TRANSACTION_EXPENSE", 1)).thenReturn(false);
        when(mappingRepository.findByEntityTypeAndOldId("ACCOUNT", 100)).thenReturn(Optional.empty());

        SyncResultResponse response = migrationSyncService.syncTransaction(request);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("Missing mapping");

        verify(transactionRepository, never()).store(any());
    }

    @Test
    void shouldSyncNewAccount() {
        SyncAccountRequest request = new SyncAccountRequest(
                1,
                "Test Account",
                BigDecimal.valueOf(1000.00),
                1,
                20,
                10,
                "Test comment",
                LocalDateTime.now()
        );

        when(mappingRepository.findByEntityTypeAndOldId("ACCOUNT", 1)).thenReturn(Optional.empty());
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));

        SyncResultResponse response = migrationSyncService.syncAccount(request);

        assertThat(response.success()).isTrue();
        assertThat(response.newId()).isNotNull();
        assertThat(response.oldId()).isEqualTo(1);

        verify(accountRepository).store(any(Account.class));
        verify(mappingRepository).save(any(MigrationEntityMapping.class));
    }

    @Test
    void shouldUpdateExistingAccountBalance() {
        UUID existingAccountId = UUID.randomUUID();
        Account existingAccount = mock(Account.class);
        Money oldBalance = Money.of(BigDecimal.valueOf(500.00), Currency.PLN);
        Money newBalance = Money.of(BigDecimal.valueOf(1000.00), Currency.PLN);

        when(existingAccount.balance()).thenReturn(oldBalance);
        when(existingAccount.deposit(any())).thenReturn(existingAccount);
        when(existingAccount.withdraw(any())).thenReturn(existingAccount);

        SyncAccountRequest request = new SyncAccountRequest(
                1,
                "Test Account",
                BigDecimal.valueOf(1000.00),
                1,
                20,
                10,
                "Test comment",
                LocalDateTime.now()
        );

        when(mappingRepository.findByEntityTypeAndOldId("ACCOUNT", 1))
                .thenReturn(Optional.of(createMapping("ACCOUNT", 1, existingAccountId)));
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));
        when(accountRepository.findById(new AccountId(existingAccountId), new GroupId(testGroupUuid)))
                .thenReturn(Optional.of(existingAccount));

        SyncResultResponse response = migrationSyncService.syncAccount(request);

        assertThat(response.success()).isTrue();
        assertThat(response.message()).contains("updated");

        verify(accountRepository).store(existingAccount);
    }

    @Test
    void shouldSyncNewCategory() {
        SyncCategoryRequest request = new SyncCategoryRequest(
                1,
                "Food",
                "EXPENSE",
                "Food expenses",
                "#FF0000",
                null,
                20,
                10,
                LocalDateTime.now()
        );

        when(mappingRepository.existsByEntityTypeAndOldId("CATEGORY_EXPENSE", 1)).thenReturn(false);
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));

        SyncResultResponse response = migrationSyncService.syncCategory(request);

        assertThat(response.success()).isTrue();
        assertThat(response.newId()).isNotNull();
        assertThat(response.oldId()).isEqualTo(1);

        verify(categoryRepository).store(any(Category.class));
        verify(mappingRepository).save(any(MigrationEntityMapping.class));
    }

    @Test
    void shouldSyncCategoryWithParent() {
        UUID parentCategoryId = UUID.randomUUID();
        SyncCategoryRequest request = new SyncCategoryRequest(
                2,
                "Groceries",
                "EXPENSE",
                "Grocery expenses",
                "#FF0000",
                1,
                20,
                10,
                LocalDateTime.now()
        );

        when(mappingRepository.existsByEntityTypeAndOldId("CATEGORY_EXPENSE", 2)).thenReturn(false);
        when(mappingRepository.findByEntityTypeAndOldId("CATEGORY_EXPENSE", 1))
                .thenReturn(Optional.of(createMapping("CATEGORY_EXPENSE", 1, parentCategoryId)));
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));

        SyncResultResponse response = migrationSyncService.syncCategory(request);

        assertThat(response.success()).isTrue();

        verify(categoryRepository).store(any(Category.class));
    }

    @Test
    void shouldMapCurrencyCorrectly() {
        SyncTransactionRequest requestPLN = createTransactionRequest(1);
        SyncTransactionRequest requestEUR = createTransactionRequest(2);
        SyncTransactionRequest requestUSD = createTransactionRequest(3);

        setupBasicMappings();

        migrationSyncService.syncTransaction(requestPLN);
        migrationSyncService.syncTransaction(requestEUR);
        migrationSyncService.syncTransaction(requestUSD);

        verify(transactionRepository, times(3)).store(any(Transaction.class));
    }

    private SyncTransactionRequest createTransactionRequest(Integer currencyId) {
        return new SyncTransactionRequest(
                currencyId,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now(),
                "Test",
                "EXPENSE",
                currencyId
        );
    }

    private void setupBasicMappings() {
        when(mappingRepository.existsByEntityTypeAndOldId(anyString(), anyInt())).thenReturn(false);
        when(mappingRepository.findByEntityTypeAndOldId("ACCOUNT", 100))
                .thenReturn(Optional.of(createMapping("ACCOUNT", 100, testAccountUuid)));
        when(mappingRepository.findByEntityTypeAndOldId("CATEGORY_EXPENSE", 200))
                .thenReturn(Optional.of(createMapping("CATEGORY_EXPENSE", 200, testCategoryUuid)));
        when(mappingRepository.findByEntityTypeAndOldId("GROUP", 20))
                .thenReturn(Optional.of(createMapping("GROUP", 20, testGroupUuid)));
    }

    private MigrationEntityMapping createMapping(String entityType, Integer oldId, UUID newId) {
        return new MigrationEntityMapping(entityType, oldId, null, newId);
    }
}
