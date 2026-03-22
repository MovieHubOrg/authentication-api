package com.authentication.api.form.sns;

import lombok.Data;

@Data
public class BaseSendSignalForm<T> {
    private String cmd;
    private T data;
}
