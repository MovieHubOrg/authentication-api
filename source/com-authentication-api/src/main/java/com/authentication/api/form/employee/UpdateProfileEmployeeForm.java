package com.authentication.api.form.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class UpdateProfileEmployeeForm {
    @ApiModelProperty(name = "password")
    private String password;

    @NotBlank(message = "oldPassword is required")
    @ApiModelProperty(name = "oldPassword", required = true)
    private String oldPassword;

    @NotBlank(message = "fullName is required")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
}
