package com.hugosave.internprojectk.dao;

import com.hugosave.intern.project.proto.TransactionEntity;
import com.hugosave.intern.project.proto.TransactionHistoryDTO;
import com.hugosave.intern.project.proto.TransactionHistoryEntity;

import java.sql.Timestamp;

public interface TransactionDao {
    TransactionEntity addTransactionEntry(TransactionEntity transaction);

    TransactionHistoryEntity getTransactionsByUserIdPaginated(String fromTs, String toTs, Timestamp pageTokenTimestamp, Timestamp reversePageTokenTimestamp, String userId, int page, int pageSize);

    int updateTransactionStatus(String transactionId, String status);

    int updateUserBalance(String userId, double totalAmount);

    int updateUserAsset(String userId, String assetCode, int quantity);
}
