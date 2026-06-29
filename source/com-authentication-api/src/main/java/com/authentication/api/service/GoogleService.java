package com.authentication.api.service;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.ErrorCode;
import com.authentication.api.dto.setting.SettingDto;
import com.authentication.api.dto.user.UserGoogleInfo;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.service.feign.FeignMovieService;
import com.authentication.api.service.redis.RedisService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class GoogleService {
    @Value("${social.google.web.client-id}")
    private String webClientId;

    @Value("${social.google.web.client-secret}")
    private String webClientSecret;

    @Value("${social.google.ios.client-id}")
    private String iosClientId;

    @Value("${social.google.android.client-id}")
    private String androidClientId;

    @Value("${social.google.web.redirect-uri.remote}")
    private String webRedirectUriRemote;

    @Value("${social.google.web.redirect-uri.local}")
    private String webRedirectUriLocal;

    @Value("${social.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${social.google.token-uri}")
    private String googleTokenUri;

    @Value("${movie.internal.api.key}")
    private String movieInternalApiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private FeignMovieService feignMovieSettingService;

    private final String ACCESS_TOKEN = "access_token";

    private final String AUTHORIZATION_CODE = "authorization_code";

    public String generateAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", webClientId)
                .queryParam("redirect_uri", getWebRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }

    private String exchangeCodeForAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", webClientId);
        params.add("client_secret", webClientSecret);
        params.add("redirect_uri", getWebRedirectUri());
        params.add("grant_type", AUTHORIZATION_CODE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                googleTokenUri, request, Map.class);

        Map body = response.getBody();
        if (body == null || !body.containsKey(ACCESS_TOKEN)) {
            throw new BadRequestException("Failed to exchange code with Google token");
        }

        return (String) body.get(ACCESS_TOKEN);
    }

    private String getWebRedirectUri() {
        return isDevMode() ? webRedirectUriLocal : webRedirectUriRemote;
    }

    private boolean isDevMode() {
        String cacheKey = redisService.buildKey(BaseConstant.SETTING_REDIS_KEY_PREFIX, BaseConstant.SETTING_KEY_DEV_MODE);
        try {
            SettingDto cachedDevMode = redisService.get(cacheKey, SettingDto.class);
            if (cachedDevMode != null) {
                return Boolean.parseBoolean(cachedDevMode.getValueData());
            }

            ApiMessageDto<SettingDto> response = feignMovieSettingService.findByKey(BaseConstant.SETTING_KEY_DEV_MODE, movieInternalApiKey);
            boolean devMode = Boolean.FALSE;
            if (response != null && Boolean.TRUE.equals(response.getResult()) && response.getData() != null) {
                devMode = Boolean.parseBoolean(response.getData().getValueData());
                redisService.put(cacheKey, response.getData(), 24 * 60 * 60); // Cache for 24 hours
            }
            return devMode;
        } catch (Exception e) {
            log.warn("Failed to load setting {}. Use remote Google redirect URI by default", BaseConstant.SETTING_KEY_DEV_MODE, e);
            return false;
        }
    }

    public UserGoogleInfo verifyIdToken(String idTokenStr, Integer platform) {
        String clientId = "";
        switch (platform) {
            case BaseConstant.PLATFORM_IOS:
                clientId = iosClientId;
                break;
            case BaseConstant.PLATFORM_ANDROID:
                clientId = androidClientId;
                break;
            default:
                break;
        }
        if (StringUtils.isBlank(clientId)) {
            throw new BadRequestException("Client ID is not configured", ErrorCode.ACCOUNT_ERROR_SOCIAL_LOGIN_FAIL);
        }
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
        try {
            GoogleIdToken idToken = verifier.verify(idTokenStr);
            if (idToken == null) {
                throw new BadRequestException("Invalid ID Token", ErrorCode.ACCOUNT_ERROR_SOCIAL_LOGIN_FAIL);
            }
            GoogleIdToken.Payload payload = idToken.getPayload();

            return new UserGoogleInfo(
                    (String) payload.get("name"),
                    (String) payload.get("picture"),
                    payload.getEmail()
            );
        } catch (Exception e) {
            throw new BadRequestException("Token verification failed", ErrorCode.ACCOUNT_ERROR_SOCIAL_LOGIN_FAIL);
        }
    }

    public UserGoogleInfo getUserInfo(String code) {
        String accessToken = exchangeCodeForAccessToken(code);
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(authHeaders);

        ResponseEntity<UserGoogleInfo> userInfoResponse = restTemplate.exchange(
                googleUserInfoUri,
                HttpMethod.GET,
                userInfoRequest,
                UserGoogleInfo.class
        );
        return userInfoResponse.getBody();
    }
}
