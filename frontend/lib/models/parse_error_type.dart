enum ErrorType {
  invalidFileType,
  fileTooLarge,
  emptyFile,
  invalidCsvFormat,
  invalidDateFormat,
  invalidAmountFormat,
  invalidCurrency,
  currencyMismatch,
  unknownError,
  failedToParseCsv,
  invalidFile;

  static ErrorType fromJson(String? value) {
    if (value == null) return ErrorType.unknownError;

    switch (value) {
      case 'INVALID_FILE_TYPE':
        return ErrorType.invalidFileType;
      case 'FILE_TOO_LARGE':
        return ErrorType.fileTooLarge;
      case 'EMPTY_FILE':
        return ErrorType.emptyFile;
      case 'INVALID_CSV_FORMAT':
        return ErrorType.invalidCsvFormat;
      case 'INVALID_DATE_FORMAT':
        return ErrorType.invalidDateFormat;
      case 'INVALID_AMOUNT_FORMAT':
        return ErrorType.invalidAmountFormat;
      case 'INVALID_CURRENCY':
        return ErrorType.invalidCurrency;
      case 'CURRENCY_MISMATCH':
        return ErrorType.currencyMismatch;
      case 'UNKNOWN_ERROR':
        return ErrorType.unknownError;
      case 'FAILED_TO_PARSE_CSV':
        return ErrorType.failedToParseCsv;
      case 'INVALID_FILE':
        return ErrorType.invalidFile;
      default:
        return ErrorType.unknownError;
    }
  }
}
