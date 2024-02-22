package com.hugosave.internprojectk.service.impl;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.facade.UserFacade;
import com.hugosave.internprojectk.infrastructure.AwsCognito;
import com.hugosave.internprojectk.utilities.mapper.DTOMapper;
import com.hugosave.internprojectk.service.UserService;
import com.hugosave.internprojectk.utilities.utils.ExceptionStatusCode;
import com.hugosave.internprojectk.utilities.utils.exception.APIResponseException;
import com.hugosave.internprojectk.utilities.utils.exception.BadRequestException;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import com.hugosave.internprojectk.utilities.utils.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserFacade userFacade;
    private final AwsCognito awsCognito;

    public UserServiceImpl(UserFacade userFacade, AwsCognito awsCognito) {
        this.userFacade = userFacade;
        this.awsCognito = awsCognito;
    }

    public UserAssetAndBalanceDTO getUserBalance(String userId) {
        try {
            UserAssetAndBalanceEntity userAssetAndBalanceEntity = userFacade.getUserAssetsAndBalanceById(userId);
            return DTOMapper.deserializeUserAssetAndBalance(userAssetAndBalanceEntity);
        } catch (Exception e) {
            logger.error("Error getting user balance.", e);
            throw new DatabaseException("Error getting user balance.", e);
        }
    }

    public UserAuthResponseDTO userLogin(UserOnboardDTO userOnboard) {
        try {
            return awsCognito.initiateAuth(userOnboard);
        } catch (Exception e) {
            logger.error("Error during user login.", e);
            throw new BadRequestException(e);
        }
    }

    @Async
    public CompletableFuture<Integer> createUserEntry(UserOnboardDTO userOnboard, UserAuthResponseDTO userAuthResponse) {
        try {
            UserEntity entity = DTOMapper.deserializeUserOnBoard(userOnboard, userAuthResponse.getUserId());
            int rowsCreated = userFacade.insertUser(entity);
            return CompletableFuture.completedFuture(rowsCreated);
        } catch (Exception e) {
            logger.error("Error asynchronously inserting user.", e);
            throw new RuntimeException("Error asynchronously inserting user.", e);
        }
    }

    @Async
    public CompletableFuture<Integer> createUserLedger(String userId) {
        try {
            int rowsCreated = userFacade.insertUserBalance(userId);
            return CompletableFuture.completedFuture(rowsCreated);
        } catch (Exception e) {
            logger.error("Error asynchronously inserting user ledger.", e);
            throw new RuntimeException("Error asynchronously inserting user ledger.", e);
        }
    }

    public UserAuthResponseDTO userSignup(UserOnboardDTO userOnboard) {
        try {
            try{
                UserOnboardDTO userName = awsCognito.signUp(userOnboard);
            } catch (Exception e){
                throw new CustomException(ExceptionStatusCode.USER_ALREADY_EXISTS);
            }

            UserAuthResponseDTO userAuthResponse = awsCognito.initiateAuth(userOnboard);
            CompletableFuture<Integer> insertUserFuture = createUserEntry(userOnboard, userAuthResponse);
            CompletableFuture<Integer> insertUserLedgerFuture = createUserLedger(userAuthResponse.getUserId());

            CompletableFuture.allOf(insertUserFuture, insertUserLedgerFuture).join();

            if (insertUserFuture.get() == 1 && insertUserLedgerFuture.get() == 1) {
                return userAuthResponse;
            } else {
                throw new DatabaseException("Error during user signup: Insertion failed.");
            }
        } catch (Exception e) {
            logger.error("Error during user signup.", e);
            throw new BadRequestException("Error during user signup.", e);
        }
    }

}
