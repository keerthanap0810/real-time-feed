package com.hugosave.internprojectk.controller;

import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.RealTimePriceList;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.service.PriceService;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset/price")
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/real-time/{asset}")
    public RealTimePriceDTO getRealTimeAssetPrice(@PathVariable(name = "asset") String asset) {
        try {
            return priceService.getRealTimePrice(asset);
        } catch (Exception e) {
            throw new CustomException(ExceptionStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/historical")
    public RealTimePriceList getHistoricalAssetPrice(
         @RequestParam(name = ConfigConstants.PARAM_PAGE_TOKEN, defaultValue = "") String pageToken,
         @RequestParam(name = ConfigConstants.PARAM_REVERSE_PAGE_TOKEN, defaultValue = "") String reversePageToken,
         @RequestParam(name = ConfigConstants.PARAM_FROM_TS, defaultValue = "" ) String fromTs,
         @RequestParam(name = ConfigConstants.PARAM_TO_TS, defaultValue = "") String toTs,
         @RequestParam(name = ConfigConstants.PARAM_PAGE_SIZE, defaultValue = "10") int pageSize,
         @RequestParam(name = ConfigConstants.PARAM_PAGE_NO, defaultValue = "1") int pageNo) {
        return priceService.getHistoricalAssetPrice(fromTs, toTs, pageToken, reversePageToken, pageSize, pageNo);
    }
}
