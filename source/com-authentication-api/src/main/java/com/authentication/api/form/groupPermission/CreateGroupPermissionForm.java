package com.authentication.api.form.groupPermission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateGroupPermissionForm {
    @NotEmpty(message = "name cant not be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotNull(message = "ordering cant not be null")
    @ApiModelProperty(name = "ordering", required = true)
    private Integer ordering;
}
