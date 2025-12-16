import 'package:frontend/services/auth_service.dart';

class FakeAuthService implements AuthService {
  final String? _fakeToken;

  FakeAuthService({String? token}) : _fakeToken = token ?? 'fake-jwt-token';

  @override
  Future<String?> getAccessToken() async => _fakeToken;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
