package com.authentication.api.form.sns;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class GetClientTokenForm {
    private Long applicationId;
    private Long applicationChannelId;
    private String secretKey;
}
