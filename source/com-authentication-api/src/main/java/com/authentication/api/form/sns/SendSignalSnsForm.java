package com.authentication.api.form.sns;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class SendSignalSnsForm {
    @NotBlank
    @ApiModelProperty(name = "payload", required = true)
    private String payload;

    private Long userId;

    private String tenantId;
}
