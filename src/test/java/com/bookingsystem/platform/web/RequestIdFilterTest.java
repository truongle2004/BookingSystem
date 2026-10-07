package com.bookingsystem.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("Request ID filter")
class RequestIdFilterTest {

    @Test
    @DisplayName("places a valid request ID in the response and MDC")
    void doFilterShouldPropagateValidRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER_NAME, "request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, (filteredRequest, filteredResponse) ->
                assertThat(MDC.get("requestId")).isEqualTo("request_123"));

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isEqualTo("request_123");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("replaces an unsafe request ID")
    void doFilterShouldReplaceUnsafeRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER_NAME, "unsafe request id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, (filteredRequest, filteredResponse) -> { });

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME))
                .isNotEqualTo("unsafe request id")
                .matches("[0-9a-f-]{36}");
    }
}
