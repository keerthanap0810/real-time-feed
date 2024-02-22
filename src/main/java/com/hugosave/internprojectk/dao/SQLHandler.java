package com.hugosave.internprojectk.dao;

import software.amazon.awssdk.services.sqs.endpoints.internal.Value;

public class SQLHandler {

    //ASSET QUERIES
    public static final String INSERT_ASSET_ENTRY = "INSERT INTO AssetInfo (asset_code, value, ask, bid, weight, currency, create_ts) VALUES ";

    public static final String FETCH_FORWARD_PAGINATED_ASSETS =
        "SELECT asset_code, value, ask, bid, weight, currency, create_ts FROM AssetInfo WHERE create_ts < ? ORDER BY create_ts DESC LIMIT ? OFFSET  ?";

    public static final String FETCH_BACKWARD_PAGINATED_ASSETS =
        "SELECT asset_code, value, ask, bid, weight, currency, create_ts FROM AssetInfo WHERE create_ts >= ? ORDER BY create_ts DESC LIMIT ? OFFSET ?";

    public static final String FETCH_BETWEEN_TIMESTAMP_PAGINATED_ASSETS =
        "SELECT asset_code, value, ask, bid, weight, currency, create_ts FROM AssetInfo WHERE create_ts BETWEEN ? AND ? ORDER BY create_ts DESC LIMIT ? OFFSET ?";

    public static final String ASSET_DEFAULT_PAGINATION =
        "SELECT asset_code, value, ask, bid, weight, currency, create_ts FROM AssetInfo WHERE create_ts < ? ORDER BY create_ts DESC LIMIT ? OFFSET ?";


    //TRANSACTION QUERIES
    public static final String INSERT_TRANSACTION_ENTRY = "INSERT INTO TransactionInfo (user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String FETCH_FORWARD_PAGINATED_TRANSACTIONS =
        "SELECT user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id, create_ts FROM TransactionInfo WHERE create_ts < ? AND user_id = ?  ORDER BY create_ts DESC LIMIT ? OFFSET  ?";

    public static final String FETCH_BACKWARD_PAGINATED_TRANSACTIONS =
        "SELECT user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id, create_ts FROM TransactionInfo WHERE create_ts >= ? AND user_id = ?  ORDER BY create_ts DESC LIMIT ? OFFSET ?";

    public static final String FETCH_BETWEEN_TIMESTAMP_PAGINATED_TRANSACTIONS =
        "SELECT user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id, create_ts FROM TransactionInfo WHERE create_ts BETWEEN ? AND ?  AND  user_id = ? ORDER BY create_ts DESC LIMIT ? OFFSET ?";

    public static final String TRANSACTION_DEFAULT_PAGINATION =
        "SELECT user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id, create_ts FROM TransactionInfo WHERE user_id = ?  ORDER BY create_ts DESC LIMIT ? OFFSET ?";

    public static final String UPDATE_TRANSACTION_STATUS = "UPDATE TransactionInfo SET status = ? WHERE transaction_id = ?";

    public static final String UPDATE_USER_BALANCE = "UPDATE UserBalance SET balance = balance + ? WHERE user_id = ?";

    public static final String UPDATE_USER_ASSET = "INSERT INTO UserAsset (user_id, asset_code, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + ?";

    public static final String SELECT_TRANSACTION_BY_ID = "SELECT user_id, transaction_id, transaction_type, status, total_amount, asset_code, quantity, value, currency, ref_id, create_ts FROM transactions WHERE transaction_id = ?";

    //USER QUERIES
    public static final String SELECT_USER_BALANCE = "SELECT user_id, balance FROM UserBalance WHERE user_id = ?";

    public static final String SELECT_USER_ASSETS = "SELECT user_id, asset_code, quantity FROM UserAsset WHERE user_id = ?";

    public static final String INSERT_USER_BALANCE = "INSERT INTO UserBalance (user_id, balance) " +
        "VALUES (?, ?)";

    public static final String INSERT_USER = "INSERT INTO User (user_id, user_name, email) VALUES (?, ?, ?)";
}
