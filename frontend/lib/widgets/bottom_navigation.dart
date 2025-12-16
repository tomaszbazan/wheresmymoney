import 'package:flutter/material.dart';

class BottomNavigation extends StatelessWidget {
  final int selectedIndex;
  final void Function(int) onItemTapped;

  const BottomNavigation({super.key, required this.selectedIndex, required this.onItemTapped});

  @override
  Widget build(BuildContext context) {
    return BottomNavigationBar(
      currentIndex: selectedIndex,
      onTap: onItemTapped,
      type: BottomNavigationBarType.fixed,
      selectedFontSize: 12,
      unselectedFontSize: 10,
      items: const [
        BottomNavigationBarItem(icon: Icon(Icons.bar_chart), label: 'Statystyki'),
        BottomNavigationBarItem(icon: Icon(Icons.account_balance), label: 'Konta'),
        BottomNavigationBarItem(icon: Icon(Icons.arrow_upward), label: 'Przychody'),
        BottomNavigationBarItem(icon: Icon(Icons.arrow_downward), label: 'Wydatki'),
        BottomNavigationBarItem(icon: Icon(Icons.category_outlined), label: 'Wydatki kat.'),
        BottomNavigationBarItem(icon: Icon(Icons.category), label: 'Przychody kat.'),
      ],
    );
  }
}
