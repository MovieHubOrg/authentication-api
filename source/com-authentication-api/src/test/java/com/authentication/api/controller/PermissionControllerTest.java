package com.authentication.api.controller;

import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.permission.PermissionDto;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.form.permission.CreatePermissionForm;
import com.authentication.api.form.permission.UpdatePermissionForm;
import com.authentication.api.mapper.PermissionMapper;
import com.authentication.api.model.GroupPermission;
import com.authentication.api.model.Permission;
import com.authentication.api.repository.GroupPermissionRepository;
import com.authentication.api.repository.PermissionRepository;
import com.authentication.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private GroupPermissionRepository groupPermissionRepository;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private PermissionController permissionController;

    private CreatePermissionForm createForm() {
        CreatePermissionForm form = new CreatePermissionForm();
        form.setName("View");
        form.setAction("USER_V");
        form.setShowMenu(true);
        form.setDescription("desc");
        form.setGroupPermissionId(5L);
        form.setPermissionCode("USER_V");
        return form;
    }

    private UpdatePermissionForm updateForm() {
        UpdatePermissionForm form = new UpdatePermissionForm();
        form.setId(1L);
        form.setName("View");
        form.setAction("USER_V");
        form.setShowMenu(true);
        form.setDescription("desc");
        form.setPermissionCode("USER_V");
        return form;
    }

    // ---------- create ----------

    @Test
    void create_whenValid_returnsSuccessResponse() {
        CreatePermissionForm form = createForm();
        GroupPermission groupPermission = new GroupPermission();
        groupPermission.setId(5L);
        Permission permission = new Permission();

        when(groupPermissionRepository.findById(5L)).thenReturn(Optional.of(groupPermission));
        when(permissionRepository.existsByNameAndGroupPermissionId("View", 5L)).thenReturn(false);
        when(permissionRepository.existsByPermissionCode("USER_V")).thenReturn(false);
        when(permissionMapper.fromCreatePermissionFormToEntity(form)).thenReturn(permission);

        ApiMessageDto<String> response = permissionController.create(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Create permission success");
        verify(permissionRepository, times(1)).save(permission);
    }

    @Test
    void create_whenGroupPermissionNotFound_throwsNotFoundException() {
        CreatePermissionForm form = createForm();
        when(groupPermissionRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionController.create(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenNameExists_throwsBadRequestException() {
        CreatePermissionForm form = createForm();
        GroupPermission groupPermission = new GroupPermission();
        groupPermission.setId(5L);

        when(groupPermissionRepository.findById(5L)).thenReturn(Optional.of(groupPermission));
        when(permissionRepository.existsByNameAndGroupPermissionId("View", 5L)).thenReturn(true);

        assertThatThrownBy(() -> permissionController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_NAME_EXIST);
    }

    @Test
    void create_whenPermissionCodeExists_throwsBadRequestException() {
        CreatePermissionForm form = createForm();
        GroupPermission groupPermission = new GroupPermission();
        groupPermission.setId(5L);

        when(groupPermissionRepository.findById(5L)).thenReturn(Optional.of(groupPermission));
        when(permissionRepository.existsByNameAndGroupPermissionId("View", 5L)).thenReturn(false);
        when(permissionRepository.existsByPermissionCode("USER_V")).thenReturn(true);

        assertThatThrownBy(() -> permissionController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_CODE_EXIST);
    }

    // ---------- update ----------

    @Test
    void update_whenValid_returnsSuccessResponse() {
        UpdatePermissionForm form = updateForm();
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("View");          // same as form -> name check short-circuits
        permission.setPermissionCode("USER_V"); // same as form -> code check short-circuits

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        ApiMessageDto<Void> response = permissionController.update(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update permission success");
        verify(permissionRepository, times(1)).save(permission);
    }

    @Test
    void update_whenPermissionNotFound_throwsNotFoundException() {
        UpdatePermissionForm form = updateForm();
        when(permissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionController.update(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNameCollides_throwsBadRequestException() {
        UpdatePermissionForm form = updateForm();
        form.setName("NewName");
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("OldName");
        GroupPermission gp = new GroupPermission();
        gp.setId(5L);
        permission.setGroupPermission(gp);

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(permissionRepository.existsByNameAndGroupPermissionId("NewName", 5L)).thenReturn(true);

        assertThatThrownBy(() -> permissionController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_NAME_EXIST);
    }

    @Test
    void update_whenPermissionCodeCollides_throwsBadRequestException() {
        UpdatePermissionForm form = updateForm();
        form.setPermissionCode("NEW_CODE");
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("View");           // same as form -> name check short-circuits
        permission.setPermissionCode("OLD_CODE");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(permissionRepository.existsByPermissionCode("NEW_CODE")).thenReturn(true);

        assertThatThrownBy(() -> permissionController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_CODE_EXIST);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponse() {
        Page<Permission> page = new PageImpl<>(Collections.singletonList(new Permission()));
        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(permissionMapper.fromEntityToPermissionDtoList(any())).thenReturn(Collections.singletonList(new PermissionDto()));

        ApiMessageDto<ResponseListDto<List<PermissionDto>>> response = permissionController.list();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get permissions list success");
        assertThat(response.getData().getContent()).hasSize(1);
    }

    // ---------- listByIds ----------

    @Test
    void listByIds_returnsSuccessResponse() {
        List<Long> ids = List.of(1L, 2L);
        List<Permission> permissions = Collections.singletonList(new Permission());
        List<PermissionDto> dtos = Collections.singletonList(new PermissionDto());

        when(permissionRepository.findAllByIdIn(ids)).thenReturn(permissions);
        when(permissionMapper.fromEntityToPermissionDtoAutoCompleteList(permissions)).thenReturn(dtos);

        ApiMessageDto<List<PermissionDto>> response = permissionController.listByIds(ids);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get permissions list success");
        assertThat(response.getData()).isEqualTo(dtos);
    }

    // ---------- delete ----------

    @Test
    void delete_whenPermissionExists_returnsSuccessResponse() {
        Permission permission = new Permission();
        permission.setId(1L);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        ApiMessageDto<Void> response = permissionController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete permission success");
        verify(permissionRepository, times(1)).deletePermissionGroupById(1L);
        verify(permissionRepository, times(1)).delete(permission);
    }

    @Test
    void delete_whenPermissionNotFound_throwsNotFoundException() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_ERROR_NOT_FOUND);
    }
}
