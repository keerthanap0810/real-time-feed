package com.hugosave.internprojectk.provider.goldbroker.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.constants.DbConstants;
import com.hugosave.internprojectk.constants.ResourceConstants;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class GoldBrokerPriceProtoMapper {
    public static AssetPriceRecord deserializeGoldBrokerResponse(ResponseEntity<String> responseEntity, String asset_code) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());

        return AssetPriceRecord.newBuilder()
            .setAssetCode(asset_code)
            .setValue(rootNode.path(ResourceConstants.GOLD_BROKER_VALUE).asDouble())
            .setAsk(rootNode.path(ResourceConstants.GOLD_BROKER_ASK).asDouble())
            .setBid(rootNode.path(ResourceConstants.GOLD_BROKER_BID).asDouble())
            .setWeight(ConfigConstants.WEIGHT)
            .setCurrency(ConfigConstants.CURRENCY)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(parseDateToTimestamp(rootNode.path(ResourceConstants.GOLD_BROKER_DATE).asText()))
                .build())
            .build();
    }
    private static long parseDateToTimestamp(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, formatter);
        return offsetDateTime.toEpochSecond();
    }
}
