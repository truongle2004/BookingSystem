package com.bookingsystem.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("API exception handler")
class ApiExceptionHandlerTest {

    @Test
    @DisplayName("maps validation failures to the standard bad-request response")
    void invalidRequestShouldReturnStandardBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestIdFilter.ATTRIBUTE_NAME, "request_123");
        ResponseEntity<ApiErrorResponse> response = new ApiExceptionHandler().invalidRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(
                ApiErrorResponse.of("INVALID_REQUEST", "The request is invalid.", "request_123"));
    }

    @Test
    @DisplayName("maps unexpected failures without exposing their details")
    void internalErrorShouldReturnStandardInternalError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestIdFilter.ATTRIBUTE_NAME, "request_456");

        ResponseEntity<ApiErrorResponse> response = new ApiExceptionHandler()
                .internalError(request, new IllegalStateException("sensitive detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(
                ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred.", "request_456"));
    }
}
