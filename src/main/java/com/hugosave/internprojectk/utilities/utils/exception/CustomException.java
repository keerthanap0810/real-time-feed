package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class CustomException extends RuntimeException {

    public CustomException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public CustomException(Exception e) {
        super(e);
    }

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Exception e) {
        super(message, e);
    }
}
