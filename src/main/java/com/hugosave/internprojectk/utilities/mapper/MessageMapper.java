package com.hugosave.internprojectk.utilities.mapper;

import com.hugosave.intern.project.proto.AssetTransactionDTO;
import com.hugosave.intern.project.proto.UserAssetDTO;
import com.hugosave.intern.project.proto.UserTransactionRequestDTO;
import com.hugosave.internprojectk.constants.DbConstants;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.Map;

public class MessageMapper {
    public static Map<String, MessageAttributeValue> serializeAssetTransactionMessage(String transactionId, String userId, String transactionType, UserTransactionRequestDTO data) {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put(DbConstants.USER_ID, MessageAttributeValue.builder().dataType("String").stringValue(userId).build());
        messageAttributes.put(DbConstants.TRANSACTION_ID, MessageAttributeValue.builder().dataType("String").stringValue(transactionId).build());
        messageAttributes.put(DbConstants.TRANSACTION_TYPE, MessageAttributeValue.builder().dataType("String").stringValue(transactionType).build());
        messageAttributes.put(DbConstants.ASSET_CODE, MessageAttributeValue.builder().dataType("String").stringValue(data.getAssetCode()).build());
        messageAttributes.put(DbConstants.QUANTITY, MessageAttributeValue.builder().dataType("Number").stringValue(String.valueOf(data.getQuantity())).build());
        messageAttributes.put(DbConstants.VALUE, MessageAttributeValue.builder().dataType("Number").stringValue(String.valueOf(data.getValue())).build());
        messageAttributes.put(DbConstants.CURRENCY, MessageAttributeValue.builder().dataType("String").stringValue(data.getCurrency()).build());
        messageAttributes.put(DbConstants.REF_ID, MessageAttributeValue.builder().dataType("String").stringValue(data.getRefId()).build());

        return messageAttributes;
    }
}
