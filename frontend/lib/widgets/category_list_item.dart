import 'package:flutter/material.dart';

import '../utils/category_hierarchy.dart';

class CategoryListItem extends StatelessWidget {
  final CategoryWithLevel categoryWithLevel;
  final VoidCallback? onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;

  const CategoryListItem({super.key, required this.categoryWithLevel, this.onTap, this.onEdit, this.onDelete});

  @override
  Widget build(BuildContext context) {
    final category = categoryWithLevel.category;
    final level = categoryWithLevel.level;
    final indentWidth = level * 24.0; // 24px wcięcia na poziom

    return Container(
      margin: EdgeInsets.only(left: indentWidth, bottom: 8),
      child: Card(
        elevation: level == 0 ? 2 : 1, // Kategorie główne bardziej wyróżnione
        child: ListTile(
          leading: Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(color: Color(int.parse(category.color.substring(1), radix: 16) + 0xFF000000), shape: BoxShape.circle),
            child: level > 0 ? const Icon(Icons.subdirectory_arrow_right, color: Colors.white, size: 20) : null,
          ),
          title: Text(category.name, style: TextStyle(fontWeight: level == 0 ? FontWeight.bold : FontWeight.normal, fontSize: level == 0 ? 16 : 14)),
          subtitle: level > 0 ? Text('Podkategoria', style: TextStyle(color: Colors.grey[600], fontSize: 12)) : null,
          onTap: onTap,
          trailing: PopupMenuButton<String>(
            onSelected: (value) {
              switch (value) {
                case 'edit':
                  onEdit?.call();
                  break;
                case 'delete':
                  onDelete?.call();
                  break;
              }
            },
            itemBuilder:
                (context) => [
                  const PopupMenuItem(value: 'edit', child: Row(children: [Icon(Icons.edit), SizedBox(width: 8), Text('Edytuj')])),
                  const PopupMenuItem(value: 'delete', child: Row(children: [Icon(Icons.delete, color: Colors.red), SizedBox(width: 8), Text('Usuń')])),
                ],
          ),
        ),
      ),
    );
  }
}
