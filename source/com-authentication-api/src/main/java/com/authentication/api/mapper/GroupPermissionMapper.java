package com.authentication.api.mapper;

import com.authentication.api.dto.permissionGroup.GroupPermissionDto;
import com.authentication.api.form.groupPermission.CreateGroupPermissionForm;
import com.authentication.api.form.groupPermission.UpdateGroupPermissionForm;
import com.authentication.api.model.GroupPermission;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupPermissionMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "ordering", target = "ordering")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToGroupPermissionDto")
    GroupPermissionDto fromEntityToGroupPermissionDto(GroupPermission groupPermission);

    @IterableMapping(elementTargetType = GroupPermissionDto.class, qualifiedByName = "fromEntityToGroupPermissionDto")
    @Named("fromEntityToGroupPermissionDtoList")
    List<GroupPermissionDto> fromEntityToGroupPermissionDtoList(List<GroupPermission> groupPermissions);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "ordering", target = "ordering")
    @BeanMapping(ignoreByDefault = true)
    GroupPermission fromCreateGroupPermissionFormToEntity(CreateGroupPermissionForm form);

    @Mapping(source = "name", target = "name")
    @BeanMapping(ignoreByDefault = true)
    void mappingUpdateGroupPermissionFormToEntity(UpdateGroupPermissionForm form, @MappingTarget GroupPermission groupPermission);
}
