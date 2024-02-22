package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class SQSException extends RuntimeException {

    public SQSException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public SQSException(Exception e) {
        super(e);
    }

    public SQSException(String message) {
        super(message);
    }

    public SQSException(String message, Exception e) {
        super(message, e);
    }
}
