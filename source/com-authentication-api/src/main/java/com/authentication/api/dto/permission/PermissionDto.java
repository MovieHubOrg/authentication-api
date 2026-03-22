package com.authentication.api.dto.permission;

import com.authentication.api.dto.ABasicAdminDto;
import com.authentication.api.dto.permissionGroup.GroupPermissionDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class PermissionDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "action")
    private String action;

    @ApiModelProperty(name = "showMenu")
    private Boolean showMenu;

    @ApiModelProperty(name = "description")
    private String description;

    @ApiModelProperty(name = "groupPermission")
    private GroupPermissionDto groupPermission;

    @ApiModelProperty(name = "permissionCode")
    private String permissionCode;
}
