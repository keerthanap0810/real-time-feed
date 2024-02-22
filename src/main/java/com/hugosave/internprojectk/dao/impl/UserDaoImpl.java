package com.hugosave.internprojectk.dao.impl;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.dao.SQLHandler;
import com.hugosave.internprojectk.dao.UserDao;
import com.hugosave.internprojectk.infrastructure.AwsDataSource;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private final HikariDataSource hikariDataSource;
    private final AwsDataSource awsDataSource;

    public UserDaoImpl(AwsDataSource awsDataSource) {
        this.hikariDataSource = awsDataSource.getDataSource();
        this.awsDataSource = awsDataSource;
    }

    public UserAssetAndBalanceEntity getUserAssetsAndBalanceById(String userId) {
        UserAssetAndBalanceEntity userAssetsAndBalance = null;
        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
            try (PreparedStatement balanceStatement = connection.prepareStatement(SQLHandler.SELECT_USER_BALANCE)) {
                balanceStatement.setString(1, userId);
                try (ResultSet balanceResultSet = balanceStatement.executeQuery()) {
                    if (balanceResultSet.next()) {
                        UserBalanceEntity userBalance = UserBalanceEntity.newBuilder()
                            .setUserId(balanceResultSet.getString("user_id"))
                            .setBalance(balanceResultSet.getDouble("balance"))
                            .build();

                        List<UserAssetEntity> userAssets = new ArrayList<>();
                        try (PreparedStatement assetsStatement = connection.prepareStatement(SQLHandler.SELECT_USER_ASSETS)) {
                            assetsStatement.setString(1, userId);
                            try (ResultSet assetsResultSet = assetsStatement.executeQuery()) {
                                while (assetsResultSet.next()) {
                                    UserAssetEntity userAsset = UserAssetEntity.newBuilder()
                                        .setUserId(assetsResultSet.getString("user_id"))
                                        .setAssetCode(assetsResultSet.getString("asset_code"))
                                        .setQuantity(assetsResultSet.getInt("quantity"))
                                        .build();
                                    userAssets.add(userAsset);
                                }
                            }
                        }

                        userAssetsAndBalance = UserAssetAndBalanceEntity.newBuilder()
                            .setUserBalance(userBalance)
                            .addAllUserAssets(userAssets)
                            .build();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving user assets and balance by ID from the database", e);
            throw new DatabaseException("Error retrieving user assets and balance by ID from the database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    LOGGER.error("Error closing database connection", closeException);
                }
            }
        }
        return userAssetsAndBalance;
    }


    public int insertUserBalance(String userId) {
        int rowUpdated = 0;

        try {
            try (PreparedStatement statement = awsDataSource.getPreparedStatement(SQLHandler.INSERT_USER_BALANCE)) {
                statement.setString(1, userId);
                statement.setDouble(2, 0.0);
                rowUpdated = statement.executeUpdate();

                LOGGER.info("Successfully insert user balance entry to the database");
            }
        } catch (SQLException e) {
            LOGGER.error("Error inserting user balance into the database", e);
            throw new DatabaseException(ExceptionStatusCode.UNABLE_TO_ADD_INITIAL_BALANCE);
        }
        return rowUpdated;
    }


    public int insertUser(UserEntity userEntity) {
        int rowUpdated = 0;

        try {

            try (PreparedStatement statement = awsDataSource.getPreparedStatement(SQLHandler.INSERT_USER)) {
                statement.setString(1, userEntity.getUserId());
                statement.setString(2, userEntity.getUserName());
                statement.setString(3, userEntity.getEmail());

                rowUpdated = statement.executeUpdate();
                LOGGER.info("Successfully insert user entry to the database");
            }
        } catch (SQLException e) {
            LOGGER.error("Error inserting user into the database", e);
            throw new DatabaseException(ExceptionStatusCode.UNABLE_TO_ADD_USER);
        }
        return rowUpdated;
    }
}
