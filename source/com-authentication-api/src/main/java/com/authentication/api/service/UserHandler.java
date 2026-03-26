package com.authentication.api.service;

import com.authentication.api.config.CustomTokenEnhancer;
import com.authentication.api.constant.BaseConstant;
import com.authentication.api.dto.OauthClientDetailsDto;
import com.authentication.api.dto.user.UserGoogleInfo;
import com.authentication.api.exception.BadRequestException;
import com.authentication.api.model.Account;
import com.authentication.api.model.Group;
import com.authentication.api.model.User;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.repository.UserRepository;
import com.authentication.api.utils.TemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserHandler {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommonAsyncService commonAsyncService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DefaultTokenServices tokenServices;
    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;

    private final String clientId = "abc_client";
    private final String grantType = "user";

    public OAuth2AccessToken handleSocialLogin(UserGoogleInfo userInfo) throws IOException {
        String email = userInfo.getEmail();
        String name = userInfo.getName();
        String picture = userInfo.getPicture();

        Account account = accountRepository.findFirstByEmail(email).orElse(null);
        if (account == null) {
            account = new Account();
            account.setEmail(email);
            account.setFullName(name);
            account.setAvatarPath(picture);
            account.setKind(BaseConstant.ACCOUNT_KIND_USER);
            accountRepository.save(account);
            User user = new User();
            user.setAccount(account);
            userRepository.save(user);
            // Send email
            String htmlContent = TemplateUtils.loadTemplate("register-success.html").replace("${email}", email);
            commonAsyncService.sendEmail(email, htmlContent, "Chào mừng đến MovieHub", true);
        }
        return getAccessToken(account);
    }

    public OAuth2AccessToken getAccessToken(Account account) {
        OauthClientDetailsDto clientDetails = getOauthClientDetails(clientId);
        if (clientDetails == null) {
            throw new BadRequestException("[General] Not found clientId");
        }
        if (!clientDetails.getAuthorizedGrantTypes().contains(grantType)) {
            throw new BadRequestException("[General] Client not contain this grant type");
        }

        boolean enabled = true;
        Set<GrantedAuthority> grantedAuthorities = getAccountPermission(account);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(account.getEmail(), account.getEmail(), enabled, enabled, enabled, enabled, grantedAuthorities);

        return generateAccessToken(userDetails, clientDetails, grantType);
    }

    public OAuth2AccessToken generateAccessToken(UserDetails userPrincipal, OauthClientDetailsDto clientDetails, String grantType) {
        try {
            OAuth2Authentication authentication = convertAuthentication(userPrincipal, clientDetails, grantType);
            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            tokenEnhancerChain.setTokenEnhancers(Arrays.asList(new CustomTokenEnhancer(jdbcTemplate), accessTokenConverter));
            tokenServices.setTokenEnhancer(tokenEnhancerChain);
            tokenServices.setReuseRefreshToken(false);
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setAccessTokenValiditySeconds(clientDetails.getAccessTokenValidInSeconds());
            tokenServices.setRefreshTokenValiditySeconds(clientDetails.getRefreshTokenValidInSeconds());
            return tokenServices.createAccessToken(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private OAuth2Authentication convertAuthentication(UserDetails userDetails, OauthClientDetailsDto clientDetails, String grantType) {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("grant_type", grantType);
        Set<String> scope = new HashSet<>();
        String[] scopeArray = clientDetails.getScope().split(",");
        Collections.addAll(scope, scopeArray);
        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        Map<String, Serializable> extensionProperties = new HashMap<>();
        OAuth2Request request = new OAuth2Request(requestParameters, clientDetails.getClientId(), userDetails.getAuthorities(), true, scope, null, null, responseTypes, extensionProperties);
        return new OAuth2Authentication(request, new UsernamePasswordAuthenticationToken(userDetails, "N/A", userDetails.getAuthorities()));
    }

    private Set<GrantedAuthority> getAccountPermission(Account account) {
        List<String> roles = new ArrayList<>();
        Group group = account.getGroup();
        if (group != null) {
            group.getPermissions().stream().filter(f -> f.getPermissionCode() != null).forEach(pName -> roles.add(pName.getPermissionCode()));
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toSet());
    }

    public OauthClientDetailsDto getOauthClientDetails(String clientId) {
        try {
            String query = "SELECT client_id, scope, authorized_grant_types, access_token_validity, refresh_token_validity " +
                    "FROM oauth_client_details WHERE client_id = '" + clientId + "' LIMIT 1";
            OauthClientDetailsDto oauthClientDetailsDto = jdbcTemplate.queryForObject(query,
                    (resultSet, rowNum) -> new OauthClientDetailsDto(resultSet.getString("client_id"),
                            resultSet.getString("scope"), resultSet.getString("authorized_grant_types"),
                            resultSet.getInt("access_token_validity"), resultSet.getInt("refresh_token_validity")));
            return oauthClientDetailsDto;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
