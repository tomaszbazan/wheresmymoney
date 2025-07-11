import 'package:flutter/material.dart';

class SideMenu extends StatelessWidget {
  final bool isExpanded;
  final int selectedIndex;
  final Function(int) onItemTapped;
  final VoidCallback onToggle;

  const SideMenu({
    super.key,
    required this.isExpanded,
    required this.selectedIndex,
    required this.onItemTapped,
    required this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      width: isExpanded ? 250 : 70,
      child: Card(
        margin: EdgeInsets.zero,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.zero,
        ),
        child: Column(
          children: [
            _buildHeader(),
            _buildMenuItem(
              index: 0,
              icon: Icons.bar_chart,
              title: 'Statystyki',
            ),
            _buildMenuItem(
              index: 1,
              icon: Icons.account_balance,
              title: 'Konta',
            ),
            const Spacer(),
            // Przycisk do przełączania trybu menu
            ListTile(
              leading: Icon(
                isExpanded ? Icons.arrow_back : Icons.arrow_forward,
              ),
              title: null,
              onTap: onToggle,
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      color: Colors.blue,
      height: 100,
      width: double.infinity,
      child: Center(
        child: isExpanded
            ? const Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.account_balance_wallet,
                    color: Colors.white,
                    size: 40,
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Where\'s My Money',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              )
            : const Icon(
                Icons.account_balance_wallet,
                color: Colors.white,
                size: 30,
              ),
      ),
    );
  }

  Widget _buildMenuItem({
    required int index,
    required IconData icon,
    required String title,
  }) {
    return ListTile(
      leading: Icon(icon),
      title: isExpanded ? Text(title) : null,
      selected: selectedIndex == index,
      onTap: () => onItemTapped(index),
    );
  }
}