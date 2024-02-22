package com.hugosave.internprojectk.facade;

import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceEntityList;
import com.hugosave.intern.project.proto.AssetPriceRecord;
import com.hugosave.intern.project.proto.RealTimePriceList;
import com.hugosave.internprojectk.dao.AssetPriceDao;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class AssetPriceFacade {
    private final AssetPriceDao assetPriceDao;

    public AssetPriceFacade(AssetPriceDao assetPriceDao) {
        this.assetPriceDao = assetPriceDao;
    }

    public void addAssetEntries(List<AssetPriceEntity> assetEntries) {
        assetPriceDao.addAssetEntries(assetEntries);
    }

    public AssetPriceEntityList getAssetInfoPaginated(String fromTs, String toTs, Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp, int pageSize, int pageNo) {
        return assetPriceDao.getAssetInfoPaginated(fromTs, toTs, pageTokenTimestamp, reversePageTokenTimestamp, pageSize, pageNo);
    }

}
