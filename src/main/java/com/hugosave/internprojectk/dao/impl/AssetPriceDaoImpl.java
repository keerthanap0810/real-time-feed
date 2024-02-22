package com.hugosave.internprojectk.dao.impl;

import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.AssetPriceEntityList;
import com.hugosave.internprojectk.dao.AssetPriceDao;
import com.hugosave.internprojectk.dao.SQLHandler;
import com.hugosave.internprojectk.infrastructure.AwsDataSource;
import com.hugosave.internprojectk.utilities.mapper.EntityMapper;
import com.hugosave.internprojectk.utilities.utils.Utils;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import com.mysql.cj.util.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AssetPriceDaoImpl implements AssetPriceDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPriceDaoImpl.class);

    private final HikariDataSource hikariDataSource;

    public AssetPriceDaoImpl(AwsDataSource awsDataSource) {
        this.hikariDataSource = awsDataSource.getDataSource();
    }

    public void addAssetEntries(List<AssetPriceEntity> assetInfoList) {
        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
            connection.setAutoCommit(false);

            StringBuilder query = new StringBuilder(SQLHandler.INSERT_ASSET_ENTRY);
            for(int i = 0; i < assetInfoList.size(); i++){
                query.append("(?, ?, ?, ?, ?, ?, ?)");
                if(i < assetInfoList.size()-1)
                    query.append(",");
            }

            try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
                int offset = 0;
                for(AssetPriceEntity assetInfo : assetInfoList){
                    statement.setString(1 + offset, assetInfo.getAssetCode());
                    statement.setDouble(2 + offset, assetInfo.getValue());
                    statement.setDouble(3 + offset, assetInfo.getAsk());
                    statement.setDouble(4 + offset, assetInfo.getBid());
                    statement.setString(5 + offset, assetInfo.getWeight());
                    statement.setString(6 + offset, assetInfo.getCurrency());
                    statement.setTimestamp(7 + offset, new Timestamp(assetInfo.getTimestamp().getSeconds() * 1000));
                    offset += 7;
                }

                statement.executeUpdate();
            }

            connection.commit();
            LOGGER.info("Successfully added asset entries to the database");
        } catch (SQLException e) {
            LOGGER.error("Error adding asset entries to the database", e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    LOGGER.error("Error rolling back transaction", rollbackException);
                }
            }
            throw new DatabaseException("Error adding asset entries to the database", e);
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

    public AssetPriceEntityList getAssetInfoPaginated(
        String fromTs, String toTs,
        Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp,
        int pageSize, int pageNo) {

        Connection connection = null;
        AssetPriceEntityList.Builder realTimePriceListBuilder = AssetPriceEntityList.newBuilder();
        int offset = (pageNo - 1) * pageSize;

        try {
            connection = hikariDataSource.getConnection();
            if (!StringUtils.isNullOrEmpty(fromTs) && !StringUtils.isNullOrEmpty(toTs)) {
                Timestamp fromTimestamp = Timestamp.valueOf(fromTs);
                Timestamp toTimestamp = Timestamp.valueOf(toTs);
                getAssetBetweenTimestamp(fromTimestamp, toTimestamp, pageSize, connection, realTimePriceListBuilder, offset);
            } else if (pageTokenTimestamp != null && reversePageTokenTimestamp != null) {
                getAssetBetweenTimestamp(pageTokenTimestamp, reversePageTokenTimestamp, pageSize, connection, realTimePriceListBuilder, offset);
            } else if (pageTokenTimestamp != null || !StringUtils.isNullOrEmpty(fromTs)) {
                Timestamp timestampToUse = pageTokenTimestamp != null ? pageTokenTimestamp : Timestamp.valueOf(fromTs);

                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_FORWARD_PAGINATED_ASSETS)) {
                    statement.setTimestamp(1, timestampToUse);
                    statement.setInt(2, pageSize);
                    statement.setInt(3, offset);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        processResultSet(resultSet, realTimePriceListBuilder);
                    }
                }
            } else if (reversePageTokenTimestamp != null || !StringUtils.isNullOrEmpty(toTs)) {
                Timestamp timestampToUse = reversePageTokenTimestamp != null ? reversePageTokenTimestamp : Timestamp.valueOf(toTs);

                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_BACKWARD_PAGINATED_ASSETS)) {
                    statement.setTimestamp(1, timestampToUse);
                    statement.setInt(2, pageSize);
                    statement.setInt(3, offset);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        processResultSet(resultSet, realTimePriceListBuilder);
                    }
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(SQLHandler.ASSET_DEFAULT_PAGINATION)) {
                    statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    statement.setInt(2, pageSize);
                    statement.setInt(3, offset);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        processResultSet(resultSet, realTimePriceListBuilder);
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            LOGGER.error("Error retrieving paginated asset information from the database", e);
            throw new DatabaseException("Error retrieving paginated asset information from the database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    LOGGER.error("Error closing database connection", closeException);
                }
            }
        }


        return realTimePriceListBuilder.build();
    }

    private void getAssetBetweenTimestamp(Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp, int pageSize, Connection connection, AssetPriceEntityList.Builder realTimePriceListBuilder, int offset) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQLHandler.FETCH_BETWEEN_TIMESTAMP_PAGINATED_ASSETS)) {
            statement.setTimestamp(1, pageTokenTimestamp);
            statement.setTimestamp(2, reversePageTokenTimestamp);
            statement.setInt(3, pageSize);
            statement.setInt(4, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                processResultSet(resultSet, realTimePriceListBuilder);
            }
        }
    }


    private void processResultSet(ResultSet resultSet, AssetPriceEntityList.Builder realTimePriceListBuilder) throws SQLException {
        while (resultSet.next()) {

            AssetPriceEntity.Builder realTimePriceEntityBuilder = EntityMapper.createRealTimePriceEntityBuilder(resultSet);
            realTimePriceListBuilder.addAssetPrices(realTimePriceEntityBuilder.build());
        }
    }
}
