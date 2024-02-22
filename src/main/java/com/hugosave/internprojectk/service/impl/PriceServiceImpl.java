package com.hugosave.internprojectk.service.impl;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.facade.AssetPriceFacade;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.provider.ProviderPriceService;
import com.hugosave.internprojectk.service.PriceService;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.Utils;
import com.hugosave.internprojectk.utilities.utils.exception.APIResponseException;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;

@Service
public class PriceServiceImpl implements PriceService {
    private static final Logger logger = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final ProviderPriceService providerPriceService;
    private final AssetPriceFacade assetPriceFacade;

    public PriceServiceImpl(ProviderPriceService providerPriceService, AssetPriceFacade assetPriceFacade) {
        this.providerPriceService = providerPriceService;
        this.assetPriceFacade = assetPriceFacade;
    }

    public RealTimePriceDTO getRealTimePrice(String asset_code) {
        try {
            AssetPriceRecord assetPrice = providerPriceService.getRealTimeAssetPrice(asset_code);
            return DTOMapper.deserializeAssetPriceRecord(assetPrice);
        } catch (Exception e) {
            logger.error("Error getting real-time price.", e);
            throw new APIResponseException(ExceptionStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public RealTimePriceList getHistoricalAssetPrice(String fromTs, String toTs, String pageToken, String reversePageToken, int pageSize, int pageNo) {
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

            AssetPriceEntityList assetInfoPaginated = assetPriceFacade.getAssetInfoPaginated(fromTs, toTs, pageTokenTimestamp, reversePageTokenTimestamp, pageSize, pageNo);

            com.google.protobuf.Timestamp forwardTimestamp = assetInfoPaginated.getAssetPrices(0).getTimestamp();
            com.google.protobuf.Timestamp backwardTimestamp = assetInfoPaginated.getAssetPrices(assetInfoPaginated.getAssetPricesCount() - 1).getTimestamp();
            String generatePageToken = Utils.generatePageToken(forwardTimestamp);
            String generateReversePageToken = Utils.generatePageToken(backwardTimestamp);

            return DTOMapper.deserializeAssetPriceEntityList(assetInfoPaginated, generatePageToken, generateReversePageToken);
        } catch (Exception e) {
            logger.error("Error getting historical asset price.", e);
            throw new DatabaseException("Error getting historical asset price.", e);
        }

    }
}
