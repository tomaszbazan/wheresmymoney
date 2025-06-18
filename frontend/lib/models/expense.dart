class Expense {
  final String id;
  final String accountId;
  final double amount;
  final String description;
  final String date;
  final String currency;

  Expense({
    required this.id,
    required this.accountId,
    required this.amount,
    required this.description,
    required this.date,
    required this.currency,
  });

  factory Expense.fromJson(Map<String, dynamic> json) {
    return Expense(
      id: json['id'],
      accountId: json['accountId'],
      amount: json['amount'].toDouble(),
      description: json['description'],
      date: json['date'],
      currency: json['currency'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'accountId': accountId,
      'amount': amount,
      'description': description,
      'date': date,
      'currency': currency,
    };
  }
}