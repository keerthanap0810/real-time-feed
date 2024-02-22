package com.hugosave.internprojectk.controller;


import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.CommonConstants;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.service.TransactionService;
import com.hugosave.internprojectk.utilities.utils.Utils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    public final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/buy")
    public AssetTransactionDTO buyAsset(@RequestBody UserTransactionRequestDTO data,
                                        @RequestHeader(name = ConfigConstants.HEADER_AUTHORIZATION) String authorizationHeader) {
        String userId = Utils.getUserId(authorizationHeader);
        return transactionService.processUserRequest(CommonConstants.TransactionType.BUY.toString(), userId, data);

    }

    @PostMapping("/sell")
    public AssetTransactionDTO sellAsset(@RequestBody UserTransactionRequestDTO data,
                                         @RequestHeader(name = ConfigConstants.HEADER_AUTHORIZATION) String authorizationHeader) {
        String userId = Utils.getUserId(authorizationHeader);
        return transactionService.processUserRequest(CommonConstants.TransactionType.SELL.toString(), userId, data);

    }

    @GetMapping("")
    public TransactionHistoryDTO getUserTransactionInfoPaginated(
        @RequestParam(name = ConfigConstants.PARAM_PAGE_TOKEN, defaultValue = "") String pageToken,
        @RequestParam(name = ConfigConstants.PARAM_REVERSE_PAGE_TOKEN, defaultValue = "") String reversePageToken,
        @RequestParam(name = ConfigConstants.PARAM_FROM_TS, defaultValue = "" ) String fromTs,
        @RequestParam(name = ConfigConstants.PARAM_TO_TS, defaultValue = "") String toTs,
        @RequestParam(name = ConfigConstants.PARAM_PAGE_SIZE, defaultValue = "10") int pageSize,
        @RequestParam(name = ConfigConstants.PARAM_PAGE_NO, defaultValue = "1") int pageNo,
        @RequestHeader(name = ConfigConstants.HEADER_AUTHORIZATION) String authorizationHeader
    ) {
        String userId = Utils.getUserId(authorizationHeader);
        return transactionService.getUserTransactionInfoPaginated(fromTs, toTs, pageToken, reversePageToken, userId, pageNo, pageSize);
    }
}
