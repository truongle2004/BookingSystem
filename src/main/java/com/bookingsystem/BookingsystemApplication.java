package com.bookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Entry point for the booking system application. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BookingsystemApplication {

    private BookingsystemApplication() {
    }

    /**
     * Starts the Spring application.
     *
     * @param args application arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(BookingsystemApplication.class, args);
    }

}
