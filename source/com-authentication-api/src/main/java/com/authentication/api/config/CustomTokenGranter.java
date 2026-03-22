package com.authentication.api.config;

import com.authentication.api.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

@Slf4j
public class CustomTokenGranter extends AbstractTokenGranter {

    private UserServiceImpl userService;
    private AuthenticationManager authenticationManager;

    protected CustomTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
    }

    public CustomTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType, UserServiceImpl userService) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        return super.getOAuth2Authentication(client, tokenRequest);
    }

    protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
        String username = tokenRequest.getRequestParameters().get("username");
        String password = tokenRequest.getRequestParameters().get("password");
        String grantType = tokenRequest.getGrantType();
        try {
            log.error("Chưa phát triển custom token granter, grantType: {}", grantType);
//            if (grantType.equalsIgnoreCase(SecurityConstant.GRANT_TYPE_CUSTOMER) ||
//                    grantType.equalsIgnoreCase(SecurityConstant.GRANT_TYPE_EMPLOYEE) ||
//                    grantType.equalsIgnoreCase(SecurityConstant.GRANT_TYPE_USER)) {
//                String userId = tokenRequest.getRequestParameters().get("userId");
//                String userKind = tokenRequest.getRequestParameters().get("userKind");
//                String permissions = tokenRequest.getRequestParameters().get("permissions");
//                return userService.getAccessTokenForCustom(client, tokenRequest, username, password, tenant, grantType, userId, userKind, permissions, this.getTokenServices());
//            } else {
//                return userService.getAccessTokenForMultipleTenancies(client, tokenRequest, username, password, tenant, this.getTokenServices());
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidTokenException("account or tenant invalid");
        }
        return null;
    }
}
