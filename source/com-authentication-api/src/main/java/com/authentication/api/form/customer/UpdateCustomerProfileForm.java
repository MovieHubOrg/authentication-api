package com.authentication.api.form.customer;

import com.authentication.api.validation.EmailConstraint;
import com.authentication.api.validation.PasswordConstraint;
import com.authentication.api.validation.PhoneConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class UpdateCustomerProfileForm {
    @EmailConstraint(allowNull = true)
    @ApiModelProperty(name = "email")
    private String email;

    @PhoneConstraint(allowNull = true)
    @ApiModelProperty(name = "phone")
    private String phone;

    @ApiModelProperty(name = "oldPassword")
    private String oldPassword;

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
}
