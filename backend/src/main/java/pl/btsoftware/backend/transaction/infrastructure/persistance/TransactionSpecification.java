package pl.btsoftware.backend.transaction.infrastructure.persistance;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.transaction.domain.TransactionSearchCriteria;

public class TransactionSpecification {

    public static Specification<TransactionEntity> from(TransactionSearchCriteria criteria, UUID groupId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("createdByGroup"), groupId));
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (!criteria.types().isEmpty()) {
                predicates.add(root.get("type").in(criteria.types()));
            }

            if (criteria.dateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), criteria.dateFrom()));
            }

            if (criteria.dateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), criteria.dateTo()));
            }

            if (criteria.minAmount() != null) {
                var expression = criteriaBuilder
                        .function(
                                "jsonb_extract_path_text",
                                String.class,
                                root.get("bill"),
                                criteriaBuilder.literal("totalAmount"))
                        .as(BigDecimal.class);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(expression, criteria.minAmount()));
            }

            if (criteria.maxAmount() != null) {
                var expression = criteriaBuilder
                        .function(
                                "jsonb_extract_path_text",
                                String.class,
                                root.get("bill"),
                                criteriaBuilder.literal("totalAmount"))
                        .as(BigDecimal.class);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(expression, criteria.maxAmount()));
            }

            if (!criteria.accountIds().isEmpty()) {
                var accountIds = criteria.accountIds().stream().map(AccountId::value).toList();
                predicates.add(root.get("accountId").in(accountIds));
            }

            if (criteria.description() != null && !criteria.description().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bill").as(String.class)),
                        "%" + criteria.description().toLowerCase() + "%"));
            }

            if (!criteria.categoryIds().isEmpty()) {
                var categoryPredicates = new ArrayList<Predicate>();
                for (var catId : criteria.categoryIds()) {
                    var pathExistsExpression = criteriaBuilder.function(
                            "jsonb_path_exists",
                            Boolean.class,
                            root.get("bill"),
                            criteriaBuilder.literal("$.items[*].categoryId ? (@ == \"" + catId.value() + "\")"));
                    categoryPredicates.add(criteriaBuilder.isTrue(pathExistsExpression));
                }
                predicates.add(criteriaBuilder.or(categoryPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
