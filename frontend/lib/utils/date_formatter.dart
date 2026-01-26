import 'package:intl/intl.dart';

class DateFormatter {
  static String formatRelativeDate(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inSeconds < 60) {
      return 'przed chwilą';
    } else if (difference.inMinutes < 60) {
      final minutes = difference.inMinutes;
      return 'przed $minutes ${_minutesLabel(minutes)}';
    } else if (difference.inHours < 24) {
      final hours = difference.inHours;
      return 'przed $hours ${_hoursLabel(hours)}';
    } else if (difference.inDays == 1) {
      return 'wczoraj';
    } else if (difference.inDays < 7) {
      final days = difference.inDays;
      return 'przed $days ${_daysLabel(days)}';
    } else if (difference.inDays < 30) {
      final weeks = (difference.inDays / 7).floor();
      return 'przed $weeks ${_weeksLabel(weeks)}';
    } else if (difference.inDays < 365) {
      final months = (difference.inDays / 30).floor();
      return 'przed $months ${_monthsLabel(months)}';
    } else {
      final years = (difference.inDays / 365).floor();
      return 'przed $years ${_yearsLabel(years)}';
    }
  }

  static String formatAbsoluteDate(DateTime dateTime) {
    final formatter = DateFormat('dd.MM.yyyy HH:mm');
    return formatter.format(dateTime);
  }

  static String _minutesLabel(int minutes) {
    if (minutes == 1) return 'minutą';
    if (minutes % 10 >= 2 && minutes % 10 <= 4 && (minutes % 100 < 10 || minutes % 100 >= 20)) {
      return 'minutami';
    }
    return 'minutami';
  }

  static String _hoursLabel(int hours) {
    if (hours == 1) return 'godziną';
    if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) {
      return 'godzinami';
    }
    return 'godzinami';
  }

  static String _daysLabel(int days) {
    if (days == 1) return 'dniem';
    return 'dniami';
  }

  static String _weeksLabel(int weeks) {
    if (weeks == 1) return 'tygodniem';
    return 'tygodniami';
  }

  static String _monthsLabel(int months) {
    if (months == 1) return 'miesiącem';
    return 'miesiącami';
  }

  static String _yearsLabel(int years) {
    if (years == 1) return 'rokiem';
    if (years % 10 >= 2 && years % 10 <= 4 && (years % 100 < 10 || years % 100 >= 20)) {
      return 'latami';
    }
    return 'latami';
  }
}
