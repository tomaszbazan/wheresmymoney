package pl.btsoftware.backend.transaction.infrastructure.persistance;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import pl.btsoftware.backend.transaction.domain.TransactionSearchCriteria;

public class TransactionSpecification {

    public static Specification<TransactionEntity> from(TransactionSearchCriteria criteria, UUID groupId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("createdByGroup"), groupId));
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (criteria.types() != null && !criteria.types().isEmpty()) {
                predicates.add(root.get("type").in(criteria.types()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
