package com.hugosave.internprojectk.constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {
    private final String providerApiUrl;
    private final String databaseUrl;
    private final String databaseDriverClassName;
    private final String awsSecretManagerSecretName;
    private final String awsSqsQueryUrl;
    private final String awsCognitoUserPoolId;
    private final String awsCognitoClientId;
    private final String awsCognitoClientSecret;

    public ApplicationProperties(Environment env) {
        this.providerApiUrl = env.getRequiredProperty("provider.api.url");
        this.databaseUrl = env.getRequiredProperty("spring.datasource.url");
        this.databaseDriverClassName = env.getRequiredProperty("spring.datasource.driver-class-name");
        this.awsSecretManagerSecretName = env.getRequiredProperty("aws.secret-manager.secret-name");
        this.awsSqsQueryUrl = env.getRequiredProperty("aws.sqs-query-url");
        this.awsCognitoUserPoolId = env.getProperty("aws.cognito.user-pool-id");
        this.awsCognitoClientId = env.getProperty("aws.cognito.client-id");
        this.awsCognitoClientSecret = env.getProperty("aws.cognito.client-secret");
    }

    public String getProviderApiUrl() {
        return providerApiUrl;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseDriverClassName() {
        return databaseDriverClassName;
    }

    public String getAwsSecretManagerSecretName() {
        return awsSecretManagerSecretName;
    }

    public String getAwsSqsQueryUrl() {
        return awsSqsQueryUrl;
    }

    public String getAwsCognitoUserPoolId() {
        return awsCognitoUserPoolId;
    }

    public String getAwsCognitoClientId() {
        return awsCognitoClientId;
    }

    public String getAwsCognitoClientSecret() {
        return awsCognitoClientSecret;
    }
}
