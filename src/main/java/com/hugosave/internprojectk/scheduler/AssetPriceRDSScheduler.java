package com.hugosave.internprojectk.scheduler;

import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.facade.AssetPriceFacade;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.utilities.mapper.EntityMapper;
import com.hugosave.internprojectk.provider.ProviderPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AssetPriceRDSScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPriceRDSScheduler.class);

    private final ProviderPriceService providerPriceService;
    private final AssetPriceFacade assetPriceFacade;

    public AssetPriceRDSScheduler(ProviderPriceService providerPriceService, AssetPriceFacade assetPriceFacade) {
        this.providerPriceService = providerPriceService;
        this.assetPriceFacade = assetPriceFacade;
    }

    @Scheduled(fixedRateString = ConfigConstants.SCHEDULER_PRICE)
    public void addAssetPrices() {
        List<String> asset_codes = Arrays.asList("XAU", "XAG", "XPD");
        try {
            LOGGER.info("Scheduled task started: Fetching real-time asset prices.");

            List<AssetPriceEntity> assetEntries = new ArrayList<>();

            for(String asset_code : asset_codes){
                AssetPriceRecord realTimeAssetPrice = providerPriceService.getRealTimeAssetPrice(asset_code);
                RealTimePriceDTO data = DTOMapper.deserializeAssetPriceRecord(realTimeAssetPrice);
                AssetPriceEntity assetPriceEntity = EntityMapper.deserializeAssetPriceDTO(data);
                assetEntries.add(assetPriceEntity);
            }

            assetPriceFacade.addAssetEntries(assetEntries);

            LOGGER.info("Scheduled task completed: Real-time asset prices added successfully.");
        } catch (Exception e) {
            LOGGER.error("Error in scheduled task add asset entry: {}", e.getMessage(), e);
        }
    }
}
