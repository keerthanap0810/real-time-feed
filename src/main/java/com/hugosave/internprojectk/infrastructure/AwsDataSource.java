package com.hugosave.internprojectk.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hugosave.internprojectk.constants.ApplicationProperties;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.constants.ResourceConstants;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class AwsDataSource {
    private final AwsSecretManager awsSecretManager;
    private final ApplicationProperties applicationProperties;
    private HikariDataSource hikariDataSource;

    private static final ThreadLocal<Connection> DB_CONNECTION_THREAD = new ThreadLocal<>();

    public AwsDataSource(AwsSecretManager awsSecretManager, ApplicationProperties applicationProperties) {
        this.awsSecretManager = awsSecretManager;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public HikariDataSource getDataSource() {
        String secret = awsSecretManager.getSecret();
        String url = applicationProperties.getDatabaseUrl();
        String driver = applicationProperties.getDatabaseDriverClassName();

        HikariConfig hikariConfig = createHikariConfig(secret, url, driver);
        hikariDataSource = new HikariDataSource(hikariConfig);
        return new HikariDataSource(hikariConfig);
    }

    private HikariConfig createHikariConfig(String secret, String url, String driver) {
        JsonObject secretJson = JsonParser.parseString(secret).getAsJsonObject();
        String username = secretJson.get(CommonConstants.USERNAME).getAsString();
        String password = secretJson.get(CommonConstants.PASSWORD).getAsString();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName(driver);

        hikariConfig.setAutoCommit(false);

        hikariConfig.setMinimumIdle(ConfigConstants.HIKARI_MINIMUM_IDLE);
        hikariConfig.setMaximumPoolSize(ConfigConstants.HIKARI_MAXIMUM_POOL_SIZE);
        hikariConfig.setConnectionTimeout(ConfigConstants.HIKARI_CONNECTION_TIMEOUT);
        hikariConfig.setIdleTimeout(ConfigConstants.HIKARI_IDLE_TIMEOUT);
        hikariConfig.setMaxLifetime(ConfigConstants.HIKARI_MAX_LIFETIME);
        hikariConfig.setKeepaliveTime(ConfigConstants.HIKARI_KEEPALIVE_TIME);
        hikariConfig.setConnectionTestQuery(ConfigConstants.HIKARI_CONNECTION_TEST_QUERY);
        return hikariConfig;
    }

    public void createDbConnection() throws SQLException {
        try {
            Connection connection = DB_CONNECTION_THREAD.get();
            if (connection == null || connection.isClosed()) {
                connection = hikariDataSource.getConnection();
                DB_CONNECTION_THREAD.set(connection);
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to create DB transaction.", ex);
        }
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return DB_CONNECTION_THREAD.get().prepareStatement(sql);
    }
    public void rollbackTransaction() throws SQLException {
        try {
            Connection connection = DB_CONNECTION_THREAD.get();
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
            else if(connection == null){
                throw new DatabaseException(ExceptionStatusCode.NO_DB_CONNECTION);
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to rollback transaction.", ex);
        }
    }

    public void closeDbConnection() throws SQLException {
        try {
            Connection connection = DB_CONNECTION_THREAD.get();
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.close();
                DB_CONNECTION_THREAD.remove();
            }
            else if(connection == null){
                throw new DatabaseException(ExceptionStatusCode.NO_DB_CONNECTION);
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to close DB transaction.", ex);
        }
    }


}
