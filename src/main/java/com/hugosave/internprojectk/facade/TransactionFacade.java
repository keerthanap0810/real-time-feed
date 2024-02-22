package com.hugosave.internprojectk.facade;

import com.hugosave.intern.project.proto.TransactionHistoryEntity;
import com.hugosave.internprojectk.dao.TransactionDao;
import com.hugosave.intern.project.proto.TransactionEntity;
import com.hugosave.intern.project.proto.TransactionHistoryDTO;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class TransactionFacade {
    private final TransactionDao transactionDao;

    public TransactionFacade(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public TransactionEntity addTransactionEntry(TransactionEntity transaction) {
       return transactionDao.addTransactionEntry(transaction);
    }

    public TransactionHistoryEntity getTransactionsByUserIdPaginated(String fromTs, String toTs, Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp, String userId, int page, int pageSize) {
        return transactionDao.getTransactionsByUserIdPaginated(fromTs, toTs, pageTokenTimestamp, reversePageTokenTimestamp, userId, page, pageSize);
    }

    public int updateTransactionStatus(String transactionId, String status) {
        return transactionDao.updateTransactionStatus( transactionId, status);
    }

    public int updateUserBalance(String userId, double totalAmount){
        return transactionDao.updateUserBalance(userId, totalAmount);
    }

    public int  updateUserAsset(String userId, String assetCode, int quantity){
        return transactionDao.updateUserAsset(userId, assetCode, quantity);
    }


}
