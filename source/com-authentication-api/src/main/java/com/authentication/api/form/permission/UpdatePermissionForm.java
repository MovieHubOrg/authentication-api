package com.authentication.api.form.permission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdatePermissionForm {
    @NotNull(message = "id cant not be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @NotEmpty(message = "name cant not be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotEmpty(message = "action cant not be null")
    @ApiModelProperty(name = "action", required = true)
    private String action;

    @NotNull(message = "showMenu cant not be null")
    @ApiModelProperty(name = "showMenu", required = true)
    private Boolean showMenu;

    @NotEmpty(message = "description cant not be null")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @NotEmpty(message = "pCode cant not be null")
    @ApiModelProperty(name = "pCode", required = true)
    private String permissionCode;
}
