package com.hugosave.internprojectk.service.impl;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.constants.DbConstants;
import com.hugosave.internprojectk.constants.ResourceConstants;
import com.hugosave.internprojectk.facade.TransactionFacade;
import com.hugosave.internprojectk.infrastructure.SqsService;
import com.hugosave.internprojectk.provider.ProviderPriceService;
import com.hugosave.internprojectk.service.TransactionService;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.utilities.mapper.EntityMapper;
import com.hugosave.internprojectk.utilities.mapper.MessageMapper;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.Utils;
import com.hugosave.internprojectk.utilities.utils.exception.BadRequestException;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import com.hugosave.internprojectk.utilities.utils.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.endpoints.internal.Value;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionFacade transactionFacade;
    private final SqsService sqsService;
    private final ProviderPriceService providerPriceService;

    public TransactionServiceImpl(TransactionFacade transactionFacade, SqsService sqsService, ProviderPriceService providerPriceService) {
        this.transactionFacade = transactionFacade;
        this.sqsService = sqsService;
        this.providerPriceService = providerPriceService;
    }


    public AssetTransactionDTO processUserRequest(String transactionType, String userId, UserTransactionRequestDTO data) {
        try {
            double totalAmount = data.getQuantity() * data.getValue();
            String uuid = UUID.randomUUID().toString();
            AssetPriceRecord assetPriceRecord = providerPriceService.getRealTimeAssetPrice(data.getAssetCode());
            long currentTimestampInSeconds = Instant.now().getEpochSecond();
            String status = CommonConstants.TransactionStatus.PENDING.toString();
            AssetTransactionDTO transactionData = null;
            TransactionEntity transactionEntity = EntityMapper.serializeTransactionEntity(transactionType, userId, data, totalAmount, uuid, currentTimestampInSeconds, status);
            transactionEntity = transactionFacade.addTransactionEntry(transactionEntity);

            if (Objects.equals(transactionType, CommonConstants.TransactionType.BUY.toString()) && data.getValue() == assetPriceRecord.getAsk()) {


                int balanceRowsUpdated = transactionFacade.updateUserBalance(userId, -totalAmount);
                int assetRowsUpdated = transactionFacade.updateUserAsset(userId, data.getAssetCode(), data.getQuantity());
                int transactionRowsUpdated = transactionFacade.updateTransactionStatus(transactionEntity.getTransactionId(), CommonConstants.TransactionStatus.SETTLED.toString());

                if (balanceRowsUpdated == 1 && transactionRowsUpdated == 1) {
                    status = CommonConstants.TransactionStatus.SETTLED.toString();
                }


            } else if (Objects.equals(transactionType, CommonConstants.TransactionType.SELL.toString()) && data.getValue() == assetPriceRecord.getBid()) {

                int balanceRowsUpdated  =  transactionFacade.updateUserBalance(userId, totalAmount);
                int assetRowsUpdated  =  transactionFacade.updateUserAsset(userId, data.getAssetCode(), -data.getQuantity());
                int transactionRowsUpdated =  transactionFacade.updateTransactionStatus(transactionEntity.getTransactionId(), CommonConstants.TransactionStatus.SETTLED.toString());

                if (balanceRowsUpdated == 1 && transactionRowsUpdated == 1) {
                    status = CommonConstants.TransactionStatus.SETTLED.toString();
                }
            } else {
                Map<String, MessageAttributeValue> messageAttribute = MessageMapper.serializeAssetTransactionMessage(uuid, userId, transactionType, data);
                sqsService.sendMessageToQueue(messageAttribute);
            }
            transactionData = DTOMapper.serializeAssetTransactionDTO(transactionType, userId, data, totalAmount, uuid, currentTimestampInSeconds, status);
            return transactionData;
        } catch (Exception e) {
            LOGGER.error("Error processing user request.", e);
            throw new DatabaseException("Error processing user request.", e);
        }
    }

    public TransactionHistoryDTO getUserTransactionInfoPaginated(String fromTs, String toTs, String pageToken, String reversePageToken, String userId, int page, int pageSize) {
        try {
            Timestamp pageTokenTimestamp = null;
            Timestamp reversePageTokenTimestamp = null;

            if (!pageToken.isEmpty()) {
                String pageTime = new String(Base64.getDecoder().decode(pageToken), StandardCharsets.UTF_8);
                pageTokenTimestamp = Timestamp.valueOf(Utils.convertToTimestamp(pageTime));
            }

            if (!reversePageToken.isEmpty()) {
                String reversePageTime = new String(Base64.getDecoder().decode(reversePageToken), StandardCharsets.UTF_8);
                reversePageTokenTimestamp = Timestamp.valueOf(Utils.convertToTimestamp(reversePageTime));
            }
            TransactionHistoryEntity transactionHistoryEntity = transactionFacade.getTransactionsByUserIdPaginated(fromTs, toTs, pageTokenTimestamp, reversePageTokenTimestamp, userId, page, pageSize);

            com.google.protobuf.Timestamp forwardTimestamp = transactionHistoryEntity.getTransactionHistory(0).getTimestamp();
            com.google.protobuf.Timestamp backwardTimestamp = transactionHistoryEntity.getTransactionHistory(transactionHistoryEntity.getTransactionHistoryCount() - 1).getTimestamp();
            String generatePageToken = Utils.generatePageToken(forwardTimestamp);
            String generateReversePageToken = Utils.generatePageToken(backwardTimestamp);

            return DTOMapper.serializeTransactionHistoryEntityToDTO(transactionHistoryEntity, generatePageToken, generateReversePageToken);
        } catch (Exception e) {
            LOGGER.error("Error getting user transaction information.", e);
            throw new CustomException(ExceptionStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String handleSQSTransaction(Message message, Map<String, AssetPriceRecord> assetPriceMap) {

        Map<String, MessageAttributeValue> messageAttributes = message.messageAttributes();
        Map<MessageSystemAttributeName, String> systemAttributes = message.attributes();
        int receiveCount = Integer.parseInt(systemAttributes.get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT));

        String assetCode = messageAttributes.get(DbConstants.ASSET_CODE).stringValue();
        String transactionType = messageAttributes.get(DbConstants.TRANSACTION_TYPE).stringValue();
        String transactionId = messageAttributes.get(DbConstants.TRANSACTION_ID).stringValue();
        double value = Double.parseDouble(messageAttributes.get(DbConstants.VALUE).stringValue());
        int quantity = Integer.parseInt(messageAttributes.get(DbConstants.QUANTITY).stringValue());
        String userId = messageAttributes.get(DbConstants.USER_ID).stringValue();
        double totalAmount = quantity * value;
        if(quantity <= 0 || value < 0) {
            throw new BadRequestException(ExceptionStatusCode.INVALID_VALUES);
        }
        AssetPriceRecord assetPriceRecord = assetPriceMap.get(assetCode);
        if (assetPriceRecord == null) {
            assetPriceRecord = providerPriceService.getRealTimeAssetPrice(assetCode);
            assetPriceMap.put(assetCode, assetPriceRecord);
        }

        String status = CommonConstants.TransactionStatus.PENDING.toString();

        if (Objects.equals(transactionType, CommonConstants.TransactionType.BUY.toString()) && assetPriceRecord.getAsk() == value) {
            status =  handleCompletedBuyTransaction(message, transactionId, userId, totalAmount, quantity, assetCode);
        } else if (Objects.equals(transactionType, CommonConstants.TransactionType.SELL.toString()) && assetPriceRecord.getBid() == value) {
            status =  handleCompletedSellTransaction(message, transactionId, userId, totalAmount, quantity, assetCode);
        } else if (receiveCount >= ConfigConstants.SQS_MAX_COUNT) {
            status = handleFailedTransaction(message, transactionId);
        }
        return  status;
    }

    private String handleCompletedBuyTransaction(Message message, String transactionId, String userId, double totalAmount, int quantity, String assetCode) {

        int balanceRowsUpdated = transactionFacade.updateUserBalance(userId, -totalAmount);
        int assetRowsUpdated = transactionFacade.updateUserAsset(userId, assetCode, quantity);
        int transactionRowsUpdated = transactionFacade.updateTransactionStatus(transactionId, CommonConstants.TransactionStatus.SETTLED.toString());

        if (balanceRowsUpdated == 1 && assetRowsUpdated == 1 && transactionRowsUpdated == 1) {
            String receiptHandle = message.receiptHandle();
            sqsService.deleteMessageFromQueue(receiptHandle);
            LOGGER.info("Buy transaction completed for user: {}, transactionId: {}", userId, transactionId);
            return  CommonConstants.TransactionStatus.SETTLED.toString();
        } else {
            LOGGER.error("Failed to update records for buy transaction. User: {}, transactionId: {}", userId, transactionId);
            throw new TransactionException(ExceptionStatusCode.BUY_TRANSACTION_FAILED);
        }
    }

    private String handleCompletedSellTransaction(Message message, String transactionId, String userId, double totalAmount, int quantity, String assetCode) {
        int balanceRowsUpdated = transactionFacade.updateUserBalance(userId, totalAmount);
        int assetRowsUpdated = transactionFacade.updateUserAsset(userId, assetCode, -quantity);
        int transactionRowsUpdated = transactionFacade.updateTransactionStatus(transactionId, CommonConstants.TransactionStatus.SETTLED.toString());

        if (balanceRowsUpdated == 1 && assetRowsUpdated == 1 && transactionRowsUpdated == 1) {
            String receiptHandle = message.receiptHandle();
            sqsService.deleteMessageFromQueue(receiptHandle);
            LOGGER.info("Sell transaction completed for user: {}, transactionId: {}", userId, transactionId);
            return CommonConstants.TransactionStatus.SETTLED.toString();
        } else {
            LOGGER.error("Failed to update records for sell transaction. User: {}, transactionId: {}", userId, transactionId);
            throw new TransactionException(ExceptionStatusCode.SELL_TRANSACTION_FAILED);
        }
    }

    private String handleFailedTransaction(Message message, String transactionId) {
        int transactionRowsUpdated = transactionFacade.updateTransactionStatus(transactionId, CommonConstants.TransactionStatus.FAILED.toString());
        if (transactionRowsUpdated == 1) {
            String receiptHandle = message.receiptHandle();
            sqsService.deleteMessageFromQueue(receiptHandle);
            LOGGER.warn("Transaction failed for transactionId: {}", transactionId);
            return CommonConstants.TransactionStatus.FAILED.toString();
        } else {
            LOGGER.error("Failed to update transaction status for transactionId: {}", transactionId);
            throw new DatabaseException("Unable to update transaction status");
        }
    }
}
