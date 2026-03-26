package com.authentication.api.config;

import com.authentication.api.constant.DatabaseConstant;
import com.authentication.api.dto.AccountForTokenDto;
import com.authentication.api.jwt.BaseJwt;
import com.authentication.api.model.Permission;
import com.authentication.api.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CustomTokenEnhancer implements TokenEnhancer {
    private JdbcTemplate jdbcTemplate;

    String prefix = DatabaseConstant.PREFIX_TABLE;

    public CustomTokenEnhancer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();
        String username = authentication.getName();
        if (authentication.getOAuth2Request().getGrantType() != null) {
            additionalInfo = getAdditionalInfo(username, authentication.getOAuth2Request().getGrantType());
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private Map<String, Object> getAdditionalInfo(String username, String grantType) {
        Map<String, Object> additionalInfo = new HashMap<>();
        AccountForTokenDto a = null;
        if (Objects.equals(grantType, "password")) {
            a = getAccountByUsername(username);
        } else if (Objects.equals(grantType, "user")) {
            a = getAccountByEmail(username);
        }
        if (a != null) {
            Long accountId = a.getId();
            String kind = a.getKind() + ""; // token kind
            String permission = "<>"; // empty string
            Integer userKind = a.getKind(); // user type is admin or something else
            Boolean isSuperAdmin = a.getIsSuperAdmin();
            additionalInfo.put("user_id", accountId);
            additionalInfo.put("user_kind", a.getKind());
            additionalInfo.put("grant_type", grantType);

            BaseJwt baseJwt = new BaseJwt(accountId, kind, permission, userKind, username, isSuperAdmin);
            additionalInfo.put("additional_info", baseJwt.toClaim());
        }
        return additionalInfo;
    }

    public AccountForTokenDto getAccountByUsername(String username) {
        try {
            String query = "SELECT id, kind, username, email, full_name, is_super_admin " +
                    "FROM " + prefix + "account " +
                    "WHERE username = ? AND status = 1 LIMIT 1";
            log.debug(query);
            List<AccountForTokenDto> dto = jdbcTemplate.query(query, new Object[]{username}, new BeanPropertyRowMapper<>(AccountForTokenDto.class));
            if (!dto.isEmpty()) return dto.get(0);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public AccountForTokenDto getAccountByEmail(String email) {
        try {
            String query = "SELECT id, kind, username, email, full_name, is_super_admin " +
                    "FROM " + prefix + "account " +
                    "WHERE email = ? AND status = 1 LIMIT 1";
            log.debug(query);
            List<AccountForTokenDto> dto = jdbcTemplate.query(query, new Object[]{email}, new BeanPropertyRowMapper<>(AccountForTokenDto.class));
            if (!dto.isEmpty()) return dto.get(0);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
