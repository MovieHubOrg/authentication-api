package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.account.AccountFanoutDto;
import com.authentication.api.dto.user.GoogleMobileCallback;
import com.authentication.api.dto.user.GoogleWebCallback;
import com.authentication.api.dto.user.UserDto;
import com.authentication.api.dto.user.UserGoogleInfo;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.ChangeStatusForm;
import com.authentication.api.form.user.ChangePasswordForm;
import com.authentication.api.form.user.ForgotPasswordForm;
import com.authentication.api.form.user.LoginUserForm;
import com.authentication.api.form.user.RegisterUserForm;
import com.authentication.api.form.user.RequestForgotPasswordForm;
import com.authentication.api.form.user.ResendOtpForm;
import com.authentication.api.form.user.UpdateMakeSurveyForm;
import com.authentication.api.form.user.UpdateUserForm;
import com.authentication.api.form.user.UpdateUserProfileForm;
import com.authentication.api.form.user.UserSettingsForm;
import com.authentication.api.form.user.VerifyOtpForm;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.mapper.UserMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.User;
import com.authentication.api.model.criteria.UserCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.repository.UserRepository;
import com.authentication.api.service.CommonAsyncService;
import com.authentication.api.service.GoogleService;
import com.authentication.api.service.MediaService;
import com.authentication.api.service.OTPService;
import com.authentication.api.service.UserHandler;
import com.authentication.api.service.impl.UserServiceImpl;
import com.authentication.api.service.rabbit.RabbitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserHandler loginService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GoogleService googleService;
    @Mock
    private MediaService mediaService;
    @Mock
    private OTPService otpService;
    @Mock
    private CommonAsyncService commonAsyncService;
    @Mock
    private RabbitService rabbitService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    private BaseJwt jwtWithAccount(long id) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(id);
        return jwt;
    }

    private Account userAccount(long id) {
        Account account = new Account();
        account.setId(id);
        account.setEmail("user@mail.com");
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        return account;
    }

    private User userWithAccount(long id) {
        User user = new User();
        user.setId(id);
        user.setAccount(userAccount(id));
        return user;
    }

    // ---------- register (create) ----------

    private RegisterUserForm registerForm() {
        RegisterUserForm form = new RegisterUserForm();
        form.setEmail("user@mail.com");
        form.setPassword("Password1!");
        form.setFullName("User One");
        return form;
    }

    @Test
    void create_whenValid_returnsSuccessResponse() throws Exception {
        RegisterUserForm form = registerForm();
        Account account = userAccount(1L);

        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.empty());
        when(groupRepository.findById(BaseConstant.GROUP_USER_ID)).thenReturn(Optional.of(new Group()));
        when(accountMapper.fromRegisterUserFormToEntity(form)).thenReturn(account);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");
        when(otpService.generate(any(Integer.class))).thenReturn("123456");

        ApiMessageDto<Void> response = userController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Register success");
        verify(accountRepository, times(1)).save(account);
        verify(otpService, times(1)).storeOtp("user@mail.com", "123456");
    }

    @Test
    void create_whenEmailExists_throwsBadRequestException() {
        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE))
                .thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> userController.create(registerForm()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_EMAIL_EXISTED);
    }

    @Test
    void create_whenGroupNotFound_throwsNotFoundException() {
        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.empty());
        when(groupRepository.findById(BaseConstant.GROUP_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.create(registerForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    // ---------- verifyOtp ----------

    private VerifyOtpForm verifyForm() {
        VerifyOtpForm form = new VerifyOtpForm();
        form.setEmail("user@mail.com");
        form.setOtp("123456");
        return form;
    }

    @Test
    void verifyOtp_whenValid_returnsSuccessResponse() {
        Account account = userAccount(1L);

        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_PENDING)).thenReturn(Optional.of(account));
        when(otpService.verifyOtp("user@mail.com", "123456")).thenReturn(true);
        when(accountMapper.fromUserToFanoutDto(any(User.class))).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = userController.verifyOtp(verifyForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Verify otp success");
        assertThat(account.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).deleteOtp("user@mail.com");
    }

    @Test
    void verifyOtp_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_PENDING)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.verifyOtp(verifyForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void verifyOtp_whenOtpInvalid_throwsBadRequestException() {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_PENDING)).thenReturn(Optional.of(userAccount(1L)));
        when(otpService.verifyOtp("user@mail.com", "123456")).thenReturn(false);

        assertThatThrownBy(() -> userController.verifyOtp(verifyForm()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_OTP_INVALID);
    }

    // ---------- resendOtp ----------

    private ResendOtpForm resendForm() {
        ResendOtpForm form = new ResendOtpForm();
        form.setEmail("user@mail.com");
        return form;
    }

    @Test
    void resendOtp_whenValid_returnsSuccessResponse() throws Exception {
        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.of(userAccount(1L)));
        when(otpService.resendOtp("user@mail.com")).thenReturn("123456");

        ApiMessageDto<Void> response = userController.resendOtp(resendForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Resend otp success");
    }

    @Test
    void resendOtp_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.resendOtp(resendForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void resendOtp_whenLimitReached_throwsBadRequestException() {
        when(accountRepository.findFirstByEmailAndStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.of(userAccount(1L)));
        when(otpService.resendOtp("user@mail.com")).thenReturn(null);

        assertThatThrownBy(() -> userController.resendOtp(resendForm()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_RESEND_OTP_LIMIT);
    }

    // ---------- requestForgotPassword ----------

    private RequestForgotPasswordForm requestForgotForm() {
        RequestForgotPasswordForm form = new RequestForgotPasswordForm();
        form.setEmail("user@mail.com");
        return form;
    }

    @Test
    void requestForgotPassword_whenValid_returnsSuccessResponse() throws Exception {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(userAccount(1L)));
        when(otpService.generate(any(Integer.class))).thenReturn("123456");

        ApiMessageDto<Void> response = userController.requestForgotPassword(requestForgotForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Request forgot password success");
        verify(otpService, times(1)).storeOtp("user@mail.com", "123456");
    }

    @Test
    void requestForgotPassword_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.requestForgotPassword(requestForgotForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- forgotPassword ----------

    private ForgotPasswordForm forgotForm() {
        ForgotPasswordForm form = new ForgotPasswordForm();
        form.setEmail("user@mail.com");
        form.setOtp("123456");
        form.setPassword("NewPass1!");
        form.setConfirmPassword("NewPass1!");
        return form;
    }

    @Test
    void forgotPassword_whenValid_returnsSuccessResponse() {
        Account account = userAccount(1L);

        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(account));
        when(otpService.verifyOtp("user@mail.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded");

        ApiMessageDto<Void> response = userController.forgotPassword(forgotForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Change password success");
        verify(accountRepository, times(1)).save(account);
        verify(otpService, times(1)).deleteOtp("user@mail.com");
    }

    @Test
    void forgotPassword_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.forgotPassword(forgotForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void forgotPassword_whenConfirmPasswordInvalid_throwsBadRequestException() {
        ForgotPasswordForm form = forgotForm();
        form.setConfirmPassword("different");

        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(userAccount(1L)));

        assertThatThrownBy(() -> userController.forgotPassword(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_CONFIRM_PASSWORD_INVALID);
    }

    @Test
    void forgotPassword_whenOtpInvalid_throwsBadRequestException() {
        when(accountRepository.findFirstByEmailAndStatus("user@mail.com", BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(userAccount(1L)));
        when(otpService.verifyOtp("user@mail.com", "123456")).thenReturn(false);

        assertThatThrownBy(() -> userController.forgotPassword(forgotForm()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_OTP_INVALID);
    }

    // ---------- get ----------

    @Test
    void get_whenUserExists_returnsSuccessResponse() {
        User user = userWithAccount(1L);
        UserDto dto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.entityToUserDto(user)).thenReturn(dto);

        ApiMessageDto<UserDto> response = userController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get user success");
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponse() {
        UserCriteria criteria = new UserCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(new User()));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.fromEntityToUserDtoList(any())).thenReturn(Collections.singletonList(new UserDto()));

        ApiMessageDto<ResponseListDto<List<UserDto>>> response = userController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get list user success");
    }

    // ---------- autoComplete ----------

    @Test
    void autoComplete_returnsSuccessResponse() {
        UserCriteria criteria = new UserCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(new User()));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.entityToUserDtoAutoCompleteList(any())).thenReturn(Collections.singletonList(new UserDto()));

        ApiMessageDto<ResponseListDto<List<UserDto>>> response = userController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get list auto complete user success");
    }

    // ---------- update ----------

    private UpdateUserForm updateUserForm() {
        UpdateUserForm form = new UpdateUserForm();
        form.setId(1L);
        form.setStatus(1);
        return form;
    }

    @Test
    void update_whenValid_returnsSuccessResponse() throws Exception {
        User user = userWithAccount(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountMapper.fromUserToFanoutDto(user)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = userController.update(updateUserForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update user success");
        verify(accountRepository, times(1)).save(user.getAccount());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.update(updateUserForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- changeStatus ----------

    private ChangeStatusForm changeStatusForm() {
        ChangeStatusForm form = new ChangeStatusForm();
        form.setId(1L);
        form.setStatus(BaseConstant.STATUS_LOCK);
        return form;
    }

    @Test
    void changeStatus_whenValid_returnsSuccessResponse() {
        User user = userWithAccount(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountMapper.fromUserToFanoutDto(user)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = userController.changeStatus(changeStatusForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Change status success");
        assertThat(user.getAccount().getStatus()).isEqualTo(BaseConstant.STATUS_LOCK);
    }

    @Test
    void changeStatus_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.changeStatus(changeStatusForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- activeVIP ----------

    @Test
    void activeVIP_whenValid_returnsSuccessResponse() {
        User user = userWithAccount(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ApiMessageDto<Void> response = userController.activeVIP();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Active vip success");
        verify(accountRepository, times(1)).save(user.getAccount());
    }

    @Test
    void activeVIP_whenUserNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.activeVIP())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- delete ----------

    @Test
    void delete_whenValid_returnsSuccessResponse() {
        User user = userWithAccount(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountMapper.fromUserToFanoutDto(user)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = userController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete user success");
        verify(userRepository, times(1)).deleteById(1L);
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- login ----------

    private LoginUserForm loginForm() {
        LoginUserForm form = new LoginUserForm();
        form.setEmail("user@mail.com");
        form.setPassword("Password1!");
        return form;
    }

    @Test
    void login_whenValid_returnsAccessToken() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("hashed");
        OAuth2AccessToken token = mock(OAuth2AccessToken.class);

        when(userRepository.findFirstByAccountEmailAndAccountStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(true);
        when(loginService.getAccessToken(user.getAccount())).thenReturn(token);

        OAuth2AccessToken result = userController.login(loginForm(), null);

        assertThat(result).isEqualTo(token);
    }

    @Test
    void login_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findFirstByAccountEmailAndAccountStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.login(loginForm(), null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void login_whenPasswordBlank_throwsUsernameNotFoundException() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("");

        when(userRepository.findFirstByAccountEmailAndAccountStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userController.login(loginForm(), null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void login_whenWrongPassword_throwsUsernameNotFoundException() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("hashed");

        when(userRepository.findFirstByAccountEmailAndAccountStatusNot("user@mail.com", BaseConstant.STATUS_DELETE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userController.login(loginForm(), null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ---------- profile ----------

    @Test
    void profile_whenValid_returnsSuccessResponse() {
        User user = userWithAccount(1L);
        UserDto dto = new UserDto();

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(userMapper.fromEntityToUserDtoProfile(user)).thenReturn(dto);

        ApiMessageDto<UserDto> response = userController.profile();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get profile success");
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void profile_whenUserNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.profile())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    // ---------- updateProfile ----------

    private UpdateUserProfileForm updateProfileForm() {
        UpdateUserProfileForm form = new UpdateUserProfileForm();
        form.setFullName("New Name");
        return form;
    }

    @Test
    void updateProfile_whenValid_returnsSuccessResponse() throws Exception {
        User user = userWithAccount(1L);

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(accountMapper.fromUserToFanoutDto(user)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = userController.updateProfile(updateProfileForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update user profile success");
        verify(accountRepository, times(1)).save(user.getAccount());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateProfile_whenUserNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.updateProfile(updateProfileForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void updateProfile_whenPhoneExists_throwsBadRequestException() throws Exception {
        User user = userWithAccount(1L);
        user.getAccount().setPhone("0111111111");
        UpdateUserProfileForm form = updateProfileForm();
        form.setPhone("0987654321");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(userRepository.existsByAccountPhoneAndAccountStatusNot("0987654321", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> userController.updateProfile(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_PHONE_EXISTED);
    }

    @Test
    void updateProfile_whenUsernameExists_throwsBadRequestException() throws Exception {
        User user = userWithAccount(1L);
        user.getAccount().setUsername("olduser");
        UpdateUserProfileForm form = updateProfileForm();
        form.setUsername("newuser");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(userRepository.existsByAccountUsernameAndAccountStatusNot("newuser", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> userController.updateProfile(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_USERNAME_EXISTED);
    }

    // ---------- changePassword ----------

    private ChangePasswordForm changePasswordForm() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("OldPass1!");
        form.setNewPassword("NewPass1!");
        return form;
    }

    @Test
    void changePassword_whenValid_returnsSuccessResponse() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("newHashed");

        ApiMessageDto<Void> response = userController.changePassword(changePasswordForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update user profile success");
        verify(accountRepository, times(1)).save(user.getAccount());
    }

    @Test
    void changePassword_whenUserNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.changePassword(changePasswordForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void changePassword_whenWrongOldPassword_throwsBadRequestException() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userController.changePassword(changePasswordForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_WRONG_PASSWORD);
    }

    @Test
    void changePassword_whenNewSameAsOld_throwsBadRequestException() {
        User user = userWithAccount(1L);
        user.getAccount().setPassword("hashed");
        ChangePasswordForm form = changePasswordForm();
        form.setNewPassword("OldPass1!"); // equal to old

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> userController.changePassword(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD);
    }

    // ---------- socialLogin ----------

    @Test
    void socialLogin_returnsSuccessResponse() {
        when(googleService.generateAuthUrl()).thenReturn("http://auth-url");

        ApiMessageDto<String> response = userController.socialLogin(1);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getData()).isEqualTo("http://auth-url");
    }

    // ---------- socialWebCallback ----------

    @Test
    void socialWebCallback_returnsAccessToken() throws Exception {
        GoogleWebCallback callback = new GoogleWebCallback();
        callback.setCode("auth-code");
        UserGoogleInfo info = mock(UserGoogleInfo.class);
        OAuth2AccessToken token = mock(OAuth2AccessToken.class);

        when(googleService.getUserInfo("auth-code")).thenReturn(info);
        when(loginService.handleSocialLogin(info)).thenReturn(token);

        OAuth2AccessToken result = userController.socialWebCallback(callback, null);

        assertThat(result).isEqualTo(token);
    }

    // ---------- socialMobileCallback ----------

    @Test
    void socialMobileCallback_returnsAccessToken() throws Exception {
        GoogleMobileCallback callback = new GoogleMobileCallback();
        callback.setIdToken("id-token");
        callback.setPlatform(BaseConstant.PLATFORM_ANDROID);
        UserGoogleInfo info = mock(UserGoogleInfo.class);
        OAuth2AccessToken token = mock(OAuth2AccessToken.class);

        when(googleService.verifyIdToken("id-token", BaseConstant.PLATFORM_ANDROID)).thenReturn(info);
        when(loginService.handleSocialLogin(info)).thenReturn(token);

        OAuth2AccessToken result = userController.socialMobileCallback(callback, null);

        assertThat(result).isEqualTo(token);
    }

    // ---------- updateSettings ----------

    @Test
    void updateSettings_whenValid_returnsSuccessResponse() throws Exception {
        User user = userWithAccount(1L);
        UserSettingsForm form = new UserSettingsForm();

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(form)).thenReturn("{\"json\":true}");

        ApiMessageDto<Void> response = userController.updateSettings(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update settings success");
        assertThat(user.getSettings()).isEqualTo("{\"json\":true}");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateSettings_whenUserNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.updateSettings(new UserSettingsForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    @Test
    void updateSettings_whenJsonProcessingFails_throwsBadRequestException() throws Exception {
        User user = userWithAccount(1L);
        UserSettingsForm form = new UserSettingsForm();

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(userRepository.findByIdAndAccountStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(form)).thenThrow(mock(JsonProcessingException.class));

        assertThatThrownBy(() -> userController.updateSettings(form, null))
                .isInstanceOf(BadRequestException.class);
    }

    // ---------- updateMakeSurvey ----------

    private UpdateMakeSurveyForm makeSurveyForm() {
        UpdateMakeSurveyForm form = new UpdateMakeSurveyForm();
        form.setUserId(1L);
        form.setIsMakeSurvey(true);
        return form;
    }

    @Test
    void updateMakeSurvey_whenValid_returnsSuccessResponse() {
        ReflectionTestUtils.setField(userController, "serverInternalPassword", "secret");
        User user = userWithAccount(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ApiMessageDto<Void> response = userController.updateMakeSurvey("secret", makeSurveyForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update make survey success");
        assertThat(user.getIsMakeSurvey()).isTrue();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateMakeSurvey_whenWrongApiKey_throwsUnauthorizationException() {
        ReflectionTestUtils.setField(userController, "serverInternalPassword", "secret");

        assertThatThrownBy(() -> userController.updateMakeSurvey("wrong", makeSurveyForm(), null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void updateMakeSurvey_whenUserNotFound_throwsNotFoundException() {
        ReflectionTestUtils.setField(userController, "serverInternalPassword", "secret");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.updateMakeSurvey("secret", makeSurveyForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_ERROR_NOT_FOUND);
    }
}
