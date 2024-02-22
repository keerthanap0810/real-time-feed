package com.hugosave.internprojectk.infrastructure;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.ApplicationProperties;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.constants.ResourceConstants;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.Utils;
import com.hugosave.internprojectk.utilities.utils.exception.BadRequestException;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import com.hugosave.internprojectk.utilities.utils.exception.UnauthorizedRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AwsCognito {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsCognito.class);

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final ApplicationProperties applicationProperties;

    public AwsCognito(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public UserAuthResponseDTO initiateAuth(UserOnboardDTO userCredentials) {
        try {
            String userName = userCredentials.getUserName();
            String password = userCredentials.getPassword();

            Map<String, String> authParameters = new HashMap<>();
            authParameters.put(ConfigConstants.AWS_COGNITO_USERNAME, userName);
            authParameters.put(ConfigConstants.AWS_COGNITO_PASSWORD, password);
            authParameters.put(ConfigConstants.AWS_COGNITO_SECRET_HASH, calculateSecretHash(applicationProperties.getAwsCognitoClientId(), applicationProperties.getAwsCognitoClientSecret(), userName));

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .clientId(applicationProperties.getAwsCognitoClientId())
                .userPoolId(applicationProperties.getAwsCognitoUserPoolId())
                .authParameters(authParameters)
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .build();

            AdminInitiateAuthResponse response = cognitoIdentityProviderClient.adminInitiateAuth(authRequest);

            String accessToken = response.authenticationResult().accessToken();
            String refreshToken = response.authenticationResult().refreshToken();
            String userId = Utils.getUserId(accessToken);

            LOGGER.info("Initiated auth successfully");

            return UserAuthResponseDTO.newBuilder()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setUserId(userId)
                .setUserName(userName)
                .build();

        } catch (CognitoIdentityProviderException e) {
            LOGGER.error("Exception during init auth", e);
            throw new UnauthorizedRequestException(ExceptionStatusCode.INVALID_CREDENTIALS);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during initiation of auth", e);
            throw new CustomException("An unexpected error occurred.", e);
        }
    }

    public UserOnboardDTO signUp(UserOnboardDTO userCredentials) {
        try {
            String userName = userCredentials.getUserName();
            String password = userCredentials.getPassword();
            String email = userCredentials.getEmail();
            AttributeType userAttrs = AttributeType.builder()
                .name(CommonConstants.USER_EMAIL)
                .value(email)
                .build();
            String secretHash = calculateSecretHash(applicationProperties.getAwsCognitoClientId(), applicationProperties.getAwsCognitoClientSecret(), userName);
            List<AttributeType> userAttrsList = Collections.singletonList(userAttrs);

            SignUpRequest signUpRequest = SignUpRequest.builder()
                .userAttributes(userAttrsList)
                .username(userName)
                .clientId(applicationProperties.getAwsCognitoClientId())
                .password(password)
                .secretHash(secretHash)
                .build();

            cognitoIdentityProviderClient.signUp(signUpRequest);

            AdminConfirmSignUpRequest adminConfirmSignUpRequest = AdminConfirmSignUpRequest.builder()
                .userPoolId(applicationProperties.getAwsCognitoUserPoolId())
                .username(userName)
                .build();
            cognitoIdentityProviderClient.adminConfirmSignUp(adminConfirmSignUpRequest);

            return UserOnboardDTO.newBuilder().setUserName(userCredentials.getUserName()).build();
        } catch (CognitoIdentityProviderException e) {
            LOGGER.error("Exception during sign up", e);
            throw new CustomException("An unexpected error occurred during sign up.", e);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during sign up", e);
            throw new CustomException("An unexpected error occurred.", e);
        }
    }

    private static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = ConfigConstants.AWS_COGNITO_HASH_ALGORITHM;
        SecretKeySpec signingKey = new SecretKeySpec(
            userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
            HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            LOGGER.error("An error occurred while calculating the secret hash", e);
            throw new CustomException("An unexpected error occurred while calculating the secret hash.");
        }
    }
}
