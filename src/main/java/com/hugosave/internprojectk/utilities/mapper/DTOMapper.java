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
import java.util.stream.Collectors;

public class DTOMapper {

    public static RealTimePriceDTO deserializeAssetPriceRecord(AssetPriceRecord realTimePriceDTO){
        return RealTimePriceDTO.newBuilder()
            .setAssetCode(realTimePriceDTO.getAssetCode())
            .setValue(realTimePriceDTO.getValue())
            .setAsk(realTimePriceDTO.getAsk())
            .setBid(realTimePriceDTO.getBid())
            .setWeight(realTimePriceDTO.getWeight())
            .setCurrency(realTimePriceDTO.getCurrency())
            .setTimestamp(realTimePriceDTO.getTimestamp())
            .build();
    }

    public TransactionMessageDTO convertToTransactionMessageDTO(Message message) {
        Map<String, MessageAttributeValue> messageAttributes = message.messageAttributes();

        String transactionId = messageAttributes.get(DbConstants.TRANSACTION_ID).stringValue();
        String transactionType = messageAttributes.get(DbConstants.TRANSACTION_TYPE).stringValue();
        int quantity = Integer.parseInt(messageAttributes.get(DbConstants.QUANTITY).stringValue());
        double value = Double.parseDouble(messageAttributes.get(DbConstants.VALUE).stringValue());
        double totalAmount = quantity * value;
        String assetName = messageAttributes.get(DbConstants.ASSET_CODE).stringValue();
        String currency = messageAttributes.get(DbConstants.CURRENCY).stringValue();
        String refId = messageAttributes.get(DbConstants.REF_ID).stringValue();

        AssetTransactionDTO assetTransactionDTO = AssetTransactionDTO.newBuilder()
            .setTransactionId(transactionId)
            .setTransactionType(transactionType)
            .setQuantity(quantity)
            .setValue(value)
            .setTotalAmount(totalAmount)
            .setAssetCode(assetName)
            .setCurrency(currency)
            .setRefId(refId)
            .build();

        return TransactionMessageDTO.newBuilder().addTransactionMessage(assetTransactionDTO).build();
    }

    public static UserAssetAndBalanceDTO deserializeUserAssetAndBalance(UserAssetAndBalanceEntity userAssetAndBalanceEntity) {
        UserAssetAndBalanceDTO.Builder dtoBuilder = UserAssetAndBalanceDTO.newBuilder()
            .setBalance(userAssetAndBalanceEntity.getUserBalance().getBalance());

        for (UserAssetEntity userAssetEntity : userAssetAndBalanceEntity.getUserAssetsList()) {
            dtoBuilder.addUserAssets(deserializeUserAssetEntity(userAssetEntity));
        }

        return dtoBuilder.build();
    }

    private static UserAssetDTO deserializeUserAssetEntity(UserAssetEntity userAssetEntity) {
        return UserAssetDTO.newBuilder()
            .setAssetCode(userAssetEntity.getAssetCode())
            .setQuantity(userAssetEntity.getQuantity())
            .build();
    }

    public static UserEntity deserializeUserOnBoard(UserOnboardDTO userOnboard, String userId){
        return UserEntity.newBuilder()
            .setUserId(userId)
            .setEmail(userOnboard.getEmail())
            .setUserName(userOnboard.getUserName())
            .build();
    }

    public static AssetTransactionDTO serializeAssetTransactionDTO(String transactionType, String userId, UserTransactionRequestDTO data, double totalAmount, String uuid, long currentTimestampInSeconds, String status) {
        return AssetTransactionDTO.newBuilder()
            .setAssetCode(data.getAssetCode())
            .setCurrency(data.getCurrency())
            .setQuantity(data.getQuantity())
            .setValue(data.getValue())
            .setRefId(data.getRefId())
            .setStatus(status)
            .setTotalAmount(totalAmount)
            .setTransactionType(transactionType)
            .setTransactionId(uuid)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(currentTimestampInSeconds)
                .build())
            .build();
    }

    public static RealTimePriceList deserializeAssetPriceEntityList(AssetPriceEntityList assetPriceEntityList, String pageToken, String reversePageToken) {
        RealTimePriceList.Builder realTimePriceListBuilder = RealTimePriceList.newBuilder()
            .setNextPageToken(pageToken)
            .setReversePageToken(reversePageToken);

        for (AssetPriceEntity assetPriceEntity : assetPriceEntityList.getAssetPricesList()) {
            RealTimePriceDTO realTimePriceDTO = RealTimePriceDTO.newBuilder()
                .setAssetCode(assetPriceEntity.getAssetCode())
                .setValue(assetPriceEntity.getValue())
                .setAsk(assetPriceEntity.getAsk())
                .setBid(assetPriceEntity.getBid())
                .setWeight(assetPriceEntity.getWeight())
                .setCurrency(assetPriceEntity.getCurrency())
                .setTimestamp(assetPriceEntity.getTimestamp())
                .build();

            realTimePriceListBuilder.addRealTimePrices(realTimePriceDTO);
        }

        return realTimePriceListBuilder.build();
    }




    public static AssetTransactionDTO.Builder createAssetTransactionDTOBuilder(ResultSet resultSet) throws SQLException {
        return AssetTransactionDTO.newBuilder()
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

    private static com.google.protobuf.Timestamp getTimestampFromResultSet(java.sql.Timestamp timestamp) throws SQLException {
        return com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(timestamp.getTime() / 1000)
            .setNanos((timestamp.getNanos() % 1000000) * 1000)
            .build();
    }

    public static TransactionHistoryDTO serializeTransactionHistoryEntityToDTO(TransactionHistoryEntity entity, String pageToken, String reversePageToken) {
        TransactionHistoryDTO.Builder dtoBuilder = TransactionHistoryDTO.newBuilder()
            .setPageToken(pageToken)
            .setReversePageToken(reversePageToken);

        for (AssetTransactionEntity assetTransactionEntity : entity.getTransactionHistoryList()) {
            AssetTransactionDTO assetTransactionDTO = AssetTransactionDTO.newBuilder()
                .setTransactionId(assetTransactionEntity.getTransactionId())
                .setTransactionType(assetTransactionEntity.getTransactionType())
                .setAssetCode(assetTransactionEntity.getAssetCode())
                .setQuantity(assetTransactionEntity.getQuantity())
                .setTotalAmount(assetTransactionEntity.getTotalAmount())
                .setStatus(assetTransactionEntity.getStatus())
                .setValue(assetTransactionEntity.getValue())
                .setCurrency(assetTransactionEntity.getCurrency())
                .setRefId(assetTransactionEntity.getRefId())
                .setTimestamp(assetTransactionEntity.getTimestamp())
                .build();

            dtoBuilder.addTransactionHistory(assetTransactionDTO);
        }

        return dtoBuilder.build();
    }


}
