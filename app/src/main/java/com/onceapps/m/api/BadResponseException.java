package com.onceapps.m.api;

import android.support.annotation.NonNull;

import com.onceapps.core.util.OfflineException;

public class BadResponseException extends OfflineException {

    private ErrorResponse mErrorResponse;

    public BadResponseException() {
    }

    public BadResponseException(String detailMessage, int responseCode) {
        super(detailMessage);
        mErrorResponse = new ErrorResponse();
        mErrorResponse.setMessage(detailMessage);
        mErrorResponse.setCode(responseCode);
    }

    public BadResponseException(@NonNull ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        mErrorResponse = errorResponse;
    }

    public BadResponseException(String detailMessage, int responseCode, Throwable throwable) {
        super(detailMessage, throwable);
        mErrorResponse = new ErrorResponse();
        mErrorResponse.setMessage(detailMessage);
        mErrorResponse.setCode(responseCode);
    }

    public BadResponseException(@NonNull ErrorResponse errorResponse, Throwable throwable) {
        super(errorResponse.getMessage(), throwable);
        mErrorResponse = errorResponse;
    }

    public BadResponseException(Throwable throwable) {
        super(throwable);
    }

    public ErrorResponse getErrorResponse() {
        return mErrorResponse;
    }
}
