package pl.btsoftware.backend.users.domain;

import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findById(GroupId groupId);

    Optional<Group>
    findByName(String name);
    void deleteById(GroupId groupId);
    boolean existsById(GroupId groupId);
}