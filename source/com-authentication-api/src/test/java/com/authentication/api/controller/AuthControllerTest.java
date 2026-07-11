package com.authentication.api.controller;

import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.jwt.JwtService;
import com.authentication.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TokenStore tokenStore;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private AuthController authController;

    // ---------- logout ----------

    @Test
    void logout_whenValidBearerToken_removesAccessAndRefreshToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        OAuth2RefreshToken refreshToken = mock(OAuth2RefreshToken.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenStore.readAccessToken("abc123")).thenReturn(accessToken);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Logout successful");
        verify(tokenStore, times(1)).removeAccessToken(accessToken);
        verify(tokenStore, times(1)).removeRefreshToken(refreshToken);
    }

    @Test
    void logout_whenNoRefreshToken_onlyRemovesAccessToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenStore.readAccessToken("abc123")).thenReturn(accessToken);
        when(accessToken.getRefreshToken()).thenReturn(null);

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        verify(tokenStore, times(1)).removeAccessToken(accessToken);
        verify(tokenStore, never()).removeRefreshToken(any(OAuth2RefreshToken.class));
    }

    @Test
    void logout_whenTokenNotFound_doesNotRemoveAnything() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenStore.readAccessToken("abc123")).thenReturn(null);

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        verify(tokenStore, never()).removeAccessToken(any(OAuth2AccessToken.class));
    }

    @Test
    void logout_whenNoAuthorizationHeader_returnsSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Logout successful");
        verify(tokenStore, never()).readAccessToken(any(String.class));
    }

    @Test
    void logout_whenHeaderNotBearer_returnsSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        verify(tokenStore, never()).readAccessToken(any(String.class));
    }

    @Test
    void logout_whenExceptionThrown_stillReturnsSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenStore.readAccessToken("abc123")).thenThrow(new RuntimeException("boom"));

        ApiMessageDto<Void> response = authController.logout(request);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Logout successful");
    }

    // ---------- getAnonymousToken ----------

    @Test
    void getAnonymousToken_returnsBearerAccessToken() {
        ReflectionTestUtils.setField(authController, "expiresIn", Duration.ofHours(1));
        when(jwtService.generateToken()).thenReturn("anon-token");

        OAuth2AccessToken result = authController.getAnonymousToken();

        assertThat(result.getValue()).isEqualTo("anon-token");
        assertThat(result.getTokenType()).isEqualTo(OAuth2AccessToken.BEARER_TYPE);
        assertThat(result.getExpiration()).isNotNull();
    }
}
