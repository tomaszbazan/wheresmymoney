package pl.btsoftware.backend.account.domain;

public enum Currency {
    PLN,
    EUR,
    USD,
    GBP;

    public static final Currency DEFAULT = PLN;
}