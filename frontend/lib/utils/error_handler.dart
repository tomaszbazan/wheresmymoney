import 'package:flutter/material.dart';
import 'package:frontend/models/http_exception.dart';

class ErrorHandler {
  static void showError(BuildContext context, Object error, {String? message}) {
    if (!context.mounted) {
      return;
    }

    final errorMessage = message ?? getErrorMessage(error);

    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(errorMessage), backgroundColor: Colors.red));
  }

  static String getErrorMessage(Object error) {
    if (error is HttpException) {
      return error.userFriendlyMessage;
    }

    return 'Nieoczekiwany błąd: $error';
  }
}
