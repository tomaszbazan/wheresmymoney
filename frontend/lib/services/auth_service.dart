import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../config/api_config.dart';
import '../models/user.dart' as app_user;

class AuthService {
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'current_user';

  final SupabaseClient _supabase = Supabase.instance.client;

  Future<app_user.User?> getCurrentUser() async {
    try {
      final session = _supabase.auth.currentSession;
      if (session?.user == null) return null;

      final prefs = await SharedPreferences.getInstance();
      final userJson = prefs.getString(_userKey);
      
      if (userJson != null) {
        return app_user.User.fromJson(jsonDecode(userJson));
      }

      return _fetchUserFromBackend();
    } catch (e) {
      return null;
    }
  }

  Future<String?> getAccessToken() async {
    final session = _supabase.auth.currentSession;
    return session?.accessToken;
  }

  Future<void> signUp({
    required String email,
    required String password,
    required String displayName,
    required String groupName,
    String? invitationToken,
  }) async {
    final response = await _supabase.auth.signUp(
      email: email,
      password: password,
      data: {
        'display_name': displayName,
      },
    );

    if (response.user == null) {
      throw Exception('Failed to create user account');
    }

    await _registerUserWithoutToken(
      externalAuthId: response.user!.id,
      email: email,
      displayName: displayName,
      groupName: groupName,
      invitationToken: invitationToken,
    );
  }

  Future<app_user.User> signInWithEmail(String email, String password) async {
    try {
      final response = await _supabase.auth.signInWithPassword(
        email: email,
        password: password,
      );

      if (response.user == null) {
        throw Exception('Invalid email or password');
      }

      final user = await _fetchUserFromBackend();
      await _saveUserLocally(user);
      return user;
    } on AuthException {
      throw Exception('Invalid email or password');
    } catch (e) {
      throw Exception('Invalid email or password');
    }
  }

  Future<void> signOut() async {
    await _supabase.auth.signOut();
    
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
  }

  Future<void> _registerUserWithoutToken({
    required String externalAuthId,
    required String email,
    required String displayName,
    required String groupName,
    String? invitationToken,
  }) async {
    final url = Uri.parse('${ApiConfig.backendUrl}/users/register');
    final body = {
      'externalAuthId': externalAuthId,
      'email': email,
      'displayName': displayName,
      'groupName': groupName,
      if (invitationToken != null) 'invitationToken': invitationToken,
    };

    final response = await http.post(
      url,
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode(body),
    );

    if (response.statusCode != 201) {
      throw Exception('Failed to register user: ${response.body}');
    }
  }

  Future<app_user.User> _fetchUserFromBackend() async {
    final token = await getAccessToken();
    if (token == null) throw Exception('No access token available');

    final url = Uri.parse('${ApiConfig.backendUrl}/users/profile');
    
    final response = await http.get(
      url,
      headers: {
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to fetch user profile: ${response.body}');
    }

    return app_user.User.fromJson(jsonDecode(response.body));
  }

  Future<void> _saveUserLocally(app_user.User user) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userKey, jsonEncode(user.toJson()));
  }

  Stream<AuthState> get authStateChanges => _supabase.auth.onAuthStateChange;

  bool get isSignedIn => _supabase.auth.currentUser != null;
}