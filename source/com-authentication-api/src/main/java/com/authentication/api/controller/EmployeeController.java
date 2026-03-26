package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ApiResponse;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.account.AccountDto;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.ChangeStatusForm;
import com.authentication.api.form.employee.CreateEmployeeForm;
import com.authentication.api.form.employee.UpdateEmployeeForm;
import com.authentication.api.form.employee.UpdateProfileEmployeeForm;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.criteria.AccountCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.service.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/employee")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class EmployeeController extends ABasicController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private MediaService mediaService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateEmployeeForm form, BindingResult bindingResult) {
        if (!isAdmin()) {
            throw new UnauthorizationException("Not allowed");
        }

        Account account = accountRepository.findAccountByUsername(form.getUsername());
        if (account != null) {
            throw new BadRequestException("[Account] Username exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }
        if (StringUtils.isNoneBlank(form.getEmail())
                && accountRepository.existsByEmailAndStatusNot(form.getEmail(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }
        if (StringUtils.isNoneBlank(form.getPhone())
                && accountRepository.existsByPhoneAndStatusNot(form.getPhone(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Phone existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
        }

        Group group = groupRepository.findById(form.getGroupId())
                .orElseThrow(() -> new BadRequestException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));
        if (group.getKind() != BaseConstant.GROUP_KIND_EMPLOYEE) {
            throw new BadRequestException("[Group] Group kind invalid", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }

        account = accountMapper.fromCreateEmployeeFormToEntity(form);
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setKind(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
        account.setGroup(group);
        accountRepository.save(account);

        return makeSuccessResponse("Create employee success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateEmployeeForm form, BindingResult bindingResult) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed");
        }

        Account account = accountRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        Group group = groupRepository.findById(form.getGroupId())
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.GROUP_ERROR_NOT_FOUND));
        if (group.getKind() != BaseConstant.GROUP_KIND_EMPLOYEE) {
            throw new BadRequestException("[Group] Group kind invalid", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }

        if (StringUtils.isNoneBlank(form.getEmail())
                && !form.getEmail().equals(account.getEmail())
                && accountRepository.existsByEmailAndStatusNot(form.getEmail(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }
        if (StringUtils.isNoneBlank(form.getPhone())
                && !form.getPhone().equals(account.getPhone())
                && accountRepository.existsByPhoneAndStatusNot(form.getPhone(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Phone existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
        }

        if (StringUtils.isNoneBlank(form.getPassword())) {
            account.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(form.getAvatarPath())) {
            if (!form.getAvatarPath().equals(account.getAvatarPath())) {
                // delete old image
                mediaService.deleteFile(avatarPath);
            }
            account.setAvatarPath(form.getAvatarPath());
        }

        account.setGroup(group);
        account.setKind(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
        accountMapper.mappingUpdateEmployeeFormToEntity(form, account);
        accountRepository.save(account);
        return makeSuccessResponse("Update employee success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_V')")
    public ApiMessageDto<AccountDto> get(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        return makeSuccessResponse(accountMapper.fromAccountToDtoShort(account), "Get employee success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        // delete avatar file
        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            mediaService.deleteFile(avatarPath);
        }
        accountRepository.deleteById(id);
        return makeSuccessResponse("Delete employee success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_L')")
    public ApiMessageDto<ResponseListDto<List<AccountDto>>> list(AccountCriteria accountCriteria, Pageable pageable) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("[Employee] Not allowed to list employee.");
        }
        accountCriteria.setKind(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
        Page<Account> accounts = accountRepository.findAll(accountCriteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(accounts, accountMapper::fromEntityToAccountDtoList), "List employee success");
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AccountDto> profile() {
        Account account = accountRepository.findById(getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        ApiResponse<AccountDto> apiMessageDto = new ApiResponse<>();
        apiMessageDto.setData(accountMapper.fromAccountToDto(account));
        apiMessageDto.setMessage("Get employee success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> updateProfile(@Valid @RequestBody UpdateProfileEmployeeForm form, BindingResult bindingResult) {
        Account account = accountRepository.findById(getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        if (!passwordEncoder.matches(form.getOldPassword(), account.getPassword())) {
            throw new BadRequestException("[Employee] Wrong password", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
        }

        if (StringUtils.isNoneBlank(form.getPassword())) {
            account.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        account.setFullName(form.getFullName());

        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(form.getAvatarPath())) {
            if (!form.getAvatarPath().equals(account.getAvatarPath())) {
                // delete old image
                mediaService.deleteFile(avatarPath);
            }
            account.setAvatarPath(form.getAvatarPath());
        }
        accountRepository.save(account);

        return makeSuccessResponse("Update employee success");
    }

    @Transactional
    @PutMapping(value = "/change-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EM_U')")
    public ApiMessageDto<Void> changeStatus(@Valid @RequestBody ChangeStatusForm form) {
        Account account = accountRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (!BaseConstant.ACCOUNT_KIND_EMPLOYEE.equals(account.getKind())) {
            throw new NotFoundException("[Employee] Employee not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        account.setStatus(form.getStatus());
        accountRepository.save(account);
        return makeSuccessResponse("Change status success");
    }
}
