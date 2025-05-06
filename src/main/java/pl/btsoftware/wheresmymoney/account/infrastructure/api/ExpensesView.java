package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import pl.btsoftware.wheresmymoney.account.domain.Expense;

import java.util.List;

public record ExpensesView(List<ExpenseView> expenses) {
    public static ExpensesView from(List<Expense> expenses) {
        return new ExpensesView(expenses.stream()
                .map(ExpenseView::from)
                .toList());
    }
}