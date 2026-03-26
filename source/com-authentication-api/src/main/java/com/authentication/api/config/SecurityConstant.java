package com.authentication.api.config;

public class SecurityConstant {
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String GRANT_TYPE_CUSTOMER = "customer";
    public static final String GRANT_TYPE_EMPLOYEE = "employee";
    public static final String GRANT_TYPE_USER = "user";

    public static final String[] PUBLIC_ENDPOINTS = {
            "/v1/account/request_forget_password", "/v1/account/forget_password",
            "/api/auth/activate/resend", "/api/auth/pwd", "/api/auth/logout",
            "/v1/user/login",
            "/v1/user/register",
            "/v1/user/verify-otp",
            "/v1/user/resend-otp",
            "/v1/user/request-forgot-password",
            "/v1/user/forgot-password",
            "/v1/user/auth/social-login",
            "/v1/user/auth/web-callback",
            "/v1/user/auth/mobile-callback",
            "/v1/auth/get-anonymous-token",
            "/v1/auth/logout"
    };
}
