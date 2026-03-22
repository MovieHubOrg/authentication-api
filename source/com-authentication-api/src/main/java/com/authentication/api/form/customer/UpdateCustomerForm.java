package com.authentication.api.form.customer;

import com.authentication.api.validation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateCustomerForm {
    @NotNull(message = "id cant not be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @UsernameConstraint
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @PhoneConstraint(allowNull = true)
    @ApiModelProperty(name = "phone")
    private String phone;

    @EmailConstraint(allowNull = true)
    @ApiModelProperty(name = "email")
    private String email;

    @PasswordConstraint(message = "newPassword invalid format", allowNull = true)
    @ApiModelProperty(name = "newPassword")
    private String newPassword;

    @NotBlank(message = "fullName cannot be null")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @ApiModelProperty(name = "logoPath")
    private String logoPath;

    @StatusConstraint
    @ApiModelProperty(name = "status")
    private Integer status;
}
