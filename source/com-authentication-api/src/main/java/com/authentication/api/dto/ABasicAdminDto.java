package com.authentication.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class ABasicAdminDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @ApiModelProperty(name = "id")
    private Long id;

    @ApiModelProperty(name = "status")
    private Integer status;

    @ApiModelProperty(name = "modifiedDate")
    private LocalDateTime modifiedDate;

    @ApiModelProperty(name = "createdDate")
    private LocalDateTime createdDate;
}
