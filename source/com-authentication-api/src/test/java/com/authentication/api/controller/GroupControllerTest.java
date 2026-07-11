package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.group.GroupDto;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.group.CreateGroupForm;
import com.authentication.api.form.group.UpdateGroupForm;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.mapper.GroupMapper;
import com.authentication.api.model.Group;
import com.authentication.api.model.criteria.GroupCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.repository.PermissionRepository;
import com.authentication.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMapper groupMapper;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private GroupController groupController;

    private BaseJwt superAdminJwt() {
        BaseJwt jwt = new BaseJwt();
        jwt.setIsSuperAdmin(true);
        return jwt;
    }

    private BaseJwt adminJwt() {
        BaseJwt jwt = new BaseJwt();
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_ADMIN);
        return jwt;
    }

    // ---------- get ----------

    @Test
    void get_whenGroupExistsAndSuperAdmin_returnsSuccessResponse() {
        Group group = new Group();
        group.setId(1L);
        group.setName("Admin");

        GroupDto groupDto = new GroupDto();
        groupDto.setName("Admin");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMapper.fromEntityToGroupDto(group)).thenReturn(groupDto);
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());

        ApiMessageDto<GroupDto> response = groupController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get group success");
        assertThat(response.getData()).isEqualTo(groupDto);
    }

    @Test
    void get_whenGroupNotFound_throwsNotFoundException() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenNotSuperAdmin_throwsUnauthorizationException() {
        Group group = new Group();
        group.setId(1L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt()); // isSuperAdmin defaults to false

        assertThatThrownBy(() -> groupController.get(1L))
                .isInstanceOf(UnauthorizationException.class);
    }

    // ---------- create ----------

    private CreateGroupForm createForm() {
        CreateGroupForm form = new CreateGroupForm();
        form.setName("Editors");
        form.setDescription("Editors group");
        form.setKind(BaseConstant.GROUP_KIND_ADMIN);
        form.setColor("#FFFFFF");
        form.setPermissions(new Long[]{});
        return form;
    }

    @Test
    void create_whenSuperAdminAndValid_returnsSuccessResponse() {
        CreateGroupForm form = createForm();
        Group group = new Group();

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(null);
        when(groupRepository.existsByKindAndColor(BaseConstant.GROUP_KIND_ADMIN, "#FFFFFF")).thenReturn(false);
        when(groupMapper.fromCreateGroupFormToEntity(form)).thenReturn(group);

        ApiMessageDto<String> response = groupController.create(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Create group success");
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void create_whenNotSuperAdmin_throwsUnauthorizationException() {
        CreateGroupForm form = createForm();
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> groupController.create(form, null))
                .isInstanceOf(UnauthorizationException.class);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void create_whenNameExists_throwsBadRequestException() {
        CreateGroupForm form = createForm();
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(new Group());

        assertThatThrownBy(() -> groupController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NAME_EXIST);
    }

    @Test
    void create_whenKindAndColorExists_throwsBadRequestException() {
        CreateGroupForm form = createForm();
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(null);
        when(groupRepository.existsByKindAndColor(BaseConstant.GROUP_KIND_ADMIN, "#FFFFFF")).thenReturn(true);

        assertThatThrownBy(() -> groupController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_COLOR_EXIST);
    }

    // ---------- update ----------

    private UpdateGroupForm updateForm() {
        UpdateGroupForm form = new UpdateGroupForm();
        form.setId(1L);
        form.setName("Editors");
        form.setDescription("desc");
        form.setColor("#FFFFFF");
        form.setPermissions(new Long[]{});
        return form;
    }

    @Test
    void update_whenSuperAdminAndValid_returnsSuccessResponse() {
        UpdateGroupForm form = updateForm();
        Group group = new Group();
        group.setId(1L);
        group.setColor("#FFFFFF"); // same as form -> color check short-circuits

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(null);

        ApiMessageDto<String> response = groupController.update(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update group success");
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void update_whenGroupNotFound_throwsNotFoundException() {
        UpdateGroupForm form = updateForm();
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupController.update(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNotSuperAdmin_throwsUnauthorizationException() {
        UpdateGroupForm form = updateForm();
        Group group = new Group();
        group.setId(1L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> groupController.update(form, null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void update_whenNameCollidesWithAnotherGroup_throwsBadRequestException() {
        UpdateGroupForm form = updateForm();
        Group group = new Group();
        group.setId(1L);

        Group other = new Group();
        other.setId(2L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(other);

        assertThatThrownBy(() -> groupController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NAME_EXIST);
    }

    @Test
    void update_whenColorCollides_throwsBadRequestException() {
        UpdateGroupForm form = updateForm();
        form.setColor("#BBBBBB");
        Group group = new Group();
        group.setId(1L);
        group.setColor("#AAAAAA");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findFirstByName("Editors")).thenReturn(null);
        when(groupRepository.existsByKindAndColor(group.getKind(), "#BBBBBB")).thenReturn(true);

        assertThatThrownBy(() -> groupController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_COLOR_EXIST);
    }

    // ---------- list ----------

    @Test
    void list_whenSuperAdmin_doesNotSetExcludeKind() {
        GroupCriteria criteria = new GroupCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> page = new PageImpl<>(Collections.singletonList(new Group()));

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(groupMapper.fromEntityToGroupDtoList(any())).thenReturn(Collections.singletonList(new GroupDto()));

        ApiMessageDto<ResponseListDto<List<GroupDto>>> response = groupController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("List group success.");
        assertThat(criteria.getExcludeKind()).isNull();
    }

    @Test
    void list_whenNotSuperAdmin_setsExcludeKindAdmin() {
        GroupCriteria criteria = new GroupCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> page = new PageImpl<>(Collections.singletonList(new Group()));

        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());
        when(groupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(groupMapper.fromEntityToGroupDtoList(any())).thenReturn(Collections.singletonList(new GroupDto()));

        ApiMessageDto<ResponseListDto<List<GroupDto>>> response = groupController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getExcludeKind()).isEqualTo(BaseConstant.GROUP_KIND_ADMIN);
    }

    // ---------- autoComplete ----------

    @Test
    void autoComplete_whenAdmin_doesNotSetExcludeKind() {
        GroupCriteria criteria = new GroupCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> page = new PageImpl<>(Collections.singletonList(new Group()));

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(groupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(groupMapper.fromEntityToGroupDtoAutoCompleteList(any())).thenReturn(Collections.singletonList(new GroupDto()));

        ApiMessageDto<ResponseListDto<List<GroupDto>>> response = groupController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getExcludeKind()).isNull();
    }

    @Test
    void autoComplete_whenNotAdmin_setsExcludeKindAdmin() {
        GroupCriteria criteria = new GroupCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> page = new PageImpl<>(Collections.singletonList(new Group()));

        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());
        when(groupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(groupMapper.fromEntityToGroupDtoAutoCompleteList(any())).thenReturn(Collections.singletonList(new GroupDto()));

        ApiMessageDto<ResponseListDto<List<GroupDto>>> response = groupController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(criteria.getExcludeKind()).isEqualTo(BaseConstant.GROUP_KIND_ADMIN);
    }

    // ---------- delete ----------

    @Test
    void delete_whenSuperAdminAndDeletable_returnsSuccessResponse() {
        Group group = new Group();
        group.setId(1L);
        group.setIsSystemRole(false);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByGroupId(1L)).thenReturn(false);

        ApiMessageDto<Void> response = groupController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete group success");
        verify(groupRepository, times(1)).delete(group);
    }

    @Test
    void delete_whenNotSuperAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> groupController.delete(1L))
                .isInstanceOf(UnauthorizationException.class);
        verify(groupRepository, never()).delete(any());
    }

    @Test
    void delete_whenGroupNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenSystemRole_throwsBadRequestException() {
        Group group = new Group();
        group.setId(1L);
        group.setIsSystemRole(true);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_CANT_DELETE);
    }

    @Test
    void delete_whenUsedByAccount_throwsBadRequestException() {
        Group group = new Group();
        group.setId(1L);
        group.setIsSystemRole(false);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByGroupId(1L)).thenReturn(true);

        assertThatThrownBy(() -> groupController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_CANT_DELETE);
    }
}
