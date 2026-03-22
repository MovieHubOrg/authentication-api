package com.authentication.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class RequestInfoWrapperDto {

    private String action;
    private String username;
    private String deviceApiKey;
    private String deviceSecretKey;
}
