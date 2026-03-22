package com.authentication.api.dto;

public class ErrorCode {
    /**
     * Group error code
     */
    public static final String GROUP_ERROR_NAME_EXIST = "ERROR-GROUP-000";
    public static final String GROUP_ERROR_NOT_FOUND = "ERROR-GROUP-001";

    /**
     * Permission error code
     */
    public static final String PERMISSION_ERROR_NAME_EXIST = "ERROR-PERMISSION-000";
    public static final String PERMISSION_ERROR_CODE_EXIST = "ERROR-PERMISSION-001";
    public static final String PERMISSION_ERROR_NOT_FOUND = "ERROR-PERMISSION-002";

    /**
     * Starting error code Account
     */
    public static final String ACCOUNT_ERROR_UNKNOWN = "ERROR-ACCOUNT-0000";
    public static final String ACCOUNT_ERROR_USERNAME_EXIST = "ERROR-ACCOUNT-0001";
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-0002";
    public static final String ACCOUNT_ERROR_WRONG_PASSWORD = "ERROR-ACCOUNT-0003";
    public static final String ACCOUNT_ERROR_WRONG_HASH_RESET_PASS = "ERROR-ACCOUNT-0004";
    public static final String ACCOUNT_ERROR_LOCKED = "ERROR-ACCOUNT-0005";
    public static final String ACCOUNT_ERROR_OPT_INVALID = "ERROR-ACCOUNT-0006";
    public static final String ACCOUNT_ERROR_LOGIN = "ERROR-ACCOUNT-0007";
    public static final String ACCOUNT_ERROR_SOCIAL_LOGIN_FAIL = "ERROR-ACCOUNT-ERROR-0008";
    public static final String ACCOUNT_ERROR_NOT_DELETE_SUPPER_ADMIN = "ERROR-ACCOUNT-00014";
    public static final String ACCOUNT_ERROR_EMAIL_EXISTED = "ERROR-ACCOUNT-00015";
    public static final String ACCOUNT_ERROR_PHONE_EXISTED = "ERROR-ACCOUNT-00016";
    public static final String ACCOUNT_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD = "ERROR-ACCOUNT-00017";

    /**
     * GroupPermission error code
     */
    public static final String GROUP_PERMISSION_ERROR_NOT_FOUND = "ERROR-GROUP-PERMISSION-000";
    public static final String GROUP_PERMISSION_ERROR_NAME_EXIST = "ERROR-GROUP-PERMISSION-001";

    /**
     * Starting error code User
     */
    public static final String USER_ERROR_NOT_FOUND = "ERROR-USER-ERROR-0000";
    public static final String USER_ERROR_USERNAME_EXISTED = "ERROR-USER-ERROR-0002";
    public static final String USER_ERROR_PHONE_EXISTED = "ERROR-USER-ERROR-0003";
    public static final String USER_ERROR_EMAIL_EXISTED = "ERROR-USER-ERROR-0004";
    public static final String USER_ERROR_WRONG_PASSWORD = "ERROR-USER-ERROR-0005";
    public static final String USER_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD = "ERROR-USER-ERROR-0006";
    public static final String USER_ERROR_OTP_INVALID = "ERROR-USER-ERROR-0007";
    public static final String USER_ERROR_RESEND_OTP_LIMIT = "ERROR-USER-ERROR-0008";
    public static final String USER_ERROR_CONFIRM_PASSWORD_INVALID = "ERROR-USER-ERROR-0009";

}
