package com.authentication.api.controller;

import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.permissionGroup.GroupPermissionDto;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.form.UpdateOrderingForm;
import com.authentication.api.form.groupPermission.CreateGroupPermissionForm;
import com.authentication.api.form.groupPermission.UpdateGroupPermissionForm;
import com.authentication.api.mapper.GroupPermissionMapper;
import com.authentication.api.model.GroupPermission;
import com.authentication.api.model.criteria.GroupPermissionCriteria;
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
import org.springframework.data.domain.PageRequest;
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
class GroupPermissionControllerTest {

    @Mock
    private GroupPermissionRepository groupPermissionRepository;
    @Mock
    private GroupPermissionMapper groupPermissionMapper;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private GroupPermissionController groupPermissionController;

    private CreateGroupPermissionForm createForm() {
        CreateGroupPermissionForm form = new CreateGroupPermissionForm();
        form.setName("UserGroup");
        return form;
    }

    private UpdateGroupPermissionForm updateForm() {
        UpdateGroupPermissionForm form = new UpdateGroupPermissionForm();
        form.setId(1L);
        form.setName("UserGroup");
        return form;
    }

    private UpdateOrderingForm orderingForm(Long id, Integer ordering) {
        UpdateOrderingForm form = new UpdateOrderingForm();
        form.setId(id);
        form.setOrdering(ordering);
        return form;
    }

    // ---------- create ----------

    @Test
    void create_whenValid_returnsSuccessResponse() {
        CreateGroupPermissionForm form = createForm();
        GroupPermission gp = new GroupPermission();

        when(groupPermissionRepository.findFirstByName("UserGroup")).thenReturn(null);
        when(groupPermissionMapper.fromCreateGroupPermissionFormToEntity(form)).thenReturn(gp);
        when(groupPermissionRepository.findMaxOrdering()).thenReturn(Optional.of(2));

        ApiMessageDto<Void> response = groupPermissionController.create(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Create group permission success");
        assertThat(gp.getOrdering()).isEqualTo(3);
        verify(groupPermissionRepository, times(1)).save(gp);
    }

    @Test
    void create_whenNoExistingOrdering_setsOrderingZero() {
        CreateGroupPermissionForm form = createForm();
        GroupPermission gp = new GroupPermission();

        when(groupPermissionRepository.findFirstByName("UserGroup")).thenReturn(null);
        when(groupPermissionMapper.fromCreateGroupPermissionFormToEntity(form)).thenReturn(gp);
        when(groupPermissionRepository.findMaxOrdering()).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = groupPermissionController.create(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(gp.getOrdering()).isEqualTo(0);
    }

    @Test
    void create_whenNameExists_throwsBadRequestException() {
        CreateGroupPermissionForm form = createForm();
        when(groupPermissionRepository.findFirstByName("UserGroup")).thenReturn(new GroupPermission());

        assertThatThrownBy(() -> groupPermissionController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NAME_EXIST);
    }

    // ---------- update ----------

    @Test
    void update_whenValid_returnsSuccessResponse() {
        UpdateGroupPermissionForm form = updateForm();
        GroupPermission gp = new GroupPermission();
        gp.setId(1L);
        gp.setName("UserGroup"); // same -> name check short-circuits

        when(groupPermissionRepository.findById(1L)).thenReturn(Optional.of(gp));

        ApiMessageDto<Void> response = groupPermissionController.update(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update group permission success");
        verify(groupPermissionRepository, times(1)).save(gp);
    }

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateGroupPermissionForm form = updateForm();
        when(groupPermissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupPermissionController.update(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNameCollides_throwsBadRequestException() {
        UpdateGroupPermissionForm form = updateForm();
        form.setName("NewName");
        GroupPermission gp = new GroupPermission();
        gp.setId(1L);
        gp.setName("OldName");

        when(groupPermissionRepository.findById(1L)).thenReturn(Optional.of(gp));
        when(groupPermissionRepository.existsByName("NewName")).thenReturn(true);

        assertThatThrownBy(() -> groupPermissionController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NAME_EXIST);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponse() {
        GroupPermissionCriteria criteria = new GroupPermissionCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupPermission> page = new PageImpl<>(Collections.singletonList(new GroupPermission()));

        when(groupPermissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(groupPermissionMapper.fromEntityToGroupPermissionDtoList(any())).thenReturn(Collections.singletonList(new GroupPermissionDto()));

        ApiMessageDto<ResponseListDto<List<GroupPermissionDto>>> response = groupPermissionController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get list group permission success");
        assertThat(response.getData().getContent()).hasSize(1);
    }

    // ---------- delete ----------

    @Test
    void delete_whenExists_returnsSuccessResponse() {
        GroupPermission gp = new GroupPermission();
        gp.setId(1L);
        when(groupPermissionRepository.findById(1L)).thenReturn(Optional.of(gp));

        ApiMessageDto<Void> response = groupPermissionController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete group permission success");
        verify(permissionRepository, times(1)).deleteByGroupPermissionId(1L);
        verify(groupPermissionRepository, times(1)).delete(gp);
    }

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(groupPermissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupPermissionController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND);
    }

    // ---------- updateOrdering ----------

    @Test
    void updateOrdering_whenValid_returnsSuccessResponse() {
        List<UpdateOrderingForm> forms = List.of(orderingForm(1L, 10), orderingForm(2L, 20));
        GroupPermission gp1 = new GroupPermission();
        gp1.setId(1L);
        GroupPermission gp2 = new GroupPermission();
        gp2.setId(2L);

        when(groupPermissionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(gp1, gp2));

        ApiMessageDto<Void> response = groupPermissionController.updateOrdering(forms);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update ordering groupPermission success");
        assertThat(gp1.getOrdering()).isEqualTo(10);
        assertThat(gp2.getOrdering()).isEqualTo(20);
        verify(groupPermissionRepository, times(1)).saveAll(List.of(gp1, gp2));
    }

    @Test
    void updateOrdering_whenNull_throwsBadRequestException() {
        assertThatThrownBy(() -> groupPermissionController.updateOrdering(null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateOrdering_whenEmpty_throwsBadRequestException() {
        assertThatThrownBy(() -> groupPermissionController.updateOrdering(Collections.emptyList()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateOrdering_whenSomeIdsNotFound_throwsNotFoundException() {
        List<UpdateOrderingForm> forms = List.of(orderingForm(1L, 10), orderingForm(2L, 20));
        GroupPermission gp1 = new GroupPermission();
        gp1.setId(1L);

        when(groupPermissionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(gp1));

        assertThatThrownBy(() -> groupPermissionController.updateOrdering(forms))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND);
    }
}
