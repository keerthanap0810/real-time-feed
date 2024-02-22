package com.hugosave.internprojectk.utilities.utils;

import com.hugosave.intern.project.proto.RequestResponse;

public class ExceptionStatusCode {

    // Server
    public static final RequestResponse GOLDBROKER_SERVER_ERROR =
        RequestResponse.newBuilder().setMessage("Internal Server Error: Goldbroker is not responding").build();

    public static final RequestResponse INTERNAL_SERVER_ERROR =
        RequestResponse.newBuilder().setStatusCode("503").setMessage("Internal Server Error: Provider not responding").build();

    public static final RequestResponse NO_DB_CONNECTION =
        RequestResponse.newBuilder().setMessage("Service Unavailable: No database connection available").build();

    // User ledger
    public static final RequestResponse UNAUTHORIZED_404 =
        RequestResponse.newBuilder().setStatusCode("404").setMessage("Unauthorized: The requested resource could not be found").build();

    public static final RequestResponse UNABLE_TO_ADD_INITIAL_BALANCE =
        RequestResponse.newBuilder().setMessage("Internal Server Error: Unable to add user's initial balance to the database").build();

    public static final RequestResponse UNABLE_TO_UPDATE_BALANCE =
        RequestResponse.newBuilder().setMessage("Internal Server Error: Unable to update user's balance entry in the database").build();

    public static final RequestResponse UNABLE_TO_ADD_USER =
        RequestResponse.newBuilder().setMessage("Internal Server Error: Unable to add user to the database").build();

    public static final RequestResponse UNABLE_TO_UPDATE_ASSET_ENTRY =
        RequestResponse.newBuilder().setMessage("Internal Server Error: Unable to update user's asset entry in the database").build();

    // Transaction
    public static final RequestResponse INVALID_VALUES=
        RequestResponse.newBuilder().setMessage("Invalid price in transaction request").build();

    public static final RequestResponse BUY_TRANSACTION_FAILED =
        RequestResponse.newBuilder().setStatusCode("500").setMessage("Internal Server Error: Buy transaction failed").build();

    public static final RequestResponse SELL_TRANSACTION_FAILED =
        RequestResponse.newBuilder().setStatusCode("500").setMessage("Internal Server Error: Sell transaction failed").build();


    // User Registration/Login
    public static final RequestResponse USER_ALREADY_EXISTS =
        RequestResponse.newBuilder().setStatusCode("409").setMessage("Conflict: User already exists").build();

    public static final RequestResponse INVALID_CREDENTIALS =
        RequestResponse.newBuilder().setStatusCode("401").setMessage("Unauthorized: Invalid username or password").build();

    public static final RequestResponse MISSING_CREDENTIALS =
        RequestResponse.newBuilder().setStatusCode("400").setMessage("Bad Request: Missing username or password").build();
}
