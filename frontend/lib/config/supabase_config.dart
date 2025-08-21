import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class SupabaseConfig {
  final String url;
  final String anonKey;

  const SupabaseConfig({
    required this.url,
    required this.anonKey,
  });

  factory SupabaseConfig.fromJson(Map<String, dynamic> json) {
    return SupabaseConfig(
      url: json['url'] as String,
      anonKey: json['anonKey'] as String,
    );
  }

  static Future<SupabaseConfig> load() async {
    try {
      // Try to load from environment variables first (for GitHub Actions)
      final envUrl = const String.fromEnvironment('SUPABASE_URL');
      final envAnonKey = const String.fromEnvironment('SUPABASE_ANON_KEY');
      
      if (envUrl.isNotEmpty && envAnonKey.isNotEmpty) {
        return SupabaseConfig(
          url: envUrl,
          anonKey: envAnonKey,
        );
      }

      // Try to load from assets (for development)
      final String configString = await rootBundle.loadString('assets/config/supabase.json');
      final Map<String, dynamic> configJson = jsonDecode(configString);
      return SupabaseConfig.fromJson(configJson);
    } catch (e) {
      if (kDebugMode) {
        print('Warning: Could not load Supabase config: $e');
        print('Using fallback configuration for development');
      }
      
      // Fallback for development
      return const SupabaseConfig(
        url: 'https://your-project-ref.supabase.co',
        anonKey: 'your-anon-key',
      );
    }
  }
}