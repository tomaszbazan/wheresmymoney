import '../models/parse_error_type.dart';

class ErrorMessages {
  static String getMessage(ErrorType type, int lineNumber) {
    switch (type) {
      case ErrorType.invalidFileType:
        return 'Nieprawidłowy format pliku. Dozwolone są tylko pliki CSV.';
      case ErrorType.fileTooLarge:
        return 'Plik jest za duży. Maksymalny rozmiar to 10 MB.';
      case ErrorType.emptyFile:
        return 'Plik jest pusty.';
      case ErrorType.invalidCsvFormat:
        return 'Plik nie zawiera wymaganego formatu mBank.';
      case ErrorType.invalidDateFormat:
        return 'Nieprawidłowy format daty w wierszu $lineNumber.';
      case ErrorType.invalidAmountFormat:
        return 'Nieprawidłowa kwota w wierszu $lineNumber.';
      case ErrorType.invalidCurrency:
        return 'Nieobsługiwana waluta w wierszu $lineNumber.';
      case ErrorType.currencyMismatch:
        return 'Waluta w wierszu $lineNumber nie pasuje do waluty konta.';
      case ErrorType.unknownError:
        return 'Wystąpił nieoczekiwany błąd.';
      case ErrorType.failedToParseCsv:
        return 'Nie udało się przetworzyć pliku CSV.';
      case ErrorType.invalidFile:
        return 'Nieprawidłowy plik.';
    }
  }

  static String? getHint(ErrorType type) {
    switch (type) {
      case ErrorType.invalidFileType:
        return 'Upewnij się, że plik ma rozszerzenie .csv';
      case ErrorType.fileTooLarge:
        return 'Spróbuj zaimportować mniejszy zakres dat';
      case ErrorType.emptyFile:
        return 'Sprawdź czy wybrałeś właściwy plik';
      case ErrorType.invalidCsvFormat:
        return 'Upewnij się, że plik został wyeksportowany z mBanku';
      case ErrorType.invalidDateFormat:
        return 'Sprawdź poprawność daty w pliku CSV';
      case ErrorType.invalidAmountFormat:
        return 'Sprawdź czy kwota zawiera poprawną wartość liczbową';
      case ErrorType.invalidCurrency:
        return 'Obsługiwane waluty: PLN, EUR, USD, GBP';
      case ErrorType.currencyMismatch:
        return 'Wybierz konto w tej samej walucie co transakcje';
      case ErrorType.unknownError:
        return 'Spróbuj ponownie lub skontaktuj się z pomocą techniczną';
      case ErrorType.failedToParseCsv:
        return 'Sprawdź czy plik CSV jest poprawny i nie jest uszkodzony';
      case ErrorType.invalidFile:
        return 'Sprawdź czy wybrałeś właściwy plik CSV';
    }
  }
}
