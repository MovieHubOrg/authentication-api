package com.authentication.api.repository;

import com.authentication.api.model.GroupPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long>, JpaSpecificationExecutor<GroupPermission> {
    GroupPermission findFirstByName(String name);

    boolean existsByName(String name);
}
