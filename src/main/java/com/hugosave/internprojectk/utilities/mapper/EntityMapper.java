package com.hugosave.internprojectk.utilities.mapper;

import com.google.protobuf.Timestamp;
import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.DbConstants;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class EntityMapper {
    public static AssetPriceEntity deserializeAssetPriceDTO(RealTimePriceDTO realTimePriceDTO){
        return AssetPriceEntity.newBuilder()
            .setAssetCode(realTimePriceDTO.getAssetCode())
            .setValue(realTimePriceDTO.getValue())
            .setAsk(realTimePriceDTO.getAsk())
            .setBid(realTimePriceDTO.getBid())
            .setWeight(realTimePriceDTO.getWeight())
            .setCurrency(realTimePriceDTO.getCurrency())
            .setTimestamp(realTimePriceDTO.getTimestamp())
            .build();
    }

    public static TransactionEntity serializeTransactionEntity(String transactionType, String userId, UserTransactionRequestDTO data, double totalAmount, String uuid, long currentTimestampInSeconds, String status) {
        return TransactionEntity.newBuilder()
            .setStatus(status)
            .setTotalAmount(totalAmount)
            .setUserId(userId)
            .setRefId(data.getRefId())
            .setTransactionType(transactionType)
            .setTransactionId(uuid)
            .setAssetCode(data.getAssetCode())
            .setCurrency(data.getCurrency())
            .setValue(data.getValue())
            .setQuantity(data.getQuantity())
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(currentTimestampInSeconds)
                .build())
            .build();
    }

    public static TransactionEntity deserializeSQSMessage(Message message) {
        Map<String, MessageAttributeValue> messageAttributes = message.messageAttributes();

        double value = Double.parseDouble(messageAttributes.get(DbConstants.VALUE).stringValue());
        int quantity = Integer.parseInt(messageAttributes.get(DbConstants.QUANTITY).stringValue());

        return TransactionEntity.newBuilder()
            .setStatus(CommonConstants.TransactionStatus.PENDING.toString())
            .setTotalAmount(quantity * value)
            .setUserId(messageAttributes.get(DbConstants.USER_ID).stringValue())
            .setRefId(messageAttributes.get(DbConstants.REF_ID).stringValue())
            .setTransactionType(messageAttributes.get(DbConstants.TRANSACTION_TYPE).stringValue())
            .setTransactionId(messageAttributes.get(DbConstants.TRANSACTION_ID).stringValue())
            .setAssetCode(messageAttributes.get(DbConstants.ASSET_CODE).stringValue())
            .setCurrency(messageAttributes.get(DbConstants.CURRENCY).stringValue())
            .setValue(value)
            .setQuantity(quantity)
            .setTimestamp((Timestamp) null)
            .build();
    }

    public static AssetTransactionEntity.Builder createAssetTransactionEntityBuilder(ResultSet resultSet) throws SQLException {
        return AssetTransactionEntity.newBuilder()
            .setTransactionId(resultSet.getString(DbConstants.TRANSACTION_ID))
            .setTransactionType(resultSet.getString(DbConstants.TRANSACTION_TYPE))
            .setStatus(resultSet.getString(DbConstants.STATUS))
            .setAssetCode(resultSet.getString(DbConstants.ASSET_CODE))
            .setTotalAmount(resultSet.getDouble(DbConstants.TOTAL_AMOUNT))
            .setValue(resultSet.getDouble(DbConstants.VALUE))
            .setQuantity(resultSet.getInt(DbConstants.QUANTITY))
            .setCurrency(resultSet.getString(DbConstants.CURRENCY))
            .setRefId(resultSet.getString(DbConstants.REF_ID))
            .setTimestamp(getTimestampFromResultSet(resultSet.getTimestamp(DbConstants.CREATE_TIMESTAMP)));

    }

    public static AssetPriceEntity.Builder createRealTimePriceEntityBuilder(ResultSet resultSet) throws SQLException {
        return AssetPriceEntity.newBuilder()
            .setAssetCode(resultSet.getString(DbConstants.ASSET_CODE))
            .setValue(resultSet.getDouble(DbConstants.VALUE))
            .setAsk(resultSet.getDouble(DbConstants.ASK))
            .setBid(resultSet.getDouble(DbConstants.BID))
            .setWeight(resultSet.getString(DbConstants.WEIGHT))
            .setCurrency(resultSet.getString(DbConstants.CURRENCY))
            .setTimestamp(getTimestampFromResultSet(resultSet.getTimestamp(DbConstants.CREATE_TIMESTAMP)));
    }


    private static com.google.protobuf.Timestamp getTimestampFromResultSet(java.sql.Timestamp timestamp) throws SQLException {
        return com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(timestamp.getTime() / 1000)
            .setNanos((timestamp.getNanos() % 1000000) * 1000)
            .build();
    }
}
