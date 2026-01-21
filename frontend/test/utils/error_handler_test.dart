import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/utils/error_handler.dart';

void main() {
  group('ErrorHandler', () {
    testWidgets('shows HttpException with user-friendly message', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (BuildContext context) {
                return ElevatedButton(
                  onPressed: () {
                    ErrorHandler.showError(context, const HttpException(404, ''));
                  },
                  child: const Text('Trigger Error'),
                );
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Trigger Error'));
      await tester.pump();

      final snackBar = tester.widget<SnackBar>(find.byType(SnackBar));
      expect(snackBar.backgroundColor, equals(Colors.red));

      final snackBarContent = snackBar.content as Text;
      expect(snackBarContent.data, equals('Zasób nie został znaleziony'));
    });

    testWidgets('shows generic error for non-HttpException', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (BuildContext context) {
                return ElevatedButton(
                  onPressed: () {
                    ErrorHandler.showError(context, Exception('Something went wrong'));
                  },
                  child: const Text('Trigger Error'),
                );
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Trigger Error'));
      await tester.pump();

      final snackBar = tester.widget<SnackBar>(find.byType(SnackBar));
      expect(snackBar.backgroundColor, equals(Colors.red));

      final snackBarContent = snackBar.content as Text;
      expect(snackBarContent.data, contains('Nieoczekiwany błąd'));
    });

    testWidgets('does not show error when context is unmounted', (WidgetTester tester) async {
      BuildContext? capturedContext;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (BuildContext context) {
                capturedContext = context;
                return const SizedBox.shrink();
              },
            ),
          ),
        ),
      );

      await tester.pumpWidget(const SizedBox.shrink());

      ErrorHandler.showError(capturedContext!, const HttpException(500, 'Server error'));

      await tester.pump();

      expect(find.byType(SnackBar), findsNothing);
    });

    testWidgets('handles network error message', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (BuildContext context) {
                return ElevatedButton(
                  onPressed: () {
                    ErrorHandler.showError(context, const HttpException(0, 'Connection refused'));
                  },
                  child: const Text('Trigger Error'),
                );
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Trigger Error'));
      await tester.pump();

      final snackBar = tester.widget<SnackBar>(find.byType(SnackBar));
      final snackBarContent = snackBar.content as Text;
      expect(snackBarContent.data, equals('Błąd połączenia z serwerem'));
    });

    testWidgets('allows custom error message', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (BuildContext context) {
                return ElevatedButton(
                  onPressed: () {
                    ErrorHandler.showError(context, Exception('Custom error'), message: 'Custom message displayed');
                  },
                  child: const Text('Trigger Error'),
                );
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Trigger Error'));
      await tester.pump();

      final snackBar = tester.widget<SnackBar>(find.byType(SnackBar));
      final snackBarContent = snackBar.content as Text;
      expect(snackBarContent.data, equals('Custom message displayed'));
    });
  });
}
