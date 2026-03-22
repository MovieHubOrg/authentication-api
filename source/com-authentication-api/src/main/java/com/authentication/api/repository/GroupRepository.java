package com.authentication.api.repository;

import com.authentication.api.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
    Group findFirstByName(String name);

    Optional<Group> findFirstByKindAndIsSystemRole(Integer kind, Boolean isSystemRole);

    @Query("SELECT g  FROM Group g where g.isSystemRole = false and g.kind = :kind")
    Page<Group> findAllByKind(@Param("kind") int kind, Pageable pageable);

    @Query("SELECT DISTINCT g  FROM Group g INNER JOIN Account  a ON g.id = a.group.id JOIN g.permissions p WHERE a.id=?1 AND a.status=1 AND p.status=1 AND INSTR(?2, p.action)> 0")
    Group checkPermission(long accountId, String action);

    Group findFirstByKind(int kind);

}
