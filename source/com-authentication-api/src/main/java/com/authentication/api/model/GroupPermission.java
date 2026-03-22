package com.authentication.api.model;

import com.authentication.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "group_permission")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class GroupPermission extends Auditable<String> {
    @Column(unique = true)
    private String name;

    private Integer ordering;
}
