package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ApiResponse;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.dto.account.AccountDto;
import com.authentication.api.dto.account.AccountFanoutDto;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.exception.UnauthorizationException;
import com.authentication.api.form.ChangeStatusForm;
import com.authentication.api.form.employee.CreateEmployeeForm;
import com.authentication.api.form.employee.UpdateEmployeeForm;
import com.authentication.api.form.employee.UpdateProfileEmployeeForm;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.mapper.AccountMapper;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.criteria.AccountCriteria;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.GroupRepository;
import com.authentication.api.service.MediaService;
import com.authentication.api.service.impl.UserServiceImpl;
import com.authentication.api.service.rabbit.RabbitService;
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
class EmployeeControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private MediaService mediaService;
    @Mock
    private RabbitService rabbitService;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private EmployeeController employeeController;

    private BaseJwt adminJwt() {
        BaseJwt jwt = new BaseJwt();
        jwt.setUserKind(BaseConstant.ACCOUNT_KIND_ADMIN);
        return jwt;
    }

    private BaseJwt jwtWithAccount(long id) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(id);
        return jwt;
    }

    private Group employeeGroup() {
        Group group = new Group();
        group.setId(20L);
        group.setKind(BaseConstant.GROUP_KIND_EMPLOYEE);
        return group;
    }

    private Account employeeAccount(long id) {
        Account account = new Account();
        account.setId(id);
        account.setKind(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
        return account;
    }

    // ---------- create ----------

    private CreateEmployeeForm createForm() {
        CreateEmployeeForm form = new CreateEmployeeForm();
        form.setUsername("emp1");
        form.setPassword("pass");
        form.setFullName("Emp One");
        form.setStatus(1);
        form.setGroupId(20L);
        return form;
    }

    @Test
    void create_whenValid_returnsSuccessResponse() {
        CreateEmployeeForm form = createForm();
        Account account = new Account();

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(null);
        when(groupRepository.findById(20L)).thenReturn(Optional.of(employeeGroup()));
        when(accountMapper.fromCreateEmployeeFormToEntity(form)).thenReturn(account);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = employeeController.create(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Create employee success");
        verify(accountRepository, times(1)).save(account);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_CREATED));
    }

    @Test
    void create_whenNotAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> employeeController.create(createForm(), null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void create_whenUsernameExists_throwsBadRequestException() {
        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(new Account());

        assertThatThrownBy(() -> employeeController.create(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
    }

    @Test
    void create_whenEmailExists_throwsBadRequestException() {
        CreateEmployeeForm form = createForm();
        form.setEmail("a@b.com");

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(null);
        when(accountRepository.existsByEmailAndStatusNot("a@b.com", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> employeeController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
    }

    @Test
    void create_whenPhoneExists_throwsBadRequestException() {
        CreateEmployeeForm form = createForm();
        form.setPhone("0987654321");

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(null);
        when(accountRepository.existsByPhoneAndStatusNot("0987654321", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> employeeController.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
    }

    @Test
    void create_whenGroupNotFound_throwsBadRequestException() {
        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(null);
        when(groupRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.create(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenGroupKindInvalid_throwsBadRequestException() {
        Group group = new Group();
        group.setId(20L);
        group.setKind(BaseConstant.GROUP_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAccountByUsername("emp1")).thenReturn(null);
        when(groupRepository.findById(20L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> employeeController.create(createForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    // ---------- update ----------

    private UpdateEmployeeForm updateForm() {
        UpdateEmployeeForm form = new UpdateEmployeeForm();
        form.setId(1L);
        form.setUsername("emp1");
        form.setFullName("Emp One");
        form.setGroupId(20L);
        form.setStatus(1);
        return form;
    }

    @Test
    void update_whenValid_returnsSuccessResponse() {
        UpdateEmployeeForm form = updateForm();
        Account account = employeeAccount(1L);

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(20L)).thenReturn(Optional.of(employeeGroup()));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = employeeController.update(form, null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update employee success");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void update_whenNotAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> employeeController.update(updateForm(), null))
                .isInstanceOf(UnauthorizationException.class);
    }

    @Test
    void update_whenAccountNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.update(updateForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenAccountNotEmployeeKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> employeeController.update(updateForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenGroupNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(employeeAccount(1L)));
        when(groupRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.update(updateForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenGroupKindInvalid_throwsBadRequestException() {
        Group group = new Group();
        group.setId(20L);
        group.setKind(BaseConstant.GROUP_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(employeeAccount(1L)));
        when(groupRepository.findById(20L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> employeeController.update(updateForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenEmailExists_throwsBadRequestException() {
        UpdateEmployeeForm form = updateForm();
        form.setEmail("new@b.com");
        Account account = employeeAccount(1L);
        account.setEmail("old@b.com");

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(20L)).thenReturn(Optional.of(employeeGroup()));
        when(accountRepository.existsByEmailAndStatusNot("new@b.com", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> employeeController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
    }

    @Test
    void update_whenPhoneExists_throwsBadRequestException() {
        UpdateEmployeeForm form = updateForm();
        form.setPhone("0987654321");
        Account account = employeeAccount(1L);
        account.setPhone("0111111111");

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(20L)).thenReturn(Optional.of(employeeGroup()));
        when(accountRepository.existsByPhoneAndStatusNot("0987654321", BaseConstant.STATUS_DELETE)).thenReturn(true);

        assertThatThrownBy(() -> employeeController.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
    }

    // ---------- get ----------

    @Test
    void get_whenEmployeeExists_returnsSuccessResponse() {
        Account account = employeeAccount(1L);
        AccountDto dto = new AccountDto();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToDtoShort(account)).thenReturn(dto);

        ApiMessageDto<AccountDto> response = employeeController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Get employee success");
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenNotEmployeeKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> employeeController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- delete ----------

    @Test
    void delete_whenValid_returnsSuccessResponse() {
        Account account = employeeAccount(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = employeeController.delete(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Delete employee success");
        verify(accountRepository, times(1)).deleteById(1L);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_DELETED));
    }

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenNotEmployeeKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> employeeController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_whenAdmin_returnsSuccessResponse() {
        AccountCriteria criteria = new AccountCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Collections.singletonList(new Account()));

        when(userService.getAddInfoFromToken()).thenReturn(adminJwt());
        when(accountRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(accountMapper.fromEntityToAccountDtoList(any())).thenReturn(Collections.singletonList(new AccountDto()));

        ApiMessageDto<ResponseListDto<List<AccountDto>>> response = employeeController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("List employee success");
        assertThat(criteria.getKind()).isEqualTo(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
    }

    @Test
    void list_whenNotAdmin_throwsUnauthorizationException() {
        when(userService.getAddInfoFromToken()).thenReturn(new BaseJwt());

        assertThatThrownBy(() -> employeeController.list(new AccountCriteria(), PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizationException.class);
    }

    // ---------- profile ----------

    @Test
    void profile_whenEmployee_returnsSuccessResponse() {
        Account account = employeeAccount(1L);
        AccountDto dto = new AccountDto();

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToDto(account)).thenReturn(dto);

        ApiResponse<AccountDto> response = employeeController.profile();

        assertThat(response.getData()).isEqualTo(dto);
        assertThat(response.getMessage()).isEqualTo("Get employee success");
    }

    @Test
    void profile_whenNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.profile())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void profile_whenNotEmployeeKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> employeeController.profile())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    // ---------- updateProfile ----------

    private UpdateProfileEmployeeForm profileForm() {
        UpdateProfileEmployeeForm form = new UpdateProfileEmployeeForm();
        form.setOldPassword("old");
        form.setFullName("New Name");
        return form;
    }

    @Test
    void updateProfile_whenValid_returnsSuccessResponse() {
        Account account = employeeAccount(1L);
        account.setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = employeeController.updateProfile(profileForm(), null);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Update employee success");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void updateProfile_whenNotFound_throwsNotFoundException() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.updateProfile(profileForm(), null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void updateProfile_whenWrongPassword_throwsBadRequestException() {
        Account account = employeeAccount(1L);
        account.setPassword("hashed");

        when(userService.getAddInfoFromToken()).thenReturn(jwtWithAccount(1L));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> employeeController.updateProfile(profileForm(), null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
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
        Account account = employeeAccount(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.fromAccountToFanoutDto(account)).thenReturn(new AccountFanoutDto());

        ApiMessageDto<Void> response = employeeController.changeStatus(changeStatusForm());

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Change status success");
        assertThat(account.getStatus()).isEqualTo(BaseConstant.STATUS_LOCK);
        verify(rabbitService, times(1)).handleSendFanout(any(), any(), eq(BaseConstant.EVENT_ACCOUNT_STATUS_CHANGED));
    }

    @Test
    void changeStatus_whenNotFound_throwsNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeController.changeStatus(changeStatusForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    @Test
    void changeStatus_whenNotEmployeeKind_throwsNotFoundException() {
        Account account = new Account();
        account.setId(1L);
        account.setKind(BaseConstant.ACCOUNT_KIND_USER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> employeeController.changeStatus(changeStatusForm()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }
}
