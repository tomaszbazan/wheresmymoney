class SupabaseConfig {
  final String url;
  final String anonKey;

  const SupabaseConfig({required this.url, required this.anonKey});

  static Future<SupabaseConfig> load() async {
    final envUrl = const String.fromEnvironment('SUPABASE_URL');
    final envAnonKey = const String.fromEnvironment('SUPABASE_ANON_KEY');

    if (envUrl.isEmpty || envAnonKey.isEmpty) {
      throw Exception(
        'Environment variables SUPABASE_URL or SUPABASE_ANON_KEY not set',
      );
    }

    return SupabaseConfig(url: envUrl, anonKey: envAnonKey);
  }
}
