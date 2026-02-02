class Money {
  final double value;
  final String currency;

  const Money({required this.value, required this.currency});

  factory Money.fromJson(Map<String, dynamic> json) {
    return Money(value: (json['value'] as num).toDouble(), currency: json['currency'] as String);
  }

  Map<String, dynamic> toJson() {
    return {'value': value, 'currency': currency};
  }

  @override
  String toString() => '${value.abs().toStringAsFixed(2)} $currency';

  @override
  bool operator ==(Object other) => identical(this, other) || other is Money && runtimeType == other.runtimeType && value == other.value && currency == other.currency;

  @override
  int get hashCode => value.hashCode ^ currency.hashCode;
}
