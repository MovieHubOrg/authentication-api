package com.authentication.api.form.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountEventForm {
    private Long accountId;
    private Integer kind;
    private Integer status;
    private String username;
    private String phone;
    private String email;
    private String fullName;
    private String avatarPath;
    private Boolean isSuperAdmin;
}
