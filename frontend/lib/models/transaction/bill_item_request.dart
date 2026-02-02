class BillItemRequest {
  final String? categoryId;
  final double amount;
  final String description;

  const BillItemRequest({required this.categoryId, required this.amount, required this.description});

  Map<String, dynamic> toJson() {
    return {'categoryId': categoryId, 'amount': amount, 'description': description};
  }
}
