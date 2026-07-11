package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ApiResponse;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.account.AccountDto;
import com.authentication.api.dto.account.AccountFanoutDto;
import com.authentication.api.dto.account.ForgetPasswordDto;
import com.authentication.api.dto.account.RequestForgetPasswordForm;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.ChangeStatusForm;
import com.authentication.api.form.account.CreateAccountAdminForm;
import com.authentication.api.form.account.ForgetPasswordForm;
import com.authentication.api.form.account.UpdateAccountAdminForm;
import com.authentication.api.form.account.UpdateProfileAdminForm;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.criteria.AccountCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.service.BaseApiService;
import com.authentication.api.service.MediaService;
import com.authentication.api.service.rabbit.RabbitService;
import com.authentication.api.utils.AESUtils;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private BaseApiService baseApiService;
    @Mock
    private MediaService mediaService;
    @Mock
    private RabbitService rabbitService;
    @Mock
    private com.authentication.api.service.impl.UserServiceImpl userService;

    @InjectMocks
    private AccountController accountController;

    private BaseJwt superAdminJwt() {
        BaseJwt jwt = new BaseJwt();
        jwt.setIsSuperAdmin(true);
        return jwt;
    }

    private BaseJwt jwtWithAccount(long accountId) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(accountId);
        return jwt;
    }

    private Group adminGroup() {
        Group group = new Group();
        group.setId(10L);
        group.setKind(BaseConstant.GROUP_KIND_ADMIN);
        return group;
    }

    private Account adminAccount(long id) {
        Account account = new Account();
        account.setId(id);
        account.setKind(BaseConstant.ACCOUNT_KIND_ADMIN);
        return account;
    }

    // ---------- createAdmin ----------

    private CreateAccountAdminForm createForm() {
        CreateAccountAdminForm form = new CreateAccountAdminForm();
        form.setUsername("admin1");
        form.setPassword("pass");
        form.setFullName("Admin One");
        form.setStatus(1);
        form.setGroupId(10L);
        return form;
    }

    @Test
    void createAdmin_whenValid_returnsSuccessResponse() {
        CreateAccountAdminForm form = createForm();
        Account account = new Account();

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(null);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(adminGroup()));
        when(accountMapper.fromCreateAdminFormToEntity(form)).thenReturn(account);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = accountController.createAdmin(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Create account admin success");
        verify(accountRepository, times(1)).save(account);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_CREATED));
    }

    @Test
    void createAdmin_whenNotSuperAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> accountController.createAdmin(createForm(), null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void createAdmin_whenUsernameExists_throwsBadRequestException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(new Account());

        assertThatThrownBy(() -> accountController.createAdmin(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
    }

    @Test
    void createAdmin_whenEmailExists_throwsBadRequestException() {
        CreateAccountAdminForm form = createForm();
        form.setEmail("a@b.com");

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(null);
        when(accountRepository.existsByEmailAndStatusNot("a@b.com", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> accountController.createAdmin(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
    }

    @Test
    void createAdmin_whenPhoneExists_throwsBadRequestException() {
        CreateAccountAdminForm form = createForm();
        form.setPhone("0987654321");

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(null);
        when(accountRepository.existsByPhoneAndStatusNot("0987654321", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> accountController.createAdmin(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
    }

    @Test
    void createAdmin_whenGroupNotFound_throwsBadRequestException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(null);
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.createAdmin(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void createAdmin_whenGroupKindInvalid_throwsBadRequestException() {
        Group group = new Group();
        group.setId(10L);
        group.setKind(BaseConstant.GROUP_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAccountByUsername("admin1")).thenReturn(null);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> accountController.createAdmin(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    // ---------- updateAdmin ----------

    private UpdateAccountAdminForm updateForm() {
        UpdateAccountAdminForm form = new UpdateAccountAdminForm();
        form.setId(1L);
        form.setUsername("admin1");
        form.setFullName("Admin One");
        form.setGroupId(10L);
        form.setStatus(1);
        return form;
    }

    @Test
    void updateAdmin_whenValid_returnsSuccessResponse() {
        UpdateAccountAdminForm form = updateForm();
        Account account = adminAccount(1L);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(adminGroup()));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = accountController.updateAdmin(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update account admin success");
        verify(accountRepository, times(1)).save(account);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_UPDATED));
    }

    @Test
    void updateAdmin_whenNotSuperAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> accountController.updateAdmin(updateForm(), null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void updateAdmin_whenAccountNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.updateAdmin(updateForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void updateAdmin_whenGroupNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(adminAccount(1L)));
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.updateAdmin(updateForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void updateAdmin_whenGroupKindInvalid_throwsBadRequestException() {
        Group group = new Group();
        group.setId(10L);
        group.setKind(BaseConstant.GROUP_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(adminAccount(1L)));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> accountController.updateAdmin(updateForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void updateAdmin_whenEmailExists_throwsBadRequestException() {
        UpdateAccountAdminForm form = updateForm();
        form.setEmail("new@b.com");
        Account account = adminAccount(1L);
        account.setEmail("old@b.com");

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(adminGroup()));
        when(accountRepository.existsByEmailAndStatusNot("new@b.com", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> accountController.updateAdmin(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
    }

    @Test
    void updateAdmin_whenPhoneExists_throwsBadRequestException() {
        UpdateAccountAdminForm form = updateForm();
        form.setPhone("0987654321");
        Account account = adminAccount(1L);
        account.setPhone("0111111111");

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(adminGroup()));
        when(accountRepository.existsByPhoneAndStatusNot("0987654321", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> accountController.updateAdmin(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
    }

    // ---------- get ----------

    @Test
    void get_whenAdminAccountExists_returnsSuccessResponse() {
        Account account = adminAccount(1L);
        AccountDto dto = new AccountDto();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToDtoShort(account)).thenReturn(dto);

        ApiMessageDto<AccountDto> response = accountController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get account success");
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenAccountNotAdminKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- delete ----------

    @Test
    void delete_whenValid_returnsSuccessResponse() {
        Account account = adminAccount(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = accountController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete Account success");
        verify(accountRepository, times(1)).deleteById(1L);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_DELETED));
    }

    @Test
    void delete_whenAccountNotFound_throwsNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenNotAdminKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenSuperAdminAccount_throwsBadRequestException() {
        Account account = adminAccount(1L);
        account.setIsSuperAdmin(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_DELETE_SUPPER_ADMIN);
    }

    // ---------- profile ----------

    @Test
    void profile_whenAdmin_returnsSuccessResponse() {
        Account account = adminAccount(1L);
        AccountDto dto = new AccountDto();

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToDto(account)).thenReturn(dto);

        ApiResponse<AccountDto> response = accountController.profile();

        assertThat(response.getData()).isEqualTo(dto);
        assertThat(response.getMessage()).isEqualTo("Get Account success");
    }

    @Test
    void profile_whenAccountNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.profile())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void profile_whenNotAdminKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.profile())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- updateProfileAdmin ----------

    private UpdateProfileAdminForm profileForm() {
        UpdateProfileAdminForm form = new UpdateProfileAdminForm();
        form.setOldPassword("old");
        form.setFullName("New Name");
        return form;
    }

    @Test
    void updateProfileAdmin_whenValid_returnsSuccessResponse() {
        Account account = adminAccount(1L);
        account.setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = accountController.updateProfileAdmin(profileForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update admin account success");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void updateProfileAdmin_whenAccountNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.updateProfileAdmin(profileForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void updateProfileAdmin_whenWrongPassword_throwsBadRequestException() {
        Account account = adminAccount(1L);
        account.setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findByIdAndStatus(1L, BaseConstant.STATUS_ACTIVE)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> accountController.updateProfileAdmin(profileForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
    }

    // ---------- requestForgetPassword ----------

    @Test
    void requestForgetPassword_whenAccountExists_returnsSuccessResponse() {
        RequestForgetPasswordForm form = new RequestForgetPasswordForm();
        form.setEmail("a@b.com");
        Account account = adminAccount(1L);
        account.setEmail("a@b.com");

        when(accountRepository.findAccountByEmail("a@b.com")).thenReturn(Optional.of(account));
        when(baseApiService.getOTPForgetPassword()).thenReturn("1234");

        ApiResponse<ForgetPasswordDto> response = accountController.requestForgetPassword(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getIdHash()).isNotNull();
        verify(baseApiService, times(1)).sendEmail(eq("a@b.com"), any(), eq("Reset password"), eq(false));
    }

    @Test
    void requestForgetPassword_whenAccountNotFound_throwsNotFoundException() {
        RequestForgetPasswordForm form = new RequestForgetPasswordForm();
        form.setEmail("a@b.com");
        when(accountRepository.findAccountByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.requestForgetPassword(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- forgetPassword ----------

    @Test
    void forgetPassword_whenValid_returnsSuccessResponse() {
        ForgetPasswordForm form = new ForgetPasswordForm();
        form.setOtp("1234");
        form.setNewPassword("newPass");
        form.setIdHash(AESUtils.encrypt("1;1234", true));
        Account account = adminAccount(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");

        ApiResponse<Long> response = accountController.forgetPassword(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Change password success.");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void forgetPassword_whenHashIdInvalid_throwsBadRequestException() {
        ForgetPasswordForm form = new ForgetPasswordForm();
        form.setOtp("1234");
        form.setNewPassword("newPass");
        form.setIdHash(AESUtils.encrypt("0;1234", true));

        assertThatThrownBy(() -> accountController.forgetPassword(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_WRONG_HASH_RESET_PASS);
    }

    @Test
    void forgetPassword_whenAccountNotFound_throwsNotFoundException() {
        ForgetPasswordForm form = new ForgetPasswordForm();
        form.setOtp("1234");
        form.setNewPassword("newPass");
        form.setIdHash(AESUtils.encrypt("5;1234", true));

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.forgetPassword(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- listAccount ----------

    @Test
    void listAccount_whenSuperAdmin_returnsSuccessResponse() {
        AccountCriteria criteria = new AccountCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Collections.singletonList(new Account()));

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(accountMapper.fromEntityToAccountDtoList(any())).thenReturn(Collections.singletonList(new AccountDto()));

        ApiMessageDto<ResponseListDto<List<AccountDto>>> response = accountController.listAccount(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("List account success");
        assertThat(criteria.getKind()).isEqualTo(BaseConstant.ACCOUNT_KIND_ADMIN);
    }

    @Test
    void listAccount_whenNotSuperAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> accountController.listAccount(new AccountCriteria(), PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizationException.class);
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
        Account account = adminAccount(1L);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = accountController.changeStatus(changeStatusForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Change status success");
        assertThat(account.getStatus()).isEqualTo(BaseConstant.STATUS_LOCK);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_STATUS_CHANGED));
    }

    @Test
    void changeStatus_whenNotSuperAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> accountController.changeStatus(changeStatusForm()))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void changeStatus_whenAccountNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountController.changeStatus(changeStatusForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void changeStatus_whenNotAdminKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.changeStatus(changeStatusForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void changeStatus_whenSuperAdminAccount_throwsBadRequestException() {
        Account account = adminAccount(1L);
        account.setIsSuperAdmin(true);

        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountController.changeStatus(changeStatusForm()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_DELETE_SUPPER_ADMIN);
    }
}
