package com.hugosave.internprojectk.infrastructure;

import com.hugosave.internprojectk.constants.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class AwsSecretManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSecretManager.class);

    private final ApplicationProperties applicationProperties;

    public AwsSecretManager(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public static SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
            .region(Region.EU_NORTH_1)
            .build();
    }

    public String getSecret() {
        SecretsManagerClient client = secretsManagerClient();
        String secretName = applicationProperties.getAwsSecretManagerSecretName();
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();

        try {
            GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
            return getSecretValueResponse.secretString();
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret: {}", secretName, e);
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }
}
