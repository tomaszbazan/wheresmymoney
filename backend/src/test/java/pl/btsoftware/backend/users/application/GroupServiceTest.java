package pl.btsoftware.backend.users.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.Group;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupInvitationRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GroupServiceTest {

    private GroupService groupService;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupInvitationRepository invitationRepository;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        groupRepository = new InMemoryGroupRepository();
        invitationRepository = new InMemoryGroupInvitationRepository();
        userRepository = new InMemoryUserRepository();
        groupService = new GroupService(groupRepository, invitationRepository, userRepository);
    }

    @Test
    void shouldFindGroupById() {
        UserId creatorId = UserId.generate();
        Group group = Group.create("Test Group", "Description", creatorId);
        groupRepository.save(group);

        Optional<Group> found = groupService.findGroupById(group.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Group");
        assertThat(found.get().getId()).isEqualTo(group.getId());
    }
}