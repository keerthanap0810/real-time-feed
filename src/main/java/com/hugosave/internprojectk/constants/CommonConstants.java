package com.hugosave.internprojectk.constants;

public class CommonConstants {

    public enum TransactionType {
        BUY,
        SELL
    }

    public enum TransactionStatus {
        SETTLED,
        PENDING,
        FAILED
    }

    public enum ASSET_CODE {
        XAU,
        XAG,
        XPD,
        XPT
    }

    public static final String USER_EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TRANSACTION_ID = "transaction-id";



}
