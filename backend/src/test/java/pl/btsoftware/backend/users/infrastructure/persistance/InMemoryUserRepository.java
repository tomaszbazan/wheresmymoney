package pl.btsoftware.backend.users.infrastructure.persistance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.domain.UserRepository;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UserId, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        users.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findByGroupId(GroupId groupId) {
        return users.values().stream()
                .filter(user -> user.groupId().equals(groupId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId userId) {
        users.remove(userId);
    }

    public int size() {
        return users.size();
    }
}
