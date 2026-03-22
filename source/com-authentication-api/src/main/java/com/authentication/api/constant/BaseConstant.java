package com.authentication.api.constant;

public class BaseConstant {
    public static final String DEFAULT_TIMEZONE = "UTC";


    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String HEADER_CLIENT_TYPE = "X-Client-Type";
    public static final String HEADER_CLIENT_TYPE_WEB = "WEB";

    public static final Integer BOOLEAN_FALSE = 0;
    public static final Integer BOOLEAN_TRUE = 1;

    public static final String RESTAURANT_SETTING_SAMPLE_DATA = "{\"customDateOff\":{\"startTime\":\"25/02/2025 14:27:24\",\"endTime\":\"01/03/2025 03:00:00\"},\"general\":{\"timezone\":{\"name\":\"Asia/Ho_Chi_Minh\",\"offset\":\"+7:00\"},\"currency\":\"€\",\"currency_position\":1,\"decimal_separator\":\",\",\"decimal_space\":\"2\",\"group_separator\":\".\",\"date_time_format\":\"DD.MM.YYYYHH:mm\",\"workload\":20},\"happy_hours_settings\":[{\"timeframe\":\"Timeframe1\",\"time_of_weeks\":[1,1,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"09\",\"minute\":\"00\"},\"to\":{\"hour\":\"18\",\"minute\":\"19\"},\"status\":1},{\"timeframe\":\"Timeframe2\",\"time_of_weeks\":[1,1,1,1,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe3\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe4\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe5\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe6\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe7\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe8\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe9\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1},{\"timeframe\":\"Timeframe10\",\"time_of_weeks\":[0,0,0,0,0,0,0],\"type\":1,\"from\":{\"hour\":\"00\",\"minute\":\"00\"},\"to\":{\"hour\":\"00\",\"minute\":\"00\"},\"status\":1}],\"printers\":[{\"number\":1,\"ip\":\"\",\"name\":\"\",\"language\":\"default\",\"group_name\":\"\",\"is_change_color_name\":true,\"options\":{\"is_check_sheet\":true,\"is_print_time\":false,\"is_print_next_part\":false,\"is_beep\":false,\"is_check_sheet_equal_items\":true}},{\"number\":2,\"ip\":\"\",\"name\":\"\",\"language\":\"default\",\"group_name\":\"\",\"is_change_color_name\":true,\"options\":{\"is_check_sheet\":true,\"is_print_time\":true,\"is_print_next_part\":false,\"is_beep\":true,\"is_check_sheet_equal_items\":true}},{\"number\":3,\"ip\":\"\",\"name\":\"\",\"language\":\"default\",\"group_name\":\"\",\"is_change_color_name\":false,\"options\":{\"is_check_sheet\":false,\"is_print_time\":false,\"is_print_next_part\":false,\"is_beep\":false,\"is_check_sheet_equal_items\":false}}],\"language\":{\"languages\":[{\"name\":\"Vietnam\",\"key\":\"vi-VN\"},{\"name\":\"UnitedStates\",\"key\":\"en-US\"},{\"name\":\"Germany\",\"key\":\"de-DE\"}]},\"location\":{\"image\":\"\",\"redirectUrl\":\"\",\"latitude\":\"\",\"longitude\":\"\"},\"openTime\":[{\"name\":0,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"06:00\",\"to\":\"22:02\"}]},{\"name\":1,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"11:00\",\"to\":\"22:02\"}]},{\"name\":2,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"06:00\",\"to\":\"22:02\"}]},{\"name\":3,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"10:39\",\"to\":\"22:02\"}]},{\"name\":4,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"06:40\",\"to\":\"22:02\"}]},{\"name\":5,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"08:00\",\"to\":\"22:02\"}]},{\"name\":6,\"time\":[{\"from\":\"07:00\",\"to\":\"10:00\"},{\"from\":\"06:00\",\"to\":\"22:02\"}]}],\"homePageSlidesShow\":[{\"title\":\"\",\"description\":\"\",\"imageUrl\":\"\",\"url\":\"\",\"actionType\":\"\"}],\"loginSlidesShow\":[{\"title\":\"\",\"description\":\"\",\"imageUrl\":\"\",\"url\":\"\",\"actionType\":\"\"}],\"timeOfDay\":{\"morning\":{\"start\":\"8:00\",\"end\":\"12:00\"},\"afternoon\":{\"start\":\"13:00\",\"end\":\"17:00\"},\"evening\":{\"start\":\"18:00\",\"end\":\"22:00\"}},\"payments\":[{\"id\":1,\"name\":\"Cash\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":1},{\"id\":2,\"name\":\"Visa\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":2},{\"id\":3,\"name\":\"EC\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":3},{\"id\":4,\"name\":\"PromotionCode\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":4},{\"id\":5,\"name\":\"Paypal\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":5},{\"id\":6,\"name\":\"Mastercard\",\"icon\":\"\",\"isOn\":false,\"isDefault\":true,\"iconDisable\":\"\",\"code\":6}]}";

    public static final String PHONE_PATTERN = "^0[35789][0-9]{8}$";
    public static final String EMAIL_PATTERN = "^(?!.*[.]{2,})[a-zA-Z0-9.%]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String USERNAME_PATTERN = "^(?=.{3,20}$)(?!.*[_.]{2})[a-zA-Z][a-zA-Z0-9_]*[a-zA-Z0-9]$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
    public static final String SERVER_ID_PATTERN = "^[a-zA-Z0-9]{1,20}$";

    public static final int PLATFORM_IOS = 1;
    public static final int PLATFORM_ANDROID = 2;

    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_EMPLOYEE = 2;
    public static final Integer USER_KIND_USER = 3;

    public static final Integer USER_GENDER_MALE = 1;
    public static final Integer USER_GENDER_FEMALE = 2;
    public static final Integer USER_GENDER_OTHER = 3;

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_DELETE = -2;

    public static final Integer GROUP_KIND_ADMIN = 1;
    public static final Integer GROUP_KIND_MANAGER = 2;
    public static final Integer GROUP_KIND_EMPLOYEE = 3;
    public static final Integer GROUP_KIND_USER = 1000;
    public static final Integer GROUP_KIND_USER_VIP = 1001;

    public static final Integer GENDER_MALE = 1;
    public static final Integer GENDER_FEMALE = 2;
    public static final Integer GENDER_OTHER = 3;

    private BaseConstant() {
        throw new IllegalStateException("Utility class");
    }
}
