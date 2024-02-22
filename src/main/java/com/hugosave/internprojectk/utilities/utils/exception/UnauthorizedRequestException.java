package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class UnauthorizedRequestException extends RuntimeException {

    public UnauthorizedRequestException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public UnauthorizedRequestException(String message) {
        super(message);
    }

    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedRequestException(Throwable cause) {
        super(cause);
    }
}
