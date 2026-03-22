package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
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
import com.authentication.api.model.criteria.PermissionCriteria;
import com.authentication.api.repository.GroupPermissionRepository;
import com.authentication.api.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/permission")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class PermissionController extends ABasicController {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreatePermissionForm form, BindingResult bindingResult) {
        GroupPermission groupPermission = groupPermissionRepository.findById(form.getGroupPermissionId())
                .orElseThrow(() -> new NotFoundException("[Group Permission] not found", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND));

        if (permissionRepository.existsByNameAndGroupPermissionId(form.getName(), groupPermission.getId())) {
            throw new BadRequestException("Permission name is existed!", ErrorCode.PERMISSION_ERROR_NAME_EXIST);
        }

        if (permissionRepository.existsByPermissionCode(form.getPermissionCode())) {
            throw new BadRequestException("Permission permissionCode is existed!", ErrorCode.PERMISSION_ERROR_CODE_EXIST);
        }


        Permission permission = permissionMapper.fromCreatePermissionFormToEntity(form);
        permission.setGroupPermission(groupPermission);
        permissionRepository.save(permission);

        return makeSuccessResponse("Create permission success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdatePermissionForm form, BindingResult bindingResult) {
        Permission permission = permissionRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Permission] not found", ErrorCode.PERMISSION_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getName(), permission.getName())
                && permissionRepository.existsByNameAndGroupPermissionId(form.getName(), permission.getGroupPermission().getId())) {
            throw new BadRequestException("Permission name is existed!", ErrorCode.PERMISSION_ERROR_NAME_EXIST);
        }

        if (!Objects.equals(form.getPermissionCode(), permission.getPermissionCode())
                && permissionRepository.existsByPermissionCode(form.getPermissionCode())) {
            throw new BadRequestException("Permission code is existed!", ErrorCode.PERMISSION_ERROR_CODE_EXIST);
        }

        permissionMapper.mappingUpdatePermissionFormToEntity(form, permission);
        permissionRepository.save(permission);
        return makeSuccessResponse("Update permission success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_L')")
    public ApiMessageDto<ResponseListDto<List<PermissionDto>>> list() {
        PermissionCriteria criteria = new PermissionCriteria();
        Page<Permission> permissions = permissionRepository.findAll(criteria.getSpecification(), PageRequest.of(0, 1000, Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        return makeSuccessResponse(makeResponseListDto(permissions, permissionMapper::fromEntityToPermissionDtoList), "Get permissions list success");
    }

    @GetMapping(value = "/list-by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_L')")
    public ApiMessageDto<List<PermissionDto>> listByIds(@RequestParam("ids") List<Long> ids) {
        List<Permission> permissions = permissionRepository.findAllByIdIn(ids);
        return makeSuccessResponse(permissionMapper.fromEntityToPermissionDtoAutoCompleteList(permissions), "Get permissions list success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Permission] not found", ErrorCode.PERMISSION_ERROR_NOT_FOUND));
        permissionRepository.deletePermissionGroupById(id);
        permissionRepository.delete(permission);
        return makeSuccessResponse("Delete permission success");
    }
}
