package com.authentication.api.form.customer;

import com.authentication.api.validation.EmailConstraint;
import com.authentication.api.validation.PhoneConstraint;
import com.authentication.api.validation.UsernameConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@ApiModel
public class CreateCustomerForm {
    @UsernameConstraint
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @PhoneConstraint
    @ApiModelProperty(name = "phone", required = true)
    private String phone;

    @EmailConstraint(allowNull = true)
    @ApiModelProperty(name = "email")
    private String email;

    @NotEmpty(message = "password cant not be empty")
    @Size(min = 6, message = "password must be at least 6 characters")
    @ApiModelProperty(name = "password", required = true)
    private String password;

    @NotEmpty(message = "fullName cant not be empty")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @ApiModelProperty(name = "logoPath")
    private String logoPath;

    @ApiModelProperty(name = "status")
    private Integer status;
}
