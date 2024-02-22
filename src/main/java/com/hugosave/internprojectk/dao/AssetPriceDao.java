package com.hugosave.internprojectk.dao;

import com.hugosave.intern.project.proto.AssetPriceEntity;
import com.hugosave.intern.project.proto.AssetPriceEntityList;
import com.hugosave.intern.project.proto.RealTimePriceList;

import java.sql.Timestamp;
import java.util.List;

public interface AssetPriceDao {
    void addAssetEntries(List<AssetPriceEntity> assetEntries);

    AssetPriceEntityList getAssetInfoPaginated(String fromTs, String toTs, Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp, int pageSize, int pageNo);
}
