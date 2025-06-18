class Account {
  final String id;
  final String name;
  final double balance;
  final String? number;
  final String? type;
  final String? currency;

  Account({
    required this.id,
    required this.name,
    required this.balance,
    this.number,
    this.type,
    this.currency,
  });

  factory Account.fromJson(Map<String, dynamic> json) {
    // Konwersja wartości typu int do double jeśli potrzeba
    double balanceValue;
    if (json['balance'] is int) {
      balanceValue = (json['balance'] as int).toDouble();
    } else {
      balanceValue = json['balance'] as double;
    }

    return Account(
      id: json['id'] as String,
      name: json['name'] as String,
      balance: balanceValue,
      number: json['number'] as String?,
      // Domyślny typ konta, jeśli nie istnieje w odpowiedzi API
      type: json['type'] as String? ?? 'Rachunek bieżący',
      currency: json['currency'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'balance': balance,
      'number': number,
      'type': type,
    };
  }
}