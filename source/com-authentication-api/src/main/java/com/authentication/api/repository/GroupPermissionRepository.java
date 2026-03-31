package com.authentication.api.repository;

import com.authentication.api.model.GroupPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long>, JpaSpecificationExecutor<GroupPermission> {
    GroupPermission findFirstByName(String name);

    boolean existsByName(String name);

    @Query("SELECT MAX(gp.ordering) FROM GroupPermission gp")
    Optional<Integer> findMaxOrdering();
}
