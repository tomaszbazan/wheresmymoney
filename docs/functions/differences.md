• Poniżej mapa pokrycia wymagań z docs/functions/*/ac-be.md przez testy oraz różnice i braki.

Account

- Tworzenie konta
    - OK: różne waluty, brak waluty = domyślnie PLN, puste/null/whitespace nazwy, zbyt długa nazwa, niedozwolone znaki,
      duplikaty nazwa+waluta, ta sama nazwa z inną walutą.
        - Testy: backend/src/test/java/pl/btsoftware/backend/account/application/AccountServiceTest.java:32, :59, :75, :
          120, :135, :168, :178, :199 itd.
        - 400 przy niepoprawnej walucie (JPY) jest testowane, ale bez weryfikacji treści komunikatu.
            - Wymaganie: docs/functions/account/ac-be.md:48–54
            - Test: backend/src/test/java/pl/btsoftware/backend/account/infrastructure/api/AccountControllerTest.java:
              208–224
        - Drobna luka: brak dodatniego testu akceptacji apostrofu (wymaganie dopuszcza spacje, myślniki, apostrofy; test
          pozytywny używa myślnika i znaków PL, ale nie apostrofu).
            - Wymaganie: docs/functions/account/ac-be.md:38–46
- Pobieranie kont
    - OK: lista aktywnych (200, pola obecne w warstwie domeny), konto po ID (200, w tym pola createdAt/updatedAt).
        - Wymaganie: docs/functions/account/ac-be.md:73–88, :89–96
        - Testy: lista (controller) backend/.../AccountControllerTest.java:70–96; szczegóły (controller) :226–246;
          szczegóły pól (service) backend/.../AccountServiceTest.java:220–257
    - Różnice:
        - Lista kont (controller) nie weryfikuje createdAt/updatedAt (wymaganie je wymienia). Propozycja: dodać asercje.
            - Wymaganie: docs/functions/account/ac-be.md:79–81
- Modyfikacja konta
    - OK: aktualizacja nazwy (200), walidacje w domenie, 404 dla nieistniejącego.
        - Testy: update (controller) backend/.../AccountControllerTest.java:125–149; błędy 404 (controller) :283–301
    - Różnice/braki:
        - 409 Conflict przy duplikacie nazwy jest wymagany (w domenie jest wyjątek), ale brak testu HTTP 409.
            - Wymaganie: docs/functions/account/ac-be.md:137–147
            - Test domenowy: backend/.../AccountServiceTest.java:480–506
        - Odświeżenie updatedAt: wprost niezweryfikowane w teście update (service/controller), choć sprawdzane w teście
          grup (patrz niżej).
            - Wymaganie: docs/functions/account/ac-be.md:132–136
- Usuwanie konta
    - Różnica: Wymaganie mówi o 200 OK, test (controller) oczekuje 204 No Content.
        - Wymaganie: docs/functions/account/ac-be.md:151–158
        - Test: backend/.../AccountControllerTest.java:113–123
        - Decyzja: ujednolicić – albo zmienić wymaganie na 204, albo testy/kod na 200.
    - Braki: brak testu HTTP 422 dla konta z historią transakcji (wymagane); jest test domenowy z właściwym wyjątkiem.
        - Wymaganie: docs/functions/account/ac-be.md:160–167
        - Test (service): backend/.../AccountServiceTest.java:546–560
    - 404 dla braku konta przy DELETE jest pokryty (controller).
        - Test: backend/.../AccountControllerTest.java:328–342
- Dostęp grupowy
    - OK: dostęp/edycja wewnątrz grupy oraz ograniczenia między grupami są testowane (domena).
        - Testy: backend/.../AccountServiceTest.java:600+ np. :603, :624, :645, :663
    - Różnica: wymagania sugerują komunikaty “Access denied ...” przy próbie cross‑group, testy sprawdzają
      AccountNotFoundException (maskowanie 404 zamiast 403/401).
        - Wymaganie: docs/functions/account/ac-be.md:211–240
        - Test: backend/.../AccountServiceTest.java:663–692
        - Decyzja: ujednolicić – albo dostosować wymagania do strategii “not found”, albo zmienić mapowanie wyjątków.

Transactions

- Tworzenie transakcji
    - OK: INCOME/EXPENSE, aktualizacja salda, przechowywanie w repozytorium, powiązanie z kontem (service).
        - Test: backend/src/test/java/pl/btsoftware/backend/transaction/application/TransactionServiceTest.java:
          63–116, :117–...
    - Braki:
        - Brak testu controller dla błędu “konto nie istnieje” (AC-2.1: 404).
            - Wymaganie: docs/functions/transactions/ac-be.md:47–57
            - Jest test service: TransactionServiceTest.shouldRejectTransactionForNonexistentAccount()
        - Brak testu controller dla niezgodności walut (AC-2.3: 422).
            - Wymaganie: docs/functions/transactions/ac-be.md:71–81
            - Jest test service: TransactionServiceTest.shouldRejectTransactionWithCurrencyMismatch()
        - Kolizja treści komunikatu przy opisie: AC mówi “1–200 znaków”, test domenowy dopuszcza pusty/null i zwraca
          “cannot exceed 200 characters”.
            - Wymaganie: docs/functions/transactions/ac-be.md:59–69
            - Testy: TransactionServiceTest.shouldAllowTransactionWithEmptyDescription(), ...NullDescription(),
              ...DescriptionTooLong() backend/.../TransactionServiceTest.java:186–215
            - Decyzja: ujednolicić wymaganie albo zachowanie.
- Pobieranie transakcji
    - Różnice:
        - AC-3.1 wymienia pole “date”; test controller nie weryfikuje date.
            - Wymaganie: docs/functions/transactions/ac-be.md:85–94
            - Test: backend/.../TransactionControllerTest.java:71–96
        - AC-3.2 oczekuje 404; test controller zwraca 400 Bad Request.
            - Wymaganie: docs/functions/transactions/ac-be.md:96–106
            - Test: backend/.../TransactionControllerTest.java:98–113
        - AC-3.3/3.4 wymaga metadanych paginacji i sortowania malejąco po dacie; testy nie sprawdzają ani paginacji, ani
          kolejności (używają containsExactlyInAnyOrder).
            - Wymaganie: docs/functions/transactions/ac-be.md:107–131
            - Testy: controller listy backend/.../TransactionControllerTest.java:115–148, :150–169; service listy
              backend/.../TransactionServiceTest.java:279–307, :304–344
- Aktualizacja transakcji
    - OK: kwota (z aktualizacją salda +250,00), opis (saldo bez zmian), kategoria (saldo bez zmian) w domenie; HTTP 200
      dla update (controller).
        - Wymaganie: docs/functions/transactions/ac-be.md:133–172
        - Testy: service :338–360, :368–388, :398–418; controller update backend/.../TransactionControllerTest.java:
          171–193
    - Różnica: AC-4.4 oczekuje 404; test controller zwraca 400.
        - Wymaganie: docs/functions/transactions/ac-be.md:174–183
        - Test: backend/.../TransactionControllerTest.java:195–212
- Usuwanie transakcji
    - OK: usunięcie – brak w normalnych zapytaniach, przy próbie pobrania 404 (service). Controller zwraca 200 OK.
        - Wymaganie: docs/functions/transactions/ac-be.md:187–200
        - Test: service backend/.../TransactionServiceTest.java:440–474, controller
          backend/.../TransactionControllerTest.java:248–258
    - Różnice/braki:
        - AC-5.2 oczekuje 404; test controller zwraca 400.
            - Wymaganie: docs/functions/transactions/ac-be.md:202–211
            - Test: backend/.../TransactionControllerTest.java:260–274
        - AC-5.1/5.3 mówią o soft delete (flaga/isDeleted i deletedAt). Testy nie weryfikują bezpośrednio deletedAt ani
          flagi w repozytorium (sprawdzają efekt w zapytaniach).
            - Wymaganie: docs/functions/transactions/ac-be.md:187–200, :213–223

Braki wymagań (ac-be.md)

- Users: brak docs/functions/users/ac-be.md mimo rozbudowanych testów modułu użytkownika i grup.
    - Testy: np. backend/src/test/java/pl/btsoftware/backend/users/...
- Category: brak docs/functions/category/ac-be.md, a są testy serwisu i kontrolera kategorii.
    - Testy: backend/src/test/java/pl/btsoftware/backend/category/...

Braki testów (względem AC)

- Account:
    - Controller: 409 Conflict przy duplikacie nazwy (update) docs/functions/account/ac-be.md:137–147.
    - Controller: 422 Unprocessable Entity przy próbie usunięcia konta z historią docs/functions/account/ac-be.md:
      160–167.
    - Controller: lista kont – asercje na createdAt/updatedAt docs/functions/account/ac-be.md:79–81.
    - Service/Controller: pozytywny test nazwy z apostrofem docs/functions/account/ac-be.md:38–46.
    - Uzgodnić status dla DELETE konta: 200 (AC) vs 204 (testy).
- Transactions:
    - Controller: 404 dla braku transakcji (GET/PUT/DELETE) docs/functions/transactions/ac-be.md:96–106, :174–183, :
      202–211.
    - Controller: 404 dla braku konta przy tworzeniu docs/functions/transactions/ac-be.md:47–57.
    - Controller: 422 dla niezgodności waluty docs/functions/transactions/ac-be.md:71–81.
    - Controller/Service: weryfikacja pola date w odpowiedziach docs/functions/transactions/ac-be.md:85–94.
    - Controller/Service: paginacja i sortowanie malejące po dacie dla list docs/functions/transactions/ac-be.md:
      107–131.
    - Uzgodnić walidację opisu: AC “1–200” vs testy dopuszczają pusty/null docs/functions/transactions/ac-be.md:59–69.

Propozycje następnych kroków

- Ujednolicić kontrakt HTTP vs domena:
    - Dodać/zmienić mapowanie wyjątków tak, by 404 i 422 odpowiadały AC (lub zaktualizować AC do obecnych 400/204).
- Uzupełnić testy kontrolerów:
    - Account: 409 na duplikat nazwy; 422 na kasowanie z historią; pola createdAt/updatedAt w listingach.
    - Transactions: 404/422 przypadki błędów; assert pola date; paginacja i sortowanie.
- Uzupełnić wymagania:
    - Dodać docs/functions/users/ac-be.md i docs/functions/category/ac-be.md aby spiąć istniejące testy z wymaganiami.
- Drobne doprecyzowania AC:
    - Komunikaty błędów (np. opis transakcji, cross‑group “Access denied” vs aktualne “Not found”).
    - Status dla DELETE konta (200 vs 204).
