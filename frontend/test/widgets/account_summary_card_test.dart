import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/widgets/account_summary_card.dart';

void main() {
  group('AccountSummaryCard', () {
    testWidgets('displays currency sums correctly', (tester) async {
      final currencySums = {'PLN': 1500.50, 'USD': 300.00, 'EUR': 250.75};

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountSummaryCard(currencySums: currencySums))));

      expect(find.text('Saldo łączne według walut:'), findsOneWidget);
      expect(find.text('PLN'), findsOneWidget);
      expect(find.text('1500.50'), findsOneWidget);
      expect(find.text('USD'), findsOneWidget);
      expect(find.text('300.00'), findsOneWidget);
      expect(find.text('EUR'), findsOneWidget);
      expect(find.text('250.75'), findsOneWidget);
    });

    testWidgets('displays negative balance in red color', (tester) async {
      final currencySums = {'PLN': -500.00};

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountSummaryCard(currencySums: currencySums))));

      final textWidget = tester.widget<Text>(find.text('-500.00'));
      expect(textWidget.style?.color, Colors.red);
    });

    testWidgets('displays positive balance in black color', (tester) async {
      final currencySums = {'PLN': 500.00};

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountSummaryCard(currencySums: currencySums))));

      final textWidget = tester.widget<Text>(find.text('500.00'));
      expect(textWidget.style?.color, Colors.black);
    });

    testWidgets('renders as a Card with padding', (tester) async {
      final currencySums = {'PLN': 100.00};

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountSummaryCard(currencySums: currencySums))));

      expect(find.byType(Card), findsOneWidget);
    });
  });
}
