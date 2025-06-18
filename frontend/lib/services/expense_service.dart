import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/expense.dart';

class ExpenseService {
  static const String baseUrl = 'http://localhost:8080/api';

  Future<List<Expense>> getExpenses() async {
    final response = await http.get(
      Uri.parse('$baseUrl/expenses'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final Map<String, dynamic> responseData = jsonDecode(response.body);
      
      // Check if response contains 'expenses' field
      if (responseData.containsKey('expenses')) {
        final List<dynamic> expensesJson = responseData['expenses'];
        return expensesJson.map((json) => Expense.fromJson(json)).toList();
      } else {
        // If there's no 'expenses' field, try treating the whole response as a list
        if (responseData is List) {
          return (responseData as List).map((json) => Expense.fromJson(json)).toList();
        }
        // Otherwise return an empty list
        return [];
      }
    } else {
      throw Exception('Failed to load expenses');
    }
  }

  Future<Expense> createExpense(String accountId, double amount, String description, String date, String currency) async {
    final response = await http.post(
      Uri.parse('$baseUrl/expenses'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'accountId': accountId,
        'amount': amount,
        'description': description,
        'date': date,
        'currency': currency,
      }),
    );

    if (response.statusCode == 201) {
      return Expense.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to create expense: ${response.statusCode}');
    }
  }

  Future<void> deleteExpense(String expenseId) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/expenses/$expenseId'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Failed to delete expense: ${response.statusCode}');
    }
  }
}