package com.hugosave.internprojectk.provider.goldbroker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.internprojectk.constants.ApplicationProperties;
import com.hugosave.internprojectk.provider.ProviderPriceService;
import com.hugosave.internprojectk.provider.goldbroker.mapper.GoldBrokerPriceProtoMapper;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GoldBrokerPriceService implements ProviderPriceService {
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;

    public GoldBrokerPriceService(RestTemplate restTemplate, ApplicationProperties applicationProperties){
        this.restTemplate = restTemplate;
        this.applicationProperties=applicationProperties;
    }

    public AssetPriceRecord getRealTimeAssetPrice(String asset_code) {
        try {
            String apiUrl = String.format(applicationProperties.getProviderApiUrl(), asset_code);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
            return GoldBrokerPriceProtoMapper.deserializeGoldBrokerResponse(responseEntity, asset_code);
        } catch (Exception e) {
            throw new CustomException(ExceptionStatusCode.GOLDBROKER_SERVER_ERROR);
        }
    }
}
