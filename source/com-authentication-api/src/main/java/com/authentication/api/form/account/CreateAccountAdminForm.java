package com.authentication.api.form.account;

import com.authentication.api.validation.EmailConstraint;
import com.authentication.api.validation.PhoneConstraint;
import com.authentication.api.validation.UsernameConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateAccountAdminForm {
    @UsernameConstraint
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @ApiModelProperty(name = "email")
    @EmailConstraint(allowNull = true)
    private String email;

    @PhoneConstraint(allowNull = true)
    @ApiModelProperty(name = "phone")
    private String phone;

    @NotEmpty(message = "password cant not be null")
    @ApiModelProperty(name = "password", required = true)
    private String password;

    @NotBlank(message = "fullName cant not be null")
    @ApiModelProperty(name = "fullName", example = "Nguyen Van A", required = true)
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @NotNull(message = "status cant not be null")
    @ApiModelProperty(name = "status", required = true)
    private Integer status;

    @NotNull(message = "groupId cant not be null")
    @ApiModelProperty(name = "groupId", required = true)
    private Long groupId;
}
