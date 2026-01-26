# Strategia Migracji Danych z WheresMyMoney_old do WheresMyMoney

## Przegląd

Ten dokument opisuje strategię migracji danych ze starej aplikacji WheresMyMoney_old do nowej aplikacji WheresMyMoney.

## Kluczowe Różnice Między Aplikacjami

### Stara Aplikacja (WheresMyMoney_old)

- **ID**: INTEGER z sekwencjami
- **Kategorie**: Oddzielne tabele `income_category` i `expense_category`
- **Transakcje**: Oddzielne tabele `income` i `expense`
- **Użytkownicy**: Identyfikacja przez `login` (VARCHAR)
- **Grupy**: Identyfikacja przez `group_name` (VARCHAR)
- **Usuwanie**: Pole `used` (boolean) - soft delete
- **Historia**: Osobna tabela `history` dla audytu

### Nowa Aplikacja (WheresMyMoney)

- **ID**: UUID dla wszystkich encji
- **Kategorie**: Jedna tabela `category` z polem `type` (INCOME/EXPENSE)
- **Transakcje**: Jedna tabela `transaction` z polem `type` (INCOME/EXPENSE)
- **Użytkownicy**: ID jako VARCHAR(100), email wymagany, powiązanie z grupą
- **Grupy**: UUID, lepsze zarządzanie członkostwem (`group_members`)
- **Usuwanie**: Pola `is_deleted`, `deleted_at` - soft delete
- **Audyt**: `audit_log` oraz pola `created_at`, `created_by`, `updated_at`, `updated_by`
- **Transfery**: Nowa tabela `transfer` dla transferów między kontami

## Mapowanie Danych

### Użytkownicy i Grupy

```sql
stara
.
user
.
login
-> nowa.users.id (VARCHAR)
stara.user.email          -> nowa.users.email
stara.user.name + surname -> nowa.users.display_name
stara.group.group_name    -> nowa.groups.name
stara.user_group          -> nowa.group_members
```

### Konta

```sql
stara
.
account
.
account_name
-> nowa.account.name
stara.account.amount       -> nowa.account.balance
stara.account.id_currency  -> nowa.account.currency (1=PLN, 2=EUR, 3=USD)
```

### Kategorie

```sql
-- Wydatki
stara
.
expense_category
.
expense_category_name
-> nowa.category.name
stara.expense_category.comment               -> nowa.category.description
stara.expense_category.icon                  -> nowa.category.color
                                                 nowa.category.type = 'EXPENSE'

-- Przychody
stara.income_category.income_category_name   -> nowa.category.name
stara.income_category.comment                -> nowa.category.description
stara.income_category.icon                   -> nowa.category.color
                                                 nowa.category.type = 'INCOME'
```

### Transakcje

```sql
-- Wydatki
stara
.
expense
.
amount
-> nowa.transaction.amount (ABS)
stara.expense.date                -> nowa.transaction.created_at
stara.expense.comment             -> nowa.transaction.description
stara.expense.id_expense_category -> nowa.transaction.category_id (mapped)
                                     nowa.transaction.type = 'EXPENSE'

-- Przychody
stara.income.amount               -> nowa.transaction.amount (ABS)
stara.income.date                 -> nowa.transaction.created_at
stara.income.comment              -> nowa.transaction.description
stara.income.id_income_category   -> nowa.transaction.category_id (mapped)
                                     nowa.transaction.type = 'INCOME'
```

## Skrypt Migracji

Główny skrypt migracji znajduje się w:

```
backend/src/main/resources/db/migration/V12__migrate_data_from_old_app.sql
```

### Wymagania Wstępne

1. PostgreSQL z rozszerzeniem `dblink`
2. Dostęp do obu baz danych z tego samego serwera
3. Uprawnienia do tworzenia temporary tables
4. Wszystkie migracje V1-V11 muszą być wykonane w nowej bazie

### Konfiguracja dblink

Przed uruchomieniem skryptu należy skonfigurować połączenie:

```sql
-- W pliku migracji należy zaktualizować connection string:
'dbname=wheresmymoney_old host=127.0.0.1 user=wheresmymoney password=yourpassword'
```

### Kolejność Migracji

1. **Grupy** (`groups`)
2. **Użytkownicy** (`users`)
3. **Członkowie grup** (`group_members`)
4. **Konta** (`account`)
5. **Kategorie** (`category`) - zarówno income jak i expense
6. **Relacje parent-child dla kategorii** (aktualizacja `parent_id`)
7. **Transakcje** (`transaction`) - zarówno income jak i expense

## Strategie Synchronizacji na Czas Przejścia

### Opcja 1: Tryb Read-Only dla Starej Aplikacji (ZALECANE)

**Opis**: Przed migracją ustaw starą aplikację w trybie read-only, wykonaj migrację, następnie przełącz użytkowników na
nową aplikację.

**Zalety**:

- Najprostsza implementacja
- Brak ryzyka niespójności danych
- Najszybsze przejście

**Wady**:

- Wymaga przestoju (użytkownicy nie mogą dodawać transakcji podczas migracji)
- Może być problematyczne jeśli migracja trwa długo

**Implementacja**:

```bash
# 1. Włącz tryb read-only w starej aplikacji
# 2. Wykonaj backup obu baz
# 3. Uruchom migrację
psql -h localhost -U wheresmymoney -d wheresmymoney_new -f V12__migrate_data_from_old_app.sql
# 4. Zweryfikuj dane
# 5. Przełącz użytkowników na nową aplikację
# 6. Wyłącz starą aplikację
```

---

### Opcja 2: Synchronizacja przez Webhook/Event (ŚREDNIA ZŁOŻONOŚĆ)

**Opis**: Stara aplikacja wysyła eventy do nowej aplikacji przy każdej zmianie danych. Nowa aplikacja odbiera eventy i
zapisuje dane.

**Zalety**:

- Synchronizacja w czasie rzeczywistym
- Możliwość testowania nowej aplikacji z aktualnymi danymi
- Stopniowe przejście

**Wady**:

- Wymaga modyfikacji starej aplikacji
- Potencjalne problemy z kolejnością eventów
- Bardziej skomplikowana implementacja

**Implementacja**:

1. **W starej aplikacji** - dodaj endpoint do wysyłania eventów:

```java
// WheresMyMoney_old: EventSender.java
@Service
public class EventSender {
    private final RestTemplate restTemplate;
    private final String newAppUrl;

    public void sendTransactionCreated(Transaction transaction) {
        TransactionEvent event = new TransactionEvent(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getDate(),
            // ... inne pola
        );

        try {
            restTemplate.postForEntity(
                newAppUrl + "/api/migration/sync/transaction",
                event,
                Void.class
            );
        } catch (Exception e) {
            log.error("Failed to sync transaction to new app", e);
            // Opcjonalnie: retry logic
        }
    }
}
```

2. **W nowej aplikacji** - dodaj endpoint do odbierania eventów (szczegóły w kolejnym pliku)

---

### Opcja 3: Periodic Sync via Scheduled Job (ZALECANE dla małej liczby użytkowników)

**Opis**: Regularny job (np. co 5-15 minut) synchronizuje zmiany ze starej do nowej aplikacji.

**Zalety**:

- Nie wymaga zmian w starej aplikacji
- Prostsze niż Opcja 2
- Tolerancja na błędy (retry w następnym cyklu)

**Wady**:

- Opóźnienie w synchronizacji (do 15 minut)
- Może być nieefektywne dla dużej liczby użytkowników

**Implementacja**:

1. **W nowej aplikacji** - dodaj scheduled job (szczegóły w oddzielnym pliku)
2. Job używa dblink do pobrania nowych/zmienionych rekordów ze starej bazy
3. Mapuje i zapisuje dane w nowej bazie

---

### Opcja 4: Dual Write z Reconciliation (NAJBARDZIEJ ZŁOŻONA)

**Opis**: Aplikacja zapisuje dane równocześnie do obu baz, z mechanizmem reconciliation do wykrywania rozbieżności.

**Zalety**:

- Zero downtime
- Pełna synchronizacja
- Możliwość rollback

**Wady**:

- Bardzo skomplikowana implementacja
- Ryzyko niespójności
- Wymaga znacznych zmian w obu aplikacjach

**NIE ZALECANE** dla małej liczby użytkowników.

---

## Rekomendacje

### Dla małej liczby użytkowników (< 10):

**Opcja 1 (Read-Only)** lub **Opcja 3 (Periodic Sync)**

### Plan wdrożenia:

1. **Przygotowanie** (1-2 dni):
    - Przeprowadź pełny backup obu baz
    - Przetestuj skrypt migracji na kopii produkcyjnej bazy
    - Przygotuj dokumentację dla użytkowników

2. **Migracja początkowa** (2-4 godziny):
    - Wykonaj migrację danych
    - Zweryfikuj poprawność migracji
    - Włącz synchronizację (jeśli wybrano Opcję 2 lub 3)

3. **Okres testowy** (1-2 tygodnie):
    - Wybrani użytkownicy testują nową aplikację
    - Dane są synchronizowane ze starej aplikacji
    - Zbieranie feedbacku

4. **Przełączenie** (1 dzień):
    - Ustaw starą aplikację w trybie read-only
    - Wykonaj końcową synchronizację
    - Przełącz wszystkich użytkowników na nową aplikację
    - Monitoruj przez kilka dni

5. **Wyłączenie** (po 1 miesiącu):
    - Jeśli wszystko działa poprawnie, wyłącz starą aplikację
    - Zachowaj backup na 6 miesięcy

## Weryfikacja Migracji

Skrypt weryfikacyjny:

```sql
-- Sprawdź liczby rekordów
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'groups', COUNT(*) FROM groups
UNION ALL
SELECT 'accounts', COUNT(*) FROM account
UNION ALL
SELECT 'categories', COUNT(*) FROM category
UNION ALL
SELECT 'transactions', COUNT(*) FROM transaction;

-- Sprawdź sumy transakcji
SELECT
    type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount
FROM transaction
GROUP BY type;

-- Sprawdź kategorie
SELECT
    type,
    COUNT(*) as category_count
FROM category
GROUP BY type;
```

## Rollback Plan

W przypadku problemów:

1. **Przed migracją**: Przywróć backup
2. **Po migracji, przed przełączeniem**: Usuń dane z nowej bazy i uruchom migrację ponownie
3. **Po przełączeniu**: Przywróć starą aplikację i backup (OSTATECZNOŚĆ)

## Monitoring

Po migracji monitoruj:

- Logi błędów w nowej aplikacji
- Czas odpowiedzi API
- Feedback użytkowników
- Spójność danych (porównaj sumy kontrolne)
