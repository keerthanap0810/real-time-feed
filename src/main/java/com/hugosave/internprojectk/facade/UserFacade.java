package com.hugosave.internprojectk.facade;

import com.hugosave.intern.project.proto.UserAssetAndBalanceEntity;
import com.hugosave.internprojectk.dao.UserDao;
import com.hugosave.intern.project.proto.RealTimePriceDTO;
import com.hugosave.intern.project.proto.UserEntity;
import com.hugosave.intern.project.proto.UserLedgerEntity;
import org.springframework.stereotype.Component;

@Component
public class UserFacade {
    private final UserDao userDao;

    public UserFacade(UserDao userDao) {
        this.userDao = userDao;
    }

    public int insertUserBalance(String userId) {
        return userDao.insertUserBalance(userId);
    }

    public int  insertUser(UserEntity userEntity) {
        return userDao.insertUser(userEntity);
    }

    public UserAssetAndBalanceEntity getUserAssetsAndBalanceById(String userId){
        return  userDao.getUserAssetsAndBalanceById(userId);
    }
}
