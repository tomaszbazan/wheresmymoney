package pl.btsoftware.backend.users.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.users.application.GroupService;
import pl.btsoftware.backend.users.application.UserService;
import pl.btsoftware.backend.users.domain.GroupInvitationRepository;
import pl.btsoftware.backend.users.domain.GroupRepository;
import pl.btsoftware.backend.users.domain.UserRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.JpaGroupInvitationRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.JpaGroupRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.JpaUserRepository;

@Configuration
public class UsersModuleConfiguration {

    @Bean
    public UserRepository userRepository(JpaUserRepository jpaUserRepository) {
        return jpaUserRepository;
    }

    @Bean
    public GroupRepository groupRepository(JpaGroupRepository jpaGroupRepository) {
        return jpaGroupRepository;
    }

    @Bean
    public GroupInvitationRepository groupInvitationRepository(JpaGroupInvitationRepository jpaGroupInvitationRepository) {
        return jpaGroupInvitationRepository;
    }

    @Bean
    public UserService userService(UserRepository userRepository,
                                   GroupRepository groupRepository,
                                   GroupInvitationRepository groupInvitationRepository) {
        return new UserService(userRepository, groupRepository, groupInvitationRepository);
    }

    @Bean
    public GroupService groupService(GroupRepository groupRepository,
                                     GroupInvitationRepository groupInvitationRepository,
                                     UserRepository userRepository) {
        return new GroupService(groupRepository, groupInvitationRepository, userRepository);
    }
}
