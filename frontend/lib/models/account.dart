class Account {
  final String id;
  final String name;
  final double balance;
  final String? currency;
  final String? type;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Account({required this.id, required this.name, required this.balance, this.currency, this.type, this.createdAt, this.updatedAt});

  factory Account.fromJson(Map<String, dynamic> json) {
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
      currency: json['currency'] as String?,
      type: json['type'] as String? ?? 'Rachunek bieżący',
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'name': name, 'balance': balance, 'currency': currency, 'type': type, 'createdAt': createdAt?.toIso8601String(), 'updatedAt': updatedAt?.toIso8601String()};
  }
}
