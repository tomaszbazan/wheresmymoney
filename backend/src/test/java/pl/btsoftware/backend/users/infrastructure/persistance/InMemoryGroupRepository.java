package pl.btsoftware.backend.users.infrastructure.persistance;

import pl.btsoftware.backend.users.domain.Group;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.GroupRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryGroupRepository implements GroupRepository {
    private final Map<GroupId, Group> groups = new HashMap<>();

    @Override
    public Group save(Group group) {
        groups.put(group.getId(), group);
        return group;
    }

    @Override
    public Optional<Group> findById(GroupId groupId) {
        return Optional.ofNullable(groups.get(groupId));
    }

    @Override
    public Optional<Group> findByName(String name) {
        return groups.values().stream()
                .filter(group -> group.getName().equals(name))
                .findFirst();
    }

    @Override
    public void deleteById(GroupId groupId) {
        groups.remove(groupId);
    }

    @Override
    public boolean existsById(GroupId groupId) {
        return groups.containsKey(groupId);
    }

    public void clear() {
        groups.clear();
    }

    public int size() {
        return groups.size();
    }
}