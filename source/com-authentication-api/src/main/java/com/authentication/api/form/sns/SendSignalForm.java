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
public class SendSignalForm {
    @NotNull(message = "applicationId cant not be null")
    @ApiModelProperty(name = "applicationId", required = true)
    private Long applicationId;

    @NotNull(message = "applicationId cant not be null")
    @ApiModelProperty(name = "applicationId", required = true)
    private Long applicationChannelId;

    @NotBlank(message = "secretKey cant not be null")
    @ApiModelProperty(name = "secretKey", required = true)
    private String secretKey;

    @NotBlank(message = "payload cant not be null")
    @ApiModelProperty(name = "payload", required = true)
    private String payload;
}
