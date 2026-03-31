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
import com.authentication.api.mapper.GroupMapper;
import com.authentication.api.model.Group;
import com.authentication.api.model.Permission;
import com.authentication.api.model.criteria.GroupCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/group")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class GroupController extends ABasicController {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private AccountRepository accountRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateGroupForm createGroupForm, BindingResult bindingResult) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed create.");
        }
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Group group = groupRepository.findFirstByName(createGroupForm.getName());
        if (group != null) {
            throw new BadRequestException("[Group] Group name is existed", ErrorCode.GROUP_ERROR_NAME_EXIST);
        }

        if (groupRepository.existsByKindAndColor(createGroupForm.getKind(), createGroupForm.getColor())) {
            throw new BadRequestException("[Group] Group with this kind and color already exists", ErrorCode.GROUP_ERROR_COLOR_EXIST);
        }

        group = groupMapper.fromCreateGroupFormToEntity(createGroupForm);
        List<Permission> permissions = new ArrayList<>();
        for (long permissionId : createGroupForm.getPermissions()) {
            permissionRepository.findById(permissionId).ifPresent(permissions::add);
        }
        group.setPermissions(permissions);
        groupRepository.save(group);
        apiMessageDto.setMessage("Create group success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateGroupForm updateGroupForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Group group = groupRepository.findById(updateGroupForm.getId())
                .orElseThrow(() -> new NotFoundException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed update");
        }
        // Check if the new name already exists
        Group otherGroup = groupRepository.findFirstByName(updateGroupForm.getName());
        if (otherGroup != null && !Objects.equals(updateGroupForm.getId(), otherGroup.getId())) {
            throw new BadRequestException("[Group] Cant update this group name because it is exist!", ErrorCode.GROUP_ERROR_NAME_EXIST);
        }

        if (!Objects.equals(updateGroupForm.getColor(), group.getColor()) && groupRepository.existsByKindAndColor(group.getKind(), updateGroupForm.getColor())) {
            throw new BadRequestException("[Group] Group with this kind and color already exists", ErrorCode.GROUP_ERROR_COLOR_EXIST);
        }

        group.setName(updateGroupForm.getName());
        group.setDescription(updateGroupForm.getDescription());
        group.setColor(updateGroupForm.getColor());
        List<Permission> permissions = new ArrayList<>();
        for (long permissionId : updateGroupForm.getPermissions()) {
            permissionRepository.findById(permissionId).ifPresent(permissions::add);
        }
        group.setPermissions(permissions);
        groupRepository.save(group);
        apiMessageDto.setMessage("Update group success");

        return apiMessageDto;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_V')")
    public ApiMessageDto<GroupDto> get(@PathVariable("id") Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Group] Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND));

        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed to get.");
        }
        return makeSuccessResponse(groupMapper.fromEntityToGroupDto(group), "Get group success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_L')")
    public ApiMessageDto<ResponseListDto<List<GroupDto>>> list(GroupCriteria groupCriteria, Pageable pageable) {
        if (!isSuperAdmin()) {
            groupCriteria.setExcludeKind(BaseConstant.GROUP_KIND_ADMIN);
        }

        Page<Group> groups = groupRepository
                .findAll(groupCriteria.getSpecification(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));

        ResponseListDto<List<GroupDto>> responseListDto = makeResponseListDto(groups, groupMapper::fromEntityToGroupDtoList);
        return makeSuccessResponse(responseListDto, "List group success.");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<GroupDto>>> autoComplete(GroupCriteria groupCriteria, Pageable pageable) {
        if (!isAdmin()) {
            groupCriteria.setExcludeKind(BaseConstant.GROUP_KIND_ADMIN);
        }
        Page<Group> groups = groupRepository.findAll(
                groupCriteria.getSpecification(),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate")))
        );
        ResponseListDto<List<GroupDto>> responseListDto = makeResponseListDto(groups, groupMapper::fromEntityToGroupDtoAutoCompleteList);
        return makeSuccessResponse(responseListDto, "List group success.");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed");
        }
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));

        if (group.getIsSystemRole()) {
            throw new BadRequestException("[Group] Cant delete system role", ErrorCode.GROUP_ERROR_CANT_DELETE);
        }

        if (accountRepository.existsByGroupId(group.getId())) {
            throw new BadRequestException("[Group] Cant delete this group because it is used by account", ErrorCode.GROUP_ERROR_CANT_DELETE);
        }
        group.getPermissions().clear();
        groupRepository.save(group);
        groupRepository.delete(group);
        return makeSuccessResponse("Delete group success");
    }
}
