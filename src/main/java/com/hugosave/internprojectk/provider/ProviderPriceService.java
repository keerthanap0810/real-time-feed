package com.hugosave.internprojectk.provider;

import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceRecord;

public interface ProviderPriceService {
    AssetPriceRecord getRealTimeAssetPrice(String asset_code);
}
