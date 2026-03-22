package com.authentication.api.repository;

import com.authentication.api.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findAllByIdIn(List<Long> ids);

    @Modifying
    @Transactional
    @Query("DELETE FROM Permission p where p.groupPermission.id = :groupPermissionId")
    void deleteByGroupPermissionId(@Param("groupPermissionId") Long groupPermissionId);

    boolean existsByPermissionCode(String permissionCode);

    boolean existsByNameAndGroupPermissionId(String name, Long groupPermissionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM db_permission_group WHERE permission_id = :permissionId", nativeQuery = true)
    void deletePermissionGroupById(@Param("permissionId") Long permissionId);
}
