package com.hugosave.internprojectk.scheduler;

import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.infrastructure.SqsService;
import com.hugosave.internprojectk.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionSQSScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSQSScheduler.class);

    private final SqsService sqsService;
    private final TransactionService transactionService;


    public TransactionSQSScheduler(SqsService sqsService, TransactionService transactionService) {
        this.sqsService = sqsService;
        this.transactionService = transactionService;

    }

    @Scheduled(fixedRateString = ConfigConstants.SCHEDULER_SQS)
    public void listenSqsMessages() {
        try {
            LOGGER.trace("Scheduled task started: Fetching Messages");
            ReceiveMessageResponse receiveMessageResponse = sqsService.listenForMessages();
            final Map<String, AssetPriceRecord> assetPriceMap = new HashMap<>();
            for (Message message : receiveMessageResponse.messages()) {
               String finalStatus =  transactionService.handleSQSTransaction(message, assetPriceMap);
                LOGGER.info("Status of Transaction is {} for {}", finalStatus, message.body());
            }

        } catch (Exception e) {
            LOGGER.error("Error in scheduled task listening to SQS: {}", e.getMessage(), e);
        }
    }

}
