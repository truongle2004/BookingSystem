package com.bookingsystem.platform.health.service;

/** Provides process and dependency health checks. */
public interface HealthService {

    /** Checks database reachability without exposing database exceptions to the web layer. */
    boolean isDatabaseReady();
}
