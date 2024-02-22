package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class BadRequestException extends RuntimeException {

    public BadRequestException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public BadRequestException(Exception e) {
        super(e);
    }

    public BadRequestException(String message, Exception e) {
        super(message, e);
    }
}
