package com.upcomingmovies.config;

import java.time.Duration;

public record AppConfig(
    String region,
    String outputFilename,
    String calendarName,
    boolean headless,
    String windowSize,
    Duration timeout
) {
    public static final String DEFAULT_REGION = "SE";
    public static final String DEFAULT_CALENDAR_NAME = "Upcoming Movies";
    public static final String DEFAULT_OUTPUT_FILENAME = "upcoming_movies.ics";
    public static final boolean DEFAULT_HEADLESS = true;
    public static final String DEFAULT_WINDOW_SIZE = "1440,900";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    public static AppConfig defaultConfig() {
        return new AppConfig(
            DEFAULT_REGION,
            DEFAULT_OUTPUT_FILENAME,
            DEFAULT_CALENDAR_NAME,
            DEFAULT_HEADLESS,
            DEFAULT_WINDOW_SIZE,
            DEFAULT_TIMEOUT
        );
    }
}
