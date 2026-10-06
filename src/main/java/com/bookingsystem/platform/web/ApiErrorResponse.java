package com.bookingsystem.platform.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Public error response returned by every HTTP failure. */
public record ApiErrorResponse(ErrorDetail error) {

    /** Stable error details safe to expose to API clients. */
    public record ErrorDetail(String code, String message, @JsonProperty("request_id") String requestId) {
    }

    public static ApiErrorResponse of(final String code, final String message, final String requestId) {
        return new ApiErrorResponse(new ErrorDetail(code, message, requestId));
    }
}
