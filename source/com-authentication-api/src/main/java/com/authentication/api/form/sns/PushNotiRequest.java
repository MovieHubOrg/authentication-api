package com.authentication.api.form.sns;

import lombok.Data;

@Data
public class PushNotiRequest {
    private Integer userKind;
    private Long userId;
    private String message;
}
