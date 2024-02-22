package com.hugosave.internprojectk.dao;

import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.UserAssetAndBalanceEntity;
import com.hugosave.intern.project.proto.UserEntity;
import com.hugosave.intern.project.proto.UserLedgerEntity;

public interface UserDao {
    int insertUser(UserEntity userEntity);

    int insertUserBalance(String userId);

    UserAssetAndBalanceEntity getUserAssetsAndBalanceById(String userId);
}
