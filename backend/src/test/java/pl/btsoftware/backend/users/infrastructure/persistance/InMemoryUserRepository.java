package pl.btsoftware.backend.users.infrastructure.persistance;

import pl.btsoftware.backend.users.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UserId, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByExternalAuthId(String externalAuthId) {
        return users.values().stream()
            .filter(user -> user.getExternalAuthId().equals(externalAuthId))
            .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
            .filter(user -> user.getEmail().equals(email))
            .findFirst();
    }

    @Override
    public List<User> findByGroupId(GroupId groupId) {
        return users.values().stream()
            .filter(user -> user.getGroupId().equals(groupId))
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId userId) {
        users.remove(userId);
    }

    @Override
    public boolean existsByExternalAuthId(String externalAuthId) {
        return users.values().stream()
            .anyMatch(user -> user.getExternalAuthId().equals(externalAuthId));
    }

    public void clear() {
        users.clear();
    }

    public int size() {
        return users.size();
    }
}