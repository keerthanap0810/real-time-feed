package com.hugosave.internprojectk.dao.impl;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.DbConstants;
import com.hugosave.internprojectk.dao.SQLHandler;
import com.hugosave.internprojectk.dao.TransactionDao;
import com.hugosave.internprojectk.infrastructure.AwsDataSource;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.utilities.mapper.EntityMapper;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import com.mysql.cj.util.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;


@Repository
public class TransactionDaoImpl implements TransactionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDaoImpl.class);

    private final HikariDataSource hikariDataSource;
    private final AwsDataSource awsDataSource;

    public TransactionDaoImpl(AwsDataSource awsDataSource){
        this.hikariDataSource = awsDataSource.getDataSource();
        this.awsDataSource = awsDataSource;
    }

    public TransactionEntity addTransactionEntry(TransactionEntity transactionData) {
        try {

            try (PreparedStatement statement =  awsDataSource.getPreparedStatement(SQLHandler.INSERT_TRANSACTION_ENTRY)) {
                statement.setString(1, transactionData.getUserId());
                statement.setString(2, transactionData.getTransactionId());
                statement.setString(3, transactionData.getTransactionType());
                statement.setString(4, transactionData.getStatus());
                statement.setDouble(5, transactionData.getTotalAmount());
                statement.setString(6, transactionData.getAssetCode());
                statement.setInt(7, transactionData.getQuantity());
                statement.setDouble(8, transactionData.getValue());
                statement.setString(9, transactionData.getCurrency());
                statement.setString(10, transactionData.getRefId());

                statement.executeUpdate();
            }
            LOGGER.info("Successfully added transaction entry to the database");
        } catch (SQLException e) {
            LOGGER.error("Error adding transaction entry to the database", e);
            throw new DatabaseException("Error adding transaction entry to the database", e);
        }
        return transactionData;
    }

    public TransactionHistoryEntity getTransactionsByUserIdPaginated(
        String fromTs, String toTs,
        Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp,
        String userId, int pageNo, int pageSize) {

        Connection connection = null;
        TransactionHistoryEntity.Builder transactionListBuilder = TransactionHistoryEntity.newBuilder();
        int offset = (pageNo - 1) * pageSize;

        try {
            connection = hikariDataSource.getConnection();

            if (!StringUtils.isNullOrEmpty(fromTs) && !StringUtils.isNullOrEmpty(toTs)) {
                Timestamp fromTimestamp = Timestamp.valueOf(fromTs);
                Timestamp toTimestamp = Timestamp.valueOf(toTs);
                getTransactionsBetweenTimestamp(fromTimestamp, toTimestamp, userId, pageSize, connection, transactionListBuilder, offset);
            } else if (pageTokenTimestamp != null && reversePageTokenTimestamp != null) {
                getTransactionsBetweenTimestamp(pageTokenTimestamp, reversePageTokenTimestamp, userId, pageSize, connection, transactionListBuilder, offset);
            } else if (pageTokenTimestamp != null || !StringUtils.isNullOrEmpty(fromTs)) {
                Timestamp timestampToUse = pageTokenTimestamp != null ? pageTokenTimestamp : Timestamp.valueOf(fromTs);

                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_FORWARD_PAGINATED_TRANSACTIONS)) {
                    statement.setTimestamp(1, timestampToUse);
                    statement.setString(2, userId);
                    statement.setInt(3, pageSize);
                    statement.setInt(4, offset);

                    processTransactionResultSet(statement, transactionListBuilder);
                }
            } else if (reversePageTokenTimestamp != null || !StringUtils.isNullOrEmpty(toTs)) {
                Timestamp timestampToUse = reversePageTokenTimestamp != null ? reversePageTokenTimestamp : Timestamp.valueOf(toTs);

                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_BACKWARD_PAGINATED_TRANSACTIONS)) {
                    statement.setTimestamp(1, timestampToUse);
                    statement.setString(2, userId);
                    statement.setInt(3, pageSize);
                    statement.setInt(4, offset);

                    processTransactionResultSet(statement, transactionListBuilder);
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.TRANSACTION_DEFAULT_PAGINATION)) {
                    statement.setString(1, userId);
                    statement.setInt(2, pageSize);
                    statement.setInt(3, offset);

                    processTransactionResultSet(statement, transactionListBuilder);
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            LOGGER.error("Error while retrieving paginated transactions from the database", e);
            throw new DatabaseException("Error while retrieving paginated transactions from the database", e);
        }

        return transactionListBuilder.build();
    }

    public int updateTransactionStatus(String transactionId, String status) {
        int rowsUpdated = 0;
        try {
            try (PreparedStatement statement = awsDataSource.getPreparedStatement(SQLHandler.UPDATE_TRANSACTION_STATUS)) {
                statement.setString(1, status);
                statement.setString(2, transactionId);
                rowsUpdated = statement.executeUpdate();
            }

            LOGGER.info("Successfully updated transaction status");
        } catch (SQLException e) {
            LOGGER.error("Error updating transaction status in the database", e);
            throw new DatabaseException("Error updating transaction status in the database", e);
        }
        return rowsUpdated;
    }

    public int updateUserBalance(String userId, double totalAmount) {
        int rowsUpdated = 0;
        try {
            try (PreparedStatement updateBalanceStatement = awsDataSource.getPreparedStatement(SQLHandler.UPDATE_USER_BALANCE)) {

                updateBalanceStatement.setDouble(1, totalAmount);
                updateBalanceStatement.setString(2, userId);
                rowsUpdated = updateBalanceStatement.executeUpdate();
            }

            LOGGER.info("Successfully updated user balance");
        } catch (SQLException e) {
            LOGGER.error("Error updating user balance in the database", e);
            throw new DatabaseException(ExceptionStatusCode.UNABLE_TO_UPDATE_BALANCE.getMessage(), e);
        }
        return rowsUpdated;
    }


    public int updateUserAsset(String userId, String assetCode, int quantity) {
        int rowsUpdated = 0;
        try {

            try (PreparedStatement updateUserAssetStatement = awsDataSource.getPreparedStatement(SQLHandler.UPDATE_USER_ASSET)) {

                updateUserAssetStatement.setString(1, userId);
                updateUserAssetStatement.setString(2, assetCode);
                updateUserAssetStatement.setInt(3, quantity);
                updateUserAssetStatement.setInt(4, quantity);
                rowsUpdated = updateUserAssetStatement.executeUpdate();
            }
            LOGGER.info("Successfully updated user asset");
        } catch (SQLException e) {
            LOGGER.error("Error updating user asset in the database", e);
            throw new DatabaseException(ExceptionStatusCode.UNABLE_TO_UPDATE_ASSET_ENTRY.getMessage(), e);
        }
        return rowsUpdated;
    }

    public TransactionEntity getTransactionByTransactionId(String transactionId, String status) {
        Connection connection = null;
        TransactionEntity transactionData = null;

        try {
            connection = hikariDataSource.getConnection();

            try (PreparedStatement statement = connection.prepareStatement(SQLHandler.SELECT_TRANSACTION_BY_ID)) {
                statement.setString(1, transactionId);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    transactionData = TransactionEntity.newBuilder()
                    .setUserId(resultSet.getString(DbConstants.USER_ID))
                    .setTransactionId(resultSet.getString(DbConstants.TRANSACTION_ID))
                    .setTransactionType(resultSet.getString(DbConstants.TRANSACTION_TYPE))
                    .setStatus(resultSet.getString(DbConstants.STATUS))
                    .setTotalAmount(resultSet.getDouble(DbConstants.TOTAL_AMOUNT))
                    .setAssetCode(resultSet.getString(DbConstants.ASSET_CODE))
                    .setQuantity(resultSet.getInt(DbConstants.QUANTITY))
                    .setValue(resultSet.getDouble(DbConstants.VALUE))
                    .setCurrency(resultSet.getString(DbConstants.CURRENCY))
                    .setRefId(resultSet.getString(DbConstants.REF_ID))
                        .build();
                }

            } catch (SQLException e) {
                LOGGER.error("Error fetching transaction data", e);
                throw new DatabaseException("Error fetching transaction data", e);
            }

            LOGGER.info("Successfully fetched transaction data from the database");
            if(transactionData != null && transactionData.getStatus().equals(status)){
                return null;
            }
            return transactionData;

        } catch (SQLException e) {
            LOGGER.error("Error fetching transaction data", e);
            throw new DatabaseException("Error fetching transaction data", e);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    LOGGER.error("Error closing database connection", closeException);
                }
            }
        }
    }

    private void getTransactionsBetweenTimestamp(Timestamp from, Timestamp to, String userId, int pageSize, Connection connection, TransactionHistoryEntity.Builder transactionListBuilder, int offset) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_BETWEEN_TIMESTAMP_PAGINATED_TRANSACTIONS)) {
            statement.setTimestamp(1, from);
            statement.setTimestamp(2, to);
            statement.setString(3, userId);
            statement.setInt(4, pageSize);
            statement.setInt(5, offset);

            processTransactionResultSet(statement, transactionListBuilder);
        }
    }

    private void processTransactionResultSet(PreparedStatement statement, TransactionHistoryEntity.Builder transactionListBuilder) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                AssetTransactionEntity.Builder assetTransactionDTO = EntityMapper.createAssetTransactionEntityBuilder(resultSet);
                transactionListBuilder.addTransactionHistory(assetTransactionDTO.build());
            }
        }
    }

}
