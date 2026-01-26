import 'package:flutter/material.dart';

import '../models/category_type.dart';
import '../models/transaction_type.dart';
import '../widgets/bottom_navigation.dart';
import '../widgets/keyboard_navigation_wrapper.dart';
import '../widgets/logout_dialog.dart';
import '../widgets/side_menu.dart';
import 'accounts_page.dart';
import 'audit_trail_page.dart';
import 'categories_page.dart';
import 'statistics_page.dart';
import 'transaction_page.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> with SingleTickerProviderStateMixin {
  int _selectedIndex = 0;
  bool _isMenuExpanded = true;
  late TabController _tabController;
  late FocusNode _focusNode;

  final List<Widget> _pages = <Widget>[
    const StatisticsPage(),
    const AccountsPage(),
    const TransactionsPage(type: TransactionType.expense),
    const TransactionsPage(type: TransactionType.income),
    const CategoriesPage(transactionType: CategoryType.expense),
    const CategoriesPage(transactionType: CategoryType.income),
    const AuditTrailPage(),
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _pages.length, vsync: this);
    _focusNode = FocusNode();
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
    _focusNode.dispose();
    super.dispose();
  }

  void _toggleMenu() {
    setState(() {
      _isMenuExpanded = !_isMenuExpanded;
    });
  }

  bool _isMobile(BuildContext context) {
    return MediaQuery.of(context).size.width < 800;
  }

  @override
  Widget build(BuildContext context) {
    final isMobile = _isMobile(context);

    if (isMobile) {
      return KeyboardNavigationWrapper(
        focusNode: _focusNode,
        onTabSelected: _onItemTapped,
        child: Scaffold(
          appBar: AppBar(
            title: const Text('Where\'s My Money'),
            actions: [IconButton(icon: const Icon(Icons.logout, color: Colors.red), onPressed: () => _showLogoutDialog(context))],
          ),
          body: TabBarView(controller: _tabController, physics: const NeverScrollableScrollPhysics(), children: _pages),
          bottomNavigationBar: BottomNavigation(selectedIndex: _selectedIndex, onItemTapped: _onItemTapped),
        ),
      );
    }

    return KeyboardNavigationWrapper(
      focusNode: _focusNode,
      onTabSelected: _onItemTapped,
      child: Scaffold(
        body: Row(
          children: [
            SideMenu(isExpanded: _isMenuExpanded, selectedIndex: _selectedIndex, onItemTapped: _onItemTapped, onToggle: _toggleMenu, isFocused: _focusNode.hasFocus),
            Expanded(child: TabBarView(controller: _tabController, physics: const NeverScrollableScrollPhysics(), children: _pages)),
          ],
        ),
      ),
    );
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog<void>(context: context, builder: (context) => const LogoutDialog());
  }
}
