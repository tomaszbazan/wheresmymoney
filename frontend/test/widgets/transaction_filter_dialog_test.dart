import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/transaction_filter.dart';
import 'package:frontend/widgets/transaction_filter_dialog.dart';

void main() {
  testWidgets('Reset button clears all fields and Apply returns empty filter', (WidgetTester tester) async {
    TransactionFilter? result;
    final initialFilter = TransactionFilter(minAmount: 100, maxAmount: 200, description: 'Test', dateFrom: DateTime(2025, 1, 1));

    await tester.pumpWidget(
      MaterialApp(
        home: Builder(
          builder: (context) {
            return Center(
              child: TextButton(
                onPressed: () async {
                  result = await showDialog<TransactionFilter>(
                    context: context,
                    builder: (context) => TransactionFilterDialog(initialFilter: initialFilter, accounts: const [], categories: const []),
                  );
                },
                child: const Text('Open Dialog'),
              ),
            );
          },
        ),
      ),
    );

    // Open the dialog
    await tester.tap(find.text('Open Dialog'));
    await tester.pumpAndSettle();

    // Verify initial values are populated
    expect(find.text('100.0'), findsOneWidget); // minAmount

    // Tap Reset
    await tester.tap(find.text('Reset'));
    await tester.pumpAndSettle();

    // Verify fields are cleared visually
    expect(find.text('100.0'), findsNothing);

    // Tap Apply
    await tester.tap(find.text('Apply'));
    await tester.pumpAndSettle();

    // Verify the result
    expect(result, isNotNull);
    expect(result!.minAmount, isNull);
    expect(result!.maxAmount, isNull);
    expect(result!.description, isNull);
    expect(result!.dateFrom, isNull);
  });
}
