package com.hugosave.internprojectk.aspect;

import com.hugosave.internprojectk.infrastructure.AwsDataSource;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@Aspect
public class TransactionAspect {

    private final AwsDataSource awsDataSource;

    public TransactionAspect(AwsDataSource awsDataSource) {
        this.awsDataSource = awsDataSource;
    }

    @Pointcut("execution(* com.hugosave.internprojectk.service.impl.*.processUserRequest(..))")
    private void processUserRequest() {}

    @Pointcut("execution(* com.hugosave.internprojectk.service.impl.*.handleSQSTransaction(..))")
    private void processSqsMessage() {}

    @Pointcut("execution(* com.hugosave.internprojectk.service.impl.*.userSignup(..))")
    private void processUserSignUp() {}
    @Before("processUserRequest() || processSqsMessage() || processUserSignUp()")
    public void beginTransaction() throws SQLException {
        try {
            awsDataSource.createDbConnection();
        } catch (SQLException e) {
            handleException(e, "error occurred while connecting to database");
        }
    }

    @AfterReturning("processUserRequest() || processSqsMessage() || processUserSignUp()")
    public void commitTransaction() {
        try {
            awsDataSource.closeDbConnection();
        } catch (SQLException e) {
            handleException(e, "error occurred while closing database connection");
        }
    }

    @AfterThrowing(pointcut = "processUserRequest() || processSqsMessage() || processUserSignUp()", throwing = "ex")
    public void rollbackTransaction(Exception ex) {
        try {
            awsDataSource.rollbackTransaction();
        } catch (SQLException e) {
            handleException(e, "error occurred while rolling back database transaction");
        }
    }

    private void handleException(SQLException e, String message) {
        throw new DatabaseException(message, e);
    }
}
