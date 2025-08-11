# Users Management Function

## Overview
The User Management system provides comprehensive user authentication, authorization, and group management for the personal finance management application. The solution prioritizes external service integration to minimize backend complexity while maintaining a generous free tier for users.

## Architectural Approach

### External Service Strategy
The system leverages external authentication services to handle the majority of user management complexity, keeping the internal codebase focused on business logic. This approach provides:

- **Cost Efficiency**: Generous free tiers reduce operational costs
- **Security**: Proven authentication providers with enterprise-grade security
- **Scalability**: External services handle scaling challenges
- **Maintenance**: Reduced internal security and compliance overhead

### Simplified Group Model
The system implements a simplified group structure where:
- **1:1 User-Group Relationship**: Each user belongs to exactly one group
- **Minimum Group Size**: Every group must contain at least one user
- **Equal Access Rights**: All group members are administrators with full access
- **Automatic Cleanup**: Groups are automatically deleted when the last member leaves

### Recommended External Provider: Supabase

**Primary Choice: Supabase**
- **Free Tier**: 100,000 Monthly Active Users (MAU)
- **Features**: Complete authentication suite, PostgreSQL database, real-time subscriptions
- **Integration**: Native SDKs for Flutter frontend and Java backend
- **Open Source**: Self-hosting option for enterprise needs

**Alternative: Clerk**
- **Free Tier**: 10,000 MAU with 24+ hour activity requirement
- **Features**: Superior developer experience, advanced user management UI
- **Best for**: B2B SaaS applications with higher customer lifetime value

## Domain Model

### User Entity
```java
public class User {
    private UserId id;
    private String externalAuthId; // Supabase user ID
    private String email;
    private String displayName;
    private GroupId groupId; // Single group membership
    private Instant createdAt;
    private Instant lastLoginAt;
    private Instant joinedGroupAt;
}
```

### Group Entity
```java
public class Group {
    private GroupId id;
    private String name;
    private String description;
    private Set<UserId> memberIds;
    private UserId createdBy; // Original creator
    private Instant createdAt;
}
```

### Group Invitation Entity
```java
public class GroupInvitation {
    private GroupInvitationId id;
    private GroupId groupId;
    private String inviteeEmail;
    private String invitationToken; // Secure token for joining
    private UserId invitedBy;
    private InvitationStatus status; // PENDING, ACCEPTED, EXPIRED
    private Instant createdAt;
    private Instant expiresAt;
}
```

### Group Constraints
```java
public class GroupConstraints {
    // Business rules:
    // 1. Each user belongs to exactly one group
    // 2. Each group must have at least one user
    // 3. All group members are administrators
    // 4. When last user leaves group, group is deleted
    // 5. Group is created automatically with user registration
    // 6. Users can join existing groups via invitation tokens
}
```

## Core Features

### 1. User Authentication (External)
- **Email/Password Authentication**: Handled by Supabase
- **Social Logins**: Google, GitHub, Apple integration
- **Magic Links**: Passwordless authentication
- **Multi-Factor Authentication**: TOTP, SMS backup
- **Password Recovery**: Automated secure flows

### 2. User Management (Hybrid)
- **Profile Management**: Basic info stored externally, finance-specific data internally
- **Account Linking**: Connect multiple authentication methods
- **Session Management**: JWT tokens with refresh mechanism
- **Audit Logging**: Track authentication events

### 3. Group Management (Internal)
- **Automatic Group Creation**: Every user gets a group created during registration
- **Invitation System**: Secure token-based invitations via email
- **Group Transfer**: Users can switch groups by accepting invitations
- **Equal Administrator Rights**: All group members have full administrative access
- **Group Lifecycle**: Automatic group deletion when last member leaves

### 4. Authorization System
- **Group-Based Permissions**: All financial data is shared within the group
- **Administrator Access**: Every group member has full read/write access
- **Feature Toggles**: Progressive feature rollout capability
- **Data Isolation**: Strict tenant separation between different groups

## Integration Architecture

### Backend Integration
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping("/register")
    public ResponseEntity<UserView> registerUser(
            @RequestBody RegisterUserRequest request,
            @RequestParam(required = false) String invitationToken) {
        // 1. Validate Supabase user exists
        // 2. If invitationToken provided, join existing group
        // 3. Otherwise, create new group with user-provided name
        // 4. Create user with group assignment
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserProfileView> getUserProfile(@AuthenticationPrincipal User user) {
        // Return user data including group info and member list
    }
}

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    
    @PostMapping("/invite")
    public ResponseEntity<GroupInvitationView> inviteToGroup(
            @AuthenticationPrincipal User user,
            @RequestBody InviteToGroupRequest request) {
        // 1. Create invitation with secure token
        // 2. Send email with invitation link
        // 3. Set expiration (e.g., 7 days)
    }
    
    @GetMapping("/invitation/{token}")
    public ResponseEntity<GroupInvitationView> getInvitationDetails(@PathVariable String token) {
        // Return group name and inviter info for invitation acceptance page
    }
    
    @PostMapping("/invitation/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal User user) {
        // 1. Validate token and expiration
        // 2. Transfer user from current group to invited group
        // 3. Delete previous group if it becomes empty
        // 4. Mark invitation as accepted
    }
    
    @GetMapping("/my")
    public ResponseEntity<GroupView> getMyGroup(@AuthenticationPrincipal User user) {
        // Return current user's group with all members
    }
    
    @PutMapping("/my")
    public ResponseEntity<GroupView> updateMyGroup(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateGroupRequest request) {
        // Update group name and description
    }
}
```

### Frontend Integration (Flutter)
```dart
class AuthService {
  final SupabaseClient _supabase = Supabase.instance.client;
  
  Future<AuthResponse> signUp({
    required String email, 
    required String password,
    required String displayName,
    required String groupName,
    String? invitationToken
  }) async {
    // 1. Register user with Supabase
    final response = await _supabase.auth.signUp(
      email: email,
      password: password,
    );
    
    if (response.user != null) {
      // 2. Register user in backend with group creation/joining
      await _registerUserInBackend(
        displayName: displayName,
        groupName: groupName,
        invitationToken: invitationToken,
      );
    }
    
    return response;
  }
  
  Future<AuthResponse> signInWithEmail(String email, String password) async {
    final response = await _supabase.auth.signInWithPassword(
      email: email,
      password: password,
    );
    
    return response;
  }
}

class GroupService {
  Future<GroupInvitation> sendInvitation(String email) async {
    final response = await http.post('/api/groups/invite', 
      body: {'email': email});
    return GroupInvitation.fromJson(response.data);
  }
  
  Future<GroupInvitationDetails> getInvitationDetails(String token) async {
    final response = await http.get('/api/groups/invitation/$token');
    return GroupInvitationDetails.fromJson(response.data);
  }
  
  Future<void> acceptInvitation(String token) async {
    await http.post('/api/groups/invitation/$token/accept');
  }
}
```

## Security Considerations

### Authentication Security
- **JWT Validation**: Backend validates Supabase JWT tokens
- **Token Refresh**: Automatic token renewal mechanism
- **Session Timeout**: Configurable session expiration
- **Device Management**: Track and revoke device sessions

### Authorization Security
- **Group-Based Access**: All group members have equal access rights
- **Resource Sharing**: All financial data is shared within the group
- **Group Isolation**: Strict boundaries between different groups
- **Audit Trails**: Track all administrative actions within groups

### Data Protection
- **PII Minimization**: Store minimal personal data internally
- **Data Encryption**: At-rest and in-transit encryption
- **GDPR Compliance**: Data deletion and export capabilities
- **Backup Security**: Encrypted backup storage

## Implementation Strategy

### Phase 1: Basic Authentication
1. **Supabase Setup**: Configure authentication providers
2. **Backend Integration**: JWT validation middleware
3. **Frontend Integration**: Login/logout flows
4. **User Sync**: Basic user data synchronization

### Phase 2: User Management
1. **Profile Management**: Extended user properties
2. **Account Settings**: Preferences and configuration
3. **Security Settings**: MFA, password changes
4. **Data Export**: GDPR compliance features

### Phase 3: Group Management
1. **Automatic Group Creation**: Groups created during user registration
2. **Invitation System**: Token-based email invitations with expiration
3. **Group Transfer**: Seamless switching between groups via invitation acceptance
4. **Group Lifecycle**: Automatic cleanup when group becomes empty

### Phase 4: Advanced Features
1. **Fine-Grained Permissions**: Resource-level access control
2. **Advanced Analytics**: User behavior insights
3. **Enterprise Features**: SSO, SCIM provisioning
4. **Mobile Features**: Biometric authentication

## Free Tier Optimization

### Supabase Free Tier Benefits
- **100K MAU**: Supports significant user growth
- **500MB Database**: Adequate for user metadata
- **5GB Bandwidth**: Sufficient for authentication traffic
- **Real-time**: WebSocket connections for live updates

### Cost Management Strategies
1. **Efficient Data Usage**: Minimize database storage
2. **Bandwidth Optimization**: Compress authentication responses
3. **Caching Strategy**: Reduce external API calls
4. **Monitoring**: Track usage against limits

## Migration Path

### From Free to Paid Tiers
- **Supabase Pro**: $25/month for additional features
- **Enterprise Features**: Custom pricing for large organizations
- **Self-Hosting Option**: Full control with Supabase open-source

### Service Provider Migration
- **Abstraction Layer**: Authentication service interface
- **Data Export**: User data portability
- **Configuration-Based**: Easy provider switching
- **Testing Strategy**: Comprehensive integration tests

## Success Metrics

### User Experience
- **Authentication Success Rate**: >99.9%
- **Login Time**: <2 seconds average
- **Password Reset Success**: >95%
- **User Satisfaction**: NPS >8.0

### Technical Performance
- **API Response Time**: <500ms p95
- **System Availability**: 99.9% uptime
- **Security Incidents**: Zero breaches
- **Cost Efficiency**: <$0.10 per MAU

## Conclusion

This user management solution balances cost efficiency, security, and developer experience by leveraging Supabase's generous free tier while maintaining control over business-critical user data. The hybrid approach ensures scalability while keeping implementation complexity manageable.

The architecture supports future growth from individual users to family groups and eventually enterprise customers, providing a clear path for monetization and feature expansion.
