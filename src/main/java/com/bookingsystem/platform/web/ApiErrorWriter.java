package com.bookingsystem.platform.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Serializes the standard public error envelope. */
@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorWriter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final int status,
            final String code,
            final String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(code, message, RequestIdFilter.currentRequestId(request)));
    }
}
