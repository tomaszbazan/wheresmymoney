package pl.btsoftware.backend.users.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId userId);
    Optional<User> findByExternalAuthId(String externalAuthId);
    Optional<User> findByEmail(String email);
    List<User> findByGroupId(GroupId groupId);
    void deleteById(UserId userId);
    boolean existsByExternalAuthId(String externalAuthId);
}