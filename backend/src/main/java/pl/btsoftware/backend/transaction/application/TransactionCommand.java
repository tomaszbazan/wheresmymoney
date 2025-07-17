package pl.btsoftware.backend.transaction.application;

public interface TransactionCommand<T> {
    T execute();
}