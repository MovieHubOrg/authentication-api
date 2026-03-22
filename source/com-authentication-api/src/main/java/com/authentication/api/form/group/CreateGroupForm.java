package com.authentication.api.form.group;

import com.authentication.api.validation.GroupKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateGroupForm {
    @NotEmpty(message = "name cant not be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotEmpty(message = "description cant not be null")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @NotNull(message = "permissions cant not be null")
    @ApiModelProperty(name = "permissions", required = true)
    private Long[] permissions;

    @GroupKind
    @ApiModelProperty(name = "kind", required = true)
    private Integer kind;
}
