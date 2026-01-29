class BillItemCategory {
  final String id;
  final String name;

  const BillItemCategory({required this.id, required this.name});

  factory BillItemCategory.fromJson(Map<String, dynamic> json) {
    return BillItemCategory(id: json['id'] as String, name: json['name'] as String);
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'name': name};
  }
}
