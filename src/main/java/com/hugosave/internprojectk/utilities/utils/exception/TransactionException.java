package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class TransactionException extends RuntimeException {

    public TransactionException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public TransactionException(Exception e) {
        super(e);
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Exception e) {
        super(message, e);
    }
}
