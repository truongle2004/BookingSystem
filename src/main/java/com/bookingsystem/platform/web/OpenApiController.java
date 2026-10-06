package com.bookingsystem.platform.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves the version-controlled public API contract. */
@RestController
public class OpenApiController {

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public Resource openApi() {
        return new ClassPathResource("openapi/openapi.yaml");
    }
}
