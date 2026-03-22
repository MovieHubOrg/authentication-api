package com.authentication.api.form.sns;

import lombok.Data;

@Data
public class PushNotiRequest {
    private String message;
    private String tenantId;
    private Integer userKind;
    private Long userId;
}
