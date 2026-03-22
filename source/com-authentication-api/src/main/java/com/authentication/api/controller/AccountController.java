package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ApiResponse;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.account.AccountDto;
import com.authentication.api.dto.account.ForgetPasswordDto;
import com.authentication.api.dto.account.RequestForgetPasswordForm;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.account.CreateAccountAdminForm;
import com.authentication.api.form.account.ForgetPasswordForm;
import com.authentication.api.form.account.UpdateAccountAdminForm;
import com.authentication.api.form.account.UpdateProfileAdminForm;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.criteria.AccountCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.service.BaseApiService;
import com.authentication.api.service.MediaService;
import com.authentication.api.utils.AESUtils;
import com.authentication.api.utils.ConvertUtils;
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
@RequestMapping("/v1/account")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AccountController extends ABasicController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private BaseApiService baseApiService;
    @Autowired
    private MediaService mediaService;

    @PostMapping(value = "/create-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_C_AD')")
    public ApiMessageDto<Void> createAdmin(@Valid @RequestBody CreateAccountAdminForm form, BindingResult bindingResult) {
        if (!isSuperAdmin()) {
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

        account = accountMapper.fromCreateAdminFormToEntity(form);
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setKind(BaseConstant.USER_KIND_ADMIN);
        account.setGroup(group);
        accountRepository.save(account);

        return makeSuccessResponse("Create account admin success");
    }

    @PutMapping(value = "/update-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_U_AD')")
    public ApiMessageDto<Void> updateAdmin(@Valid @RequestBody UpdateAccountAdminForm form, BindingResult bindingResult) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed");
        }

        Account account = accountRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        Group group = groupRepository.findById(form.getGroupId())
                .orElseThrow(() -> new NotFoundException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));

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
        accountMapper.mappingUpdateAdminFormToEntity(form, account);
        accountRepository.save(account);

        return makeSuccessResponse("Update account admin success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_V')")
    public ApiMessageDto<AccountDto> get(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        return makeSuccessResponse(accountMapper.fromAccountToDtoShort(account), "Get account success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (account.getIsSuperAdmin()) {
            throw new BadRequestException("[Account] Account super admin cannot delete", ErrorCode.ACCOUNT_ERROR_NOT_DELETE_SUPPER_ADMIN);
        }
        // delete avatar file
        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            mediaService.deleteFile(avatarPath);
        }
        accountRepository.deleteById(id);

        return makeSuccessResponse("Delete Account success");
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AccountDto> profile() {
        long id = getCurrentUser();
        Account account = accountRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        ApiResponse<AccountDto> apiMessageDto = new ApiResponse<>();
        apiMessageDto.setData(accountMapper.fromAccountToDto(account));
        apiMessageDto.setMessage("Get Account success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update-profile-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> updateProfileAdmin(@Valid @RequestBody UpdateProfileAdminForm updateProfileAdminForm, BindingResult bindingResult) {
        Account account = accountRepository.findByIdAndStatus(getCurrentUser(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if (!passwordEncoder.matches(updateProfileAdminForm.getOldPassword(), account.getPassword())) {
            throw new BadRequestException("[Account] Wrong password", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
        }

        if (StringUtils.isNoneBlank(updateProfileAdminForm.getPassword())) {
            account.setPassword(passwordEncoder.encode(updateProfileAdminForm.getPassword()));
        }

        account.setFullName(updateProfileAdminForm.getFullName());

        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(updateProfileAdminForm.getAvatarPath())) {
            if (!updateProfileAdminForm.getAvatarPath().equals(account.getAvatarPath())) {
                // delete old image
                mediaService.deleteFile(avatarPath);
            }
            account.setAvatarPath(updateProfileAdminForm.getAvatarPath());
        }
        accountRepository.save(account);

        return makeSuccessResponse("Update admin account success");
    }

    @PostMapping(value = "/request-forget-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ForgetPasswordDto> requestForgetPassword(@Valid @RequestBody RequestForgetPasswordForm forgetForm, BindingResult bindingResult) {
        ApiResponse<ForgetPasswordDto> apiMessageDto = new ApiResponse<>();
        Account account = accountRepository.findAccountByEmail(forgetForm.getEmail())
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        String otp = baseApiService.getOTPForgetPassword();
        accountRepository.save(account);

        //send email
        baseApiService.sendEmail(account.getEmail(), "OTP: " + otp, "Reset password", false);

        ForgetPasswordDto forgetPasswordDto = new ForgetPasswordDto();
        String hash = AESUtils.encrypt(account.getId() + ";" + otp, true);
        forgetPasswordDto.setIdHash(hash);

        apiMessageDto.setResult(true);
        apiMessageDto.setData(forgetPasswordDto);
        apiMessageDto.setMessage("Request forget password successfully, please check email.");
        return apiMessageDto;
    }

    @PostMapping(value = "/forget-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Long> forgetPassword(@Valid @RequestBody ForgetPasswordForm forgetForm, BindingResult bindingResult) {
        ApiResponse<Long> apiMessageDto = new ApiResponse<>();

        String[] hash = AESUtils.decrypt(forgetForm.getIdHash(), true).split(";", 2);
        Long id = ConvertUtils.convertStringToLong(hash[0]);
        if (id <= 0) {
            throw new BadRequestException("[Account] Wrong hash reset password", ErrorCode.ACCOUNT_ERROR_WRONG_HASH_RESET_PASS);
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        account.setPassword(passwordEncoder.encode(forgetForm.getNewPassword()));
        accountRepository.save(account);

        apiMessageDto.setResult(true);
        apiMessageDto.setMessage("Change password success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_L')")
    public ApiMessageDto<ResponseListDto<List<AccountDto>>> listAccount(AccountCriteria accountCriteria, Pageable pageable) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("[Account] Not allowed to list account.");
        }
        Page<Account> accounts = accountRepository.findAll(accountCriteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(accounts, accountMapper::fromEntityToAccountDtoList), "List account success");
    }
}
