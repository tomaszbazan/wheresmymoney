import 'package:flutter/material.dart';
import '../widgets/side_menu.dart';
import 'expenses_page.dart';
import 'statistics_page.dart';
import 'accounts_page.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> with SingleTickerProviderStateMixin {
  int _selectedIndex = 0;
  bool _isMenuExpanded = true;
  late TabController _tabController;
  
  final List<Widget> _pages = <Widget>[
    const StatisticsPage(),
    const ExpensesPage(),
    const AccountsPage(),
  ];
  
  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _pages.length, vsync: this);
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
      _tabController.animateTo(index);
    });
  }
  
  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _toggleMenu() {
    setState(() {
      _isMenuExpanded = !_isMenuExpanded;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Row(
        children: [
          // Stałe boczne menu jako osobny widget
          SideMenu(
            isExpanded: _isMenuExpanded,
            selectedIndex: _selectedIndex,
            onItemTapped: _onItemTapped,
            onToggle: _toggleMenu,
          ),
          // Zawartość strony
          Expanded(
            child: TabBarView(
              controller: _tabController,
              physics: const NeverScrollableScrollPhysics(), // Blokujemy przesuwanie palcem
              children: _pages,
            ),
          ),
        ],
      ),
    );
  }
}