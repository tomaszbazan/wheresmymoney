import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class TestSetup {
  static Future<void> initializeSupabase() async {
    try {
      // Try to access the instance to see if it's already initialized
      Supabase.instance.client;
    } catch (e) {
      // Mock shared preferences for testing
      TestWidgetsFlutterBinding.ensureInitialized();
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(
            const MethodChannel('plugins.flutter.io/shared_preferences'),
            (methodCall) async {
              if (methodCall.method == 'getAll') {
                return <String, dynamic>{};
              }
              return null;
            },
          );

      // If not initialized, initialize it
      await Supabase.initialize(
        url: 'https://test-project.supabase.co',
        anonKey: 'test-anon-key',
      );
    }
  }
}
