package com.authentication.api.form.sns;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateSnsConfigForm {
    @NotNull(message = "applicationId cant not be null")
    @ApiModelProperty(name = "applicationId", required = true)
    private Long applicationId;

    @NotBlank(message = "tenantId cant not be null")
    @ApiModelProperty(name = "tenantId", required = true)
    private String tenantId;
}
