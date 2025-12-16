import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/config/supabase_config.dart';

void main() {
  group('SupabaseConfig', () {
    group('load', () {
      test('should throw exception when SUPABASE_URL environment variable is not set', () async {
        expect(
          () async => await SupabaseConfig.load(),
          throwsA(isA<Exception>().having((e) => e.toString(), 'message', contains('Environment variables SUPABASE_URL or SUPABASE_ANON_KEY not set'))),
        );
      });
    });
  });
}
