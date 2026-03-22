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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/group-permission")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class GroupPermissionController extends ABasicController {
    @Autowired
    GroupPermissionRepository groupPermissionRepository;
    @Autowired
    GroupPermissionMapper groupPermissionMapper;
    @Autowired
    PermissionRepository permissionRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_PER_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateGroupPermissionForm form, BindingResult bindingResult) {
        GroupPermission groupPermission = groupPermissionRepository.findFirstByName(form.getName());
        if (groupPermission != null) {
            throw new BadRequestException("[GroupPermission] name is existed!", ErrorCode.GROUP_PERMISSION_ERROR_NAME_EXIST);
        }

        groupPermission = groupPermissionMapper.fromCreateGroupPermissionFormToEntity(form);
        groupPermissionRepository.save(groupPermission);

        return makeSuccessResponse("Create group permission success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_PER_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateGroupPermissionForm form, BindingResult bindingResult) {
        GroupPermission groupPermission = groupPermissionRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[GroupPermission] not found", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getName(), groupPermission.getName()) && groupPermissionRepository.existsByName(form.getName())) {
            throw new BadRequestException("[GroupPermission] name is existed!", ErrorCode.GROUP_PERMISSION_ERROR_NAME_EXIST);
        }

        groupPermissionMapper.mappingUpdateGroupPermissionFormToEntity(form, groupPermission);
        groupPermissionRepository.save(groupPermission);

        return makeSuccessResponse("Update group permission success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_PER_L')")
    public ApiMessageDto<ResponseListDto<List<GroupPermissionDto>>> list(GroupPermissionCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<GroupPermission> groupPermissions = groupPermissionRepository.findAll(
                criteria.getSpecification(),
                pageable
        );
        ResponseListDto<List<GroupPermissionDto>> responseListDto = makeResponseListDto(groupPermissions, groupPermissionMapper::fromEntityToGroupPermissionDtoList);
        return makeSuccessResponse(responseListDto, "Get list group permission success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_PER_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        GroupPermission groupPermission = groupPermissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[GroupPermission] not found", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND));
        permissionRepository.deleteByGroupPermissionId(groupPermission.getId());
        groupPermissionRepository.delete(groupPermission);
        return makeSuccessResponse("Delete group permission success");
    }

    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_PER_U')")
    public ApiMessageDto<Void> updateOrdering(@RequestBody List<@Valid UpdateOrderingForm> form) {
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Input list cannot be empty");
        }

        List<Long> ids = form.stream()
                .map(UpdateOrderingForm::getId)
                .collect(Collectors.toList());
        List<GroupPermission> groupPermissions = groupPermissionRepository.findAllById(ids);

        if (groupPermissions.size() != ids.size()) {
            throw new NotFoundException("[GroupPermissions] Not found", ErrorCode.GROUP_PERMISSION_ERROR_NOT_FOUND);
        }

        Map<Long, Integer> orderingMap = form.stream()
                .collect(Collectors.toMap(UpdateOrderingForm::getId, UpdateOrderingForm::getOrdering));

        for (GroupPermission item : groupPermissions) {
            item.setOrdering(orderingMap.get(item.getId()));
        }
        groupPermissionRepository.saveAll(groupPermissions);

        return makeSuccessResponse("Update ordering groupPermission success");
    }
}
