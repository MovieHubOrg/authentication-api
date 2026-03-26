package com.authentication.api.controller;

import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AuthController extends ABasicController {
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private JwtService jwtService;

    @Value("${auth.expires-in}")
    private Duration expiresIn;

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);

                if (accessToken != null) {
                    tokenStore.removeAccessToken(accessToken);
                    if (accessToken.getRefreshToken() != null) {
                        tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                    }
                }
            }
        } catch (Exception e) {
            // optional log
        }
        return makeSuccessResponse("Logout successful");
    }

    @PostMapping(value = "/get-anonymous-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AccessToken getAnonymousToken() {
        String token = jwtService.generateToken();
        long millis = expiresIn.toMillis();
        DefaultOAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(token);
        accessToken.setTokenType(OAuth2AccessToken.BEARER_TYPE);
        accessToken.setExpiration(new Date(System.currentTimeMillis() + millis));
        return accessToken;
    }
}
