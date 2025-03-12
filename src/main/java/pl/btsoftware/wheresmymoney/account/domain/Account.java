package pl.btsoftware.wheresmymoney.account.domain;

public record Account(AccountId id, String name) {
    public Account changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        return new Account(id, newName);
    }

    // Dodatkowe metody biznesowe można dodać tutaj
}