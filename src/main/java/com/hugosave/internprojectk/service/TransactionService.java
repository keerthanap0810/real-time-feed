package com.hugosave.internprojectk.service;

import com.hugosave.intern.project.proto.*;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Map;

public interface TransactionService {
    AssetTransactionDTO processUserRequest(String transactionType, String userId, UserTransactionRequestDTO data);

    TransactionHistoryDTO getUserTransactionInfoPaginated(String fromTs, String toTs, String pageToken, String reversePageToken, String userId, int page, int pageSize);

    String handleSQSTransaction(Message message, Map<String, AssetPriceRecord> assetPriceMap);
}
