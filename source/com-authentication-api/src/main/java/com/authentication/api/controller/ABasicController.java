package com.authentication.api.controller;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ResponseListDto;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;

    public <T> ApiMessageDto<T> makeResponse(Boolean result, T data, String message, String code) {
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setResult(result);
        apiMessageDto.setData(data);
        apiMessageDto.setMessage(message);
        apiMessageDto.setCode(code);
        return apiMessageDto;
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(String message) {
        return makeResponse(true, null, message, null);
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(T data, String message) {
        return makeResponse(true, data, message, null);
    }

    public <T> ApiMessageDto<T> makeErrorResponse(String message) {
        return makeResponse(false, null, message, null);
    }

    public <T, R> ResponseListDto<R> makeResponseListDto(Page<T> page, Function<List<T>, R> mapper) {
        return new ResponseListDto<>(
                mapper.apply(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public long getCurrentUser() {
        BaseJwt jwt = userService.getAddInfoFromToken();
        return jwt.getAccountId();
    }

    public long getTokenId() {
        BaseJwt jwt = userService.getAddInfoFromToken();
        return jwt.getTokenId();
    }

    public BaseJwt getSessionFromToken() {
        return userService.getAddInfoFromToken();
    }

    public boolean isSuperAdmin() {
        BaseJwt jwt = userService.getAddInfoFromToken();
        if (jwt != null) {
            return jwt.getIsSuperAdmin();
        }
        return false;
    }

    public boolean isAdmin() {
        BaseJwt baseJwt = userService.getAddInfoFromToken();
        if (baseJwt != null) {
            return Objects.equals(baseJwt.getUserKind(), BaseConstant.ACCOUNT_KIND_ADMIN);
        }
        return false;
    }

    public boolean isEmployee() {
        BaseJwt baseJwt = userService.getAddInfoFromToken();
        if (baseJwt != null) {
            return Objects.equals(baseJwt.getUserKind(), BaseConstant.ACCOUNT_KIND_EMPLOYEE);
        }
        return false;
    }

    public boolean isUser() {
        BaseJwt baseJwt = userService.getAddInfoFromToken();
        if (baseJwt != null) {
            return Objects.equals(baseJwt.getUserKind(), BaseConstant.ACCOUNT_KIND_USER);
        }
        return false;
    }

    public String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                return oauthDetails.getTokenValue();
            }
        }
        return null;
    }
}
