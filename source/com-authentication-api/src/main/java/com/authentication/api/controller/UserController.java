package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.user.GoogleMobileCallback;
import com.authentication.api.dto.user.GoogleWebCallback;
import com.authentication.api.dto.user.UserDto;
import com.authentication.api.dto.user.UserGoogleInfo;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.form.ChangeStatusForm;
import com.authentication.api.form.user.*;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.mapper.UserMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.User;
import com.authentication.api.model.criteria.UserCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.UserRepository;
import com.authentication.api.service.*;
import com.authentication.api.utils.TemplateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class UserController extends ABasicController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserHandler loginService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleService googleService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private CommonAsyncService commonAsyncService;

    private final Integer otpLength = 6;

    @Transactional
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> create(@Valid @RequestBody RegisterUserForm form) throws IOException {
        Account account = accountRepository.findFirstByEmailAndStatusNot(form.getEmail(), BaseConstant.STATUS_DELETE).orElse(null);
        if (account != null) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }

        account = accountMapper.fromRegisterUserFormToEntity(form);
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setKind(BaseConstant.USER_KIND_USER);
        account.setStatus(BaseConstant.STATUS_PENDING);
        accountRepository.save(account);

        String otp = otpService.generate(otpLength);
        otpService.storeOtp(form.getEmail(), otp);

        // Send email
        String htmlContent = TemplateUtils.loadTemplate("active-account.html").replace("${email}", account.getEmail()).replace("${otp}", otp);
        commonAsyncService.sendEmail(account.getEmail(), htmlContent, "Chào mừng đến MovieHub", true);

        return makeSuccessResponse("Register success");
    }

    @Transactional
    @PostMapping(value = "/verify-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> verifyOtp(@Valid @RequestBody VerifyOtpForm form) {
        Account account = accountRepository.findFirstByEmailAndStatus(form.getEmail(), BaseConstant.STATUS_PENDING)
                .orElseThrow(() -> new NotFoundException("[Account] Not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if (!otpService.verifyOtp(form.getEmail(), form.getOtp())) {
            throw new BadRequestException("Invalid OTP", ErrorCode.USER_ERROR_OTP_INVALID);
        }

        account.setStatus(BaseConstant.STATUS_ACTIVE);
        accountRepository.save(account);

        User user = new User();
        user.setAccount(account);
        userRepository.save(user);

        otpService.deleteOtp(form.getEmail());
        return makeSuccessResponse("Verify otp success");
    }

    @Transactional
    @PostMapping(value = "/resend-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> resendOtp(@Valid @RequestBody ResendOtpForm form) throws IOException {
        Account account = accountRepository.findFirstByEmailAndStatusNot(form.getEmail(), BaseConstant.STATUS_DELETE)
                .orElseThrow(() -> new NotFoundException("[Account] Not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        String otp = otpService.resendOtp(account.getEmail());
        if (otp == null) {
            throw new BadRequestException("Resend OTP limit (3 per 10 minutes)", ErrorCode.USER_ERROR_RESEND_OTP_LIMIT);
        }

        // Send email
        String htmlContent = TemplateUtils.loadTemplate("active-account.html").replace("${email}", account.getEmail()).replace("${otp}", otp);
        commonAsyncService.sendEmail(account.getEmail(), htmlContent, "Chào mừng đến MovieHub", true);

        return makeSuccessResponse("Resend otp success");
    }

    @Transactional
    @PostMapping(value = "/request-forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> requestForgotPassword(@Valid @RequestBody RequestForgotPasswordForm form) throws IOException {
        Account account = accountRepository.findFirstByEmailAndStatus(form.getEmail(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Account] Not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        String otp = otpService.generate(otpLength);
        otpService.storeOtp(account.getEmail(), otp);

        // Send email
        String htmlContent = TemplateUtils.loadTemplate("active-account.html").replace("${email}", account.getEmail()).replace("${otp}", otp);
        commonAsyncService.sendEmail(account.getEmail(), htmlContent, "Chào mừng đến MovieHub", true);

        return makeSuccessResponse("Request forgot password success");
    }

    @Transactional
    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> forgotPassword(@Valid @RequestBody ForgotPasswordForm form) {
        Account account = accountRepository.findFirstByEmailAndStatus(form.getEmail(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Account] Not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            throw new BadRequestException("[Account] Confirm password invalid", ErrorCode.USER_ERROR_CONFIRM_PASSWORD_INVALID);
        }

        if (!otpService.verifyOtp(form.getEmail(), form.getOtp())) {
            throw new BadRequestException("Invalid OTP", ErrorCode.USER_ERROR_OTP_INVALID);
        }

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        accountRepository.save(account);

        otpService.deleteOtp(form.getEmail());

        return makeSuccessResponse("Change password success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USR_V')")
    public ApiMessageDto<UserDto> get(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        return makeSuccessResponse(userMapper.entityToUserDto(user), "Get user success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USR_L')")
    public ApiMessageDto<ResponseListDto<List<UserDto>>> list(UserCriteria criteria, Pageable pageable) {
        Page<User> users = userRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(users, userMapper::fromEntityToUserDtoList), "Get list user success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USR_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateUserForm form) throws JsonProcessingException {
        User user = userRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        accountMapper.fromUpdateUserFormToEntity(form, user.getAccount());
        accountRepository.save(user.getAccount());

//        userMapper.fromUpdateUserFormToEntity(form, user);
        userRepository.save(user);
        return makeSuccessResponse("Update user success");
    }

    @Transactional
    @PutMapping(value = "/change-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USR_U')")
    public ApiMessageDto<Void> changeStatus(@Valid @RequestBody ChangeStatusForm form) {
        User user = userRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        user.getAccount().setStatus(form.getStatus());
        accountRepository.save(user.getAccount());
        userRepository.save(user);

        return makeSuccessResponse("Change status success");
    }

    @Transactional
    @PutMapping(value = "/active-vip", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> activeVIP() {
        User user = userRepository.findById(getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        accountRepository.save(user.getAccount());

        return makeSuccessResponse("Active vip success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USR_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));
        mediaService.deleteFile(user.getAccount().getAvatarPath());
        userRepository.deleteById(id);
        accountRepository.deleteById(id);

        return makeSuccessResponse("Delete user success");
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AccessToken login(@Valid @RequestBody LoginUserForm form) {
        User user = userRepository.findFirstByAccountEmailAndAccountStatusNot(form.getEmail(), BaseConstant.STATUS_DELETE)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));

        if (!passwordEncoder.matches(form.getPassword(), user.getAccount().getPassword())) {
            log.error("Invalid username or password.");
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        OAuth2AccessToken result = loginService.getAccessToken(user.getAccount());
        if (result == null) {
            log.error("Get token failed.");
        }
        return result;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<UserDto> profile() {
        User user = userRepository.findByIdAndAccountStatus(getCurrentUser(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));
        return makeSuccessResponse(userMapper.fromEntityToUserDtoProfile(user), "Get profile success");
    }

    @Transactional
    @PutMapping(value = "/update-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> updateProfile(@Valid @RequestBody UpdateUserProfileForm form) throws JsonProcessingException {
        User user = userRepository.findByIdAndAccountStatus(getCurrentUser(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        if (StringUtils.isNotBlank(form.getPhone()) && !Objects.equals(user.getAccount().getPhone(), form.getPhone())
                && userRepository.existsByAccountPhoneAndAccountStatusNot(form.getPhone(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[User] Phone existed", ErrorCode.USER_ERROR_PHONE_EXISTED);
        }

        if (StringUtils.isNotBlank(form.getUsername()) && !Objects.equals(user.getAccount().getUsername(), form.getUsername())
                && userRepository.existsByAccountUsernameAndAccountStatusNot(form.getUsername(), BaseConstant.STATUS_DELETE)) {
            throw new BadRequestException("[User] Username existed", ErrorCode.USER_ERROR_USERNAME_EXISTED);
        }

        if (!Objects.equals(form.getAvatarPath(), user.getAccount().getAvatarPath())) {
            String avatarPath = user.getAccount().getAvatarPath();
            mediaService.deleteFile(avatarPath);
        }

        accountMapper.fromUpdateUserProfileFormToEntity(form, user.getAccount());
        accountRepository.save(user.getAccount());

        userMapper.fromUpdateUserProfileFormToEntity(form, user);
        userRepository.save(user);
        return makeSuccessResponse("Update user profile success");
    }

    @Transactional
    @PutMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> changePassword(@Valid @RequestBody ChangePasswordForm form) {
        User user = userRepository.findById(getCurrentUser())
                .orElseThrow(() -> new NotFoundException("[User] Not found", ErrorCode.USER_ERROR_NOT_FOUND));

        if (StringUtils.isNoneBlank(form.getNewPassword()) && StringUtils.isNoneBlank(form.getOldPassword())) {
            if (!passwordEncoder.matches(form.getOldPassword(), user.getAccount().getPassword())) {
                throw new BadRequestException("[User] Wrong password", ErrorCode.USER_ERROR_WRONG_PASSWORD);
            }
            if (form.getNewPassword().equals(form.getOldPassword())) {
                throw new BadRequestException("[User] New password must be different from old password", ErrorCode.USER_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD);
            }
            user.getAccount().setPassword(passwordEncoder.encode(form.getNewPassword()));
        }

        accountRepository.save(user.getAccount());
        return makeSuccessResponse("Update user profile success");
    }

    @GetMapping(value = "/auth/social-login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> socialLogin(@RequestParam Integer loginType) {
        String redirectUri = googleService.generateAuthUrl();
        return makeSuccessResponse(redirectUri, "Success");
    }

    @Transactional
    @PostMapping(value = "/auth/web-callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AccessToken socialWebCallback(@Valid @RequestBody GoogleWebCallback googleCallback) throws IOException {
        UserGoogleInfo userInfo = googleService.getUserInfo(googleCallback.getCode());
        OAuth2AccessToken result = loginService.handleSocialLogin(userInfo);
        log.info(result.toString());

        return result;
    }

    @Transactional
    @PostMapping(value = "/auth/mobile-callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AccessToken socialMobileCallback(@Valid @RequestBody GoogleMobileCallback callback) throws IOException {
        UserGoogleInfo userInfo = googleService.verifyIdToken(callback.getIdToken(), callback.getPlatform());
        OAuth2AccessToken result = loginService.handleSocialLogin(userInfo);
        log.info(result.toString());

        return result;
    }
}
