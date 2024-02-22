package com.hugosave.internprojectk.utilities.utils.exception;

import com.hugosave.intern.project.proto.RequestResponse;

public class DatabaseException extends RuntimeException {

    public DatabaseException(RequestResponse requestResponse) {
        super(requestResponse.getMessage());
    }

    public DatabaseException(Exception e) {
        super(e);
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Exception e) {
        super(message, e);
    }
}
