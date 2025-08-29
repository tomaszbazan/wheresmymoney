package pl.btsoftware.backend.users.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId userId);
    List<User> findByGroupId(GroupId groupId);
    void deleteById(UserId userId);
}