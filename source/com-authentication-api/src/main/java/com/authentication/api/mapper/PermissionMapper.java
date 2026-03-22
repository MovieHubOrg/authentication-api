package com.authentication.api.mapper;

import com.authentication.api.dto.permission.PermissionDto;
import com.authentication.api.form.permission.CreatePermissionForm;
import com.authentication.api.form.permission.UpdatePermissionForm;
import com.authentication.api.model.Permission;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GroupPermissionMapper.class})
public interface PermissionMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @BeanMapping(ignoreByDefault = true)
    Permission fromCreatePermissionFormToEntity(CreatePermissionForm createPermissionForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @BeanMapping(ignoreByDefault = true)
    void mappingUpdatePermissionFormToEntity(UpdatePermissionForm form, @MappingTarget Permission permission);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "groupPermission", target = "groupPermission", qualifiedByName = "fromEntityToGroupPermissionDto")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDto")
    PermissionDto fromEntityToPermissionDto(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDto")
    @Named("fromEntityToPermissionDtoList")
    List<PermissionDto> fromEntityToPermissionDtoList(List<Permission> permissions);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "groupPermission", target = "groupPermission", qualifiedByName = "fromEntityToGroupPermissionDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDtoShort")
    PermissionDto fromEntityToPermissionDtoShort(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDtoShort")
    @Named("fromEntityToPermissionDtoShortList")
    List<PermissionDto> fromEntityToPermissionDtoShortList(List<Permission> permissions);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "groupPermission", target = "groupPermission", qualifiedByName = "fromEntityToGroupPermissionDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDtoAutoComplete")
    PermissionDto fromEntityToPermissionDtoAutoComplete(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDtoAutoComplete")
    @Named("fromEntityToPermissionDtoAutoCompleteList")
    List<PermissionDto> fromEntityToPermissionDtoAutoCompleteList(List<Permission> permissions);
}
