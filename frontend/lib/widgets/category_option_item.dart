import 'package:flutter/material.dart';

import '../models/category.dart';

class CategoryOptionItem extends StatelessWidget {
  final Category category;
  final int level;
  final Color Function(String) parseColor;

  const CategoryOptionItem({super.key, required this.category, required this.level, required this.parseColor});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(left: level * 24.0, top: 8.0, bottom: 8.0, right: 8.0),
      child: Row(
        children: [
          Container(width: 12, height: 12, decoration: BoxDecoration(color: parseColor(category.color), shape: BoxShape.circle)),
          const SizedBox(width: 8),
          Expanded(child: Text(category.name, overflow: TextOverflow.ellipsis)),
        ],
      ),
    );
  }
}
