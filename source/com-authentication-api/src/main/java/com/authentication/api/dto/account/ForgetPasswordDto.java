package com.authentication.api.dto.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ForgetPasswordDto {
    @ApiModelProperty(name = "idHash")
    private String idHash;
}
