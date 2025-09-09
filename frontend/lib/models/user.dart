class User {
  final String id;
  final String email;
  final String displayName;
  final String groupId;
  final DateTime createdAt;
  final DateTime? lastLoginAt;
  final DateTime joinedGroupAt;

  const User({
    required this.id,
    required this.email,
    required this.displayName,
    required this.groupId,
    required this.createdAt,
    this.lastLoginAt,
    required this.joinedGroupAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as String,
      email: json['email'] as String,
      displayName: json['displayName'] as String,
      groupId: json['groupId'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      lastLoginAt:
          json['lastLoginAt'] != null
              ? DateTime.parse(json['lastLoginAt'] as String)
              : null,
      joinedGroupAt: DateTime.parse(json['joinedGroupAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'displayName': displayName,
      'groupId': groupId,
      'createdAt': createdAt.toIso8601String(),
      'lastLoginAt': lastLoginAt?.toIso8601String(),
      'joinedGroupAt': joinedGroupAt.toIso8601String(),
    };
  }
}
