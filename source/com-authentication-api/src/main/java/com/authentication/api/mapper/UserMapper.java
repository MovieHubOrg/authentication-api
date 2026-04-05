package com.authentication.api.mapper;

import com.authentication.api.dto.user.UserDto;
import com.authentication.api.form.user.UpdateUserProfileForm;
import com.authentication.api.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GroupMapper.class})
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
    @Mapping(source = "settings", target = "settings")
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
    @Mapping(source = "settings", target = "settings")
    @Mapping(source = "isMakeSurvey", target = "isMakeSurvey")
    @Mapping(source = "account.group", target = "group", qualifiedByName = "fromEntityToGroupDtoAutoComplete")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToUserDtoProfile")
    UserDto fromEntityToUserDtoProfile(User user);

    @Mapping(source = "gender", target = "gender")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateUserProfileFormToEntity(UpdateUserProfileForm form, @MappingTarget User user);
}
