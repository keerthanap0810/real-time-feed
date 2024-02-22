package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class APIResponseException extends RuntimeException {
    private final RequestResponse requestResponse;

    public APIResponseException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
        this.requestResponse = requestResponse;
    }

    public RequestResponse getRequestResponse() {
        return requestResponse;
    }
}
