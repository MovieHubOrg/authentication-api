package com.authentication.api.form.user;

import com.authentication.api.validation.EmailConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ResendOtpForm {
    @EmailConstraint
    @ApiModelProperty(required = true)
    private String email;
}
