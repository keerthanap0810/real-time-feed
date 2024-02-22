package com.hugosave.internprojectk.constants;

public class ConfigConstants {
    public static final String PARAM_PAGE_TOKEN = "page-token";
    public static final String PARAM_REVERSE_PAGE_TOKEN = "reverse-page-token";
    public static final String PARAM_FROM_TS = "from-timestamp";
    public static final String PARAM_TO_TS = "to-timestamp";
    public static final String PARAM_PAGE_SIZE = "page-size";
    public static final String PARAM_PAGE_NO = "page-no";


    public static final String HEADER_AUTHORIZATION = "authorization";
    public static final String BEARER_TOKEN = "Bearer";

    public static final String CURRENCY = "INR";
    public static final String WEIGHT = "g";

    public static final String SCHEDULER_SQS = "PT1S";
    public static final String SCHEDULER_PRICE = "PT5M";

    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    //AWS Cognito Configuration Constants
    public static final String AWS_COGNITO_USERNAME = "USERNAME";
    public static final String AWS_COGNITO_PASSWORD = "PASSWORD";
    public static final String AWS_COGNITO_SECRET_HASH = "SECRET_HASH";
    public static final String AWS_COGNITO_HASH_ALGORITHM = "HmacSHA256";

    // HikariCP Configuration Constants
    public static final int HIKARI_MINIMUM_IDLE = 1;
    public static final int HIKARI_MAXIMUM_POOL_SIZE = 5;
    public static final int HIKARI_CONNECTION_TIMEOUT = 30000;
    public static final int HIKARI_IDLE_TIMEOUT = 10000;
    public static final int HIKARI_MAX_LIFETIME = 900000;
    public static final int HIKARI_KEEPALIVE_TIME = 30000;
    public static final String HIKARI_CONNECTION_TEST_QUERY = "SELECT 1";

    //SQS Configuration Constants
    public static final int SQS_MAX_NUMBER_OF_MESSAGES = 10;
    public static final int SQS_WAIT_TIME_SECONDS = 0;
    public static final int SQS_VISIBILITY_TIMEOUT = 300;
    public static final int SQS_MAX_COUNT = 12;
    public static final String SQS_MESSAGE_ATTRIBUTE_NAMES = "All";
}
