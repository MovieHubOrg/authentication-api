package com.authentication.api.dto.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingDto {
    private String groupName;
    private String description;
    private String keyName;
    private String valueData;
    private String dataType;
    private String options;
    private Boolean isSystem;
}
