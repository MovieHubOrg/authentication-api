package com.authentication.api.form.user;

import com.authentication.api.validation.GenderConstraint;
import com.authentication.api.validation.PhoneConstraint;
import com.authentication.api.validation.UsernameConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class UpdateUserProfileForm {
    @UsernameConstraint(allowNull = true)
    @ApiModelProperty(name = "username")
    private String username;

    @PhoneConstraint(allowNull = true)
    @ApiModelProperty(name = "phone")
    private String phone;

    @NotBlank(message = "fullName cant not be empty")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @GenderConstraint
    @ApiModelProperty(name = "gender")
    private Integer gender;
}
