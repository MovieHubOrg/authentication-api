package com.authentication.api.dto.account;

import com.authentication.api.validation.EmailConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RequestForgetPasswordForm {
    @EmailConstraint
    @ApiModelProperty(name = "email", required = true)
    private String email;
}
