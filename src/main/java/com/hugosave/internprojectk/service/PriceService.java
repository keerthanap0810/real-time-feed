package com.hugosave.internprojectk.service;

import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.RealTimePriceList;

public interface PriceService {
    RealTimePriceDTO getRealTimePrice(String asset);

    RealTimePriceList getHistoricalAssetPrice(String fromTs, String toTs, String pageToken, String reversePageToken, int pageSize, int pageNo);
}
