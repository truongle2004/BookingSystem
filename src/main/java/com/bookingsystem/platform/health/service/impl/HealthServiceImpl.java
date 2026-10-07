package com.bookingsystem.platform.health.service.impl;

import com.bookingsystem.platform.health.service.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** JDBC-backed implementation of the health service contract. */
@Service
public class HealthServiceImpl implements HealthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public HealthServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isDatabaseReady() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.warn("Database readiness check failed", exception);
            return false;
        }
    }
}
