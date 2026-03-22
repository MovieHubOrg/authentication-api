package com.authentication.api.model;

import com.authentication.api.constant.DatabaseConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "permission")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Permission extends Auditable<String> {
    private String name;

    private String action;

    private Boolean showMenu;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_group_id")
    private GroupPermission groupPermission;

    @Column(name = "p_code")
    private String permissionCode;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private List<Group> groups = new ArrayList<>();
}
