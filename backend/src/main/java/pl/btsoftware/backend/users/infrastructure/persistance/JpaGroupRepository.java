package pl.btsoftware.backend.users.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.users.domain.Group;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.GroupRepository;

import java.util.Optional;

@Repository
public class JpaGroupRepository implements GroupRepository {
    private final GroupJpaRepository jpaRepository;

    public JpaGroupRepository(GroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Group save(Group group) {
        GroupEntity entity = GroupEntity.from(group);
        GroupEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Group> findById(GroupId groupId) {
        return jpaRepository.findById(groupId.getValue())
            .map(GroupEntity::toDomain);
    }

    @Override
    public Optional<Group> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(GroupEntity::toDomain);
    }

    @Override
    public void deleteById(GroupId groupId) {
        jpaRepository.deleteById(groupId.getValue());
    }

    @Override
    public boolean existsById(GroupId groupId) {
        return jpaRepository.existsById(groupId.getValue());
    }
}