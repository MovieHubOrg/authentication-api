package com.authentication.api.dto.sns;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientTokenDto {
    @ApiModelProperty(name = "token")
    private String token;
}
