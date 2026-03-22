package com.authentication.api.dto.account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.authentication.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AccountAutoCompleteDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private long id;
    private String fullName;
    private String avatarPath;
}
