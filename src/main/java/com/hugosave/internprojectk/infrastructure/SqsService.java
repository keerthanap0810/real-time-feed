package com.hugosave.internprojectk.infrastructure;

import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.intern.project.proto.AssetTransactionDTO;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.TransactionEntity;
import com.hugosave.internprojectk.constants.ApplicationProperties;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.constants.ResourceConstants;
import com.hugosave.internprojectk.facade.TransactionFacade;
import com.hugosave.internprojectk.provider.ProviderPriceService;
import com.hugosave.internprojectk.service.PriceService;
import com.hugosave.internprojectk.utilities.utils.exception.SQSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.hugosave.internprojectk.utilities.mapper.EntityMapper.deserializeSQSMessage;

@Service
public class SqsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsService.class);
    private final SqsClient sqsClient;
    private final ApplicationProperties applicationProperties;

    public SqsService(ApplicationProperties applicationProperties) {
        this.sqsClient = SqsClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
        this.applicationProperties = applicationProperties;
    }

    public void sendMessageToQueue(Map<String, MessageAttributeValue> messageAttributes) {
        try {
            String queueUrl = applicationProperties.getAwsSqsQueryUrl();
            SendMessageResponse sendMessageResponse = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody("User Transaction data of transaction id " + messageAttributes.get(CommonConstants.TRANSACTION_ID))
                .messageAttributes(messageAttributes)
                .build());

            LOGGER.info("Message sent to SQS: {}", sendMessageResponse.messageId());
        } catch (Exception e) {
            LOGGER.error("Error occurred while sending message to SQS.", e);
            throw new SQSException("Error occurred while sending message to SQS.", e);
        }
    }

    public ReceiveMessageResponse listenForMessages() {
        try {
            String queueUrl = applicationProperties.getAwsSqsQueryUrl();

            return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(ConfigConstants.SQS_MAX_NUMBER_OF_MESSAGES)
                .waitTimeSeconds(ConfigConstants.SQS_WAIT_TIME_SECONDS)
                .visibilityTimeout(ConfigConstants.SQS_VISIBILITY_TIMEOUT)
                .attributeNamesWithStrings(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT.toString())
                .messageAttributeNames(ConfigConstants.SQS_MESSAGE_ATTRIBUTE_NAMES)
                .build());
        } catch (Exception e) {
            LOGGER.error("Error occurred while listening for messages from SQS.", e);
            throw new SQSException("Error occurred while listening for messages from SQS.", e);
        }
    }

    public void deleteMessageFromQueue(String receiptHandle) {
        try {
            String queueUrl = applicationProperties.getAwsSqsQueryUrl();
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build());
            LOGGER.info("Deleted SQS message");
        } catch (Exception e) {
            LOGGER.error("Error occurred while deleting message from SQS.", e);
            throw new SQSException("Error occurred while deleting message from SQS.", e);
        }
    }
}
