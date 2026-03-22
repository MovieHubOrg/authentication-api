package com.authentication.api.mapper;

import com.authentication.api.dto.user.UserDto;
import com.authentication.api.form.user.UpdateUserForm;
import com.authentication.api.form.user.UpdateUserProfileForm;
import com.authentication.api.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "account.kind", target = "kind")
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.phone", target = "phone")
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.fullName", target = "fullName")
    @Mapping(source = "account.avatarPath", target = "avatarPath")
    @Mapping(source = "account.status", target = "status")
    @Mapping(source = "gender", target = "gender")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToUserDto")
    UserDto entityToUserDto(User user);

    @IterableMapping(elementTargetType = UserDto.class, qualifiedByName = "entityToUserDto")
    List<UserDto> fromEntityToUserDtoList(List<User> users);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "account.kind", target = "kind")
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.phone", target = "phone")
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.fullName", target = "fullName")
    @Mapping(source = "account.avatarPath", target = "avatarPath")
    @Mapping(source = "gender", target = "gender")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToUserDtoProfile")
    UserDto fromEntityToUserDtoProfile(User user);
//
//    @Mapping(source = "status", target = "status")
//    @BeanMapping(ignoreByDefault = true)
//    void fromUpdateUserFormToEntity(UpdateUserForm form, @MappingTarget User user);

    @Mapping(source = "gender", target = "gender")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateUserProfileFormToEntity(UpdateUserProfileForm form, @MappingTarget User user);

}
