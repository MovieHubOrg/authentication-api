package com.authentication.api.dto.account;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AccountFanoutDto {
    private Long id;
    private Integer kind;
    private String username;
    private String phone;
    private String email;
    private String fullName;
    private String avatarPath;
    private Integer gender;
    private Date createdDate;
    private String createdBy;
    private Date modifiedDate;
    private String modifiedBy;
    private Integer status;
}
