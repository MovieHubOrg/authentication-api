package com.authentication.api.model.criteria;

import com.authentication.api.model.Account;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class AccountCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private int kind;
    private String keyword;
    private String username;
    private Integer status;
    private String email;
    private String fullName;
    private String phone;
    private Long groupId;

    public Specification<Account> getSpecification() {
        return new Specification<Account>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getKeyword() != null) {
                    String likePattern = "%" + getKeyword().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("username")), likePattern),
                            cb.like(cb.lower(root.get("fullName")), likePattern),
                            cb.like(cb.lower(root.get("email")), likePattern)
                    ));
                }
                if (getKind() > 0) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (!StringUtils.isEmpty(getUsername())) {
                    predicates.add(cb.like(cb.lower(root.get("username")), "%" + getUsername().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), "%" + getEmail().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getFullName())) {
                    predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + getFullName().toLowerCase() + "%"));
                }
                if (getGroupId() != null) {
                    predicates.add(cb.equal(root.get("group").get("id"), getGroupId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
