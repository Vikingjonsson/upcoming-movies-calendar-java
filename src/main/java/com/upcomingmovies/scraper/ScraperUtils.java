package com.upcomingmovies.scraper;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class ScraperUtils {

    private ScraperUtils() {}

    private static final DateTimeFormatter IMDB_DATE_FORMAT = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMM d, yyyy")
        .toFormatter(Locale.US);

    private static final DateTimeFormatter IMDB_LONG_DATE_FORMAT = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMMM d, yyyy")
        .toFormatter(Locale.US);

    public static LocalDate parseImdbReleaseDate(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            throw new IllegalArgumentException("dateText cannot be empty");
        }
        String cleaned = dateText.trim();
        try {
            return LocalDate.parse(cleaned, IMDB_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(cleaned, IMDB_LONG_DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Unparseable IMDB date: '" + dateText + "'", ex);
            }
        }
    }

    public static LocalDate parseFlexibleReleaseDate(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            throw new IllegalArgumentException("dateText cannot be empty");
        }
        String cleaned = dateText.trim();

        // 1. Try RFC 1123 / 2822 (e.g. "Fri, 02 Oct 2026 00:00:00 GMT")
        try {
            return ZonedDateTime.parse(cleaned, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        // 2. Try ISO-8601 (e.g. "2026-10-02" or "2026-10-02T12:00:00Z")
        try {
            if (cleaned.contains("T")) {
                return ZonedDateTime.parse(cleaned).toLocalDate();
            }
            return LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }

        // 3. Fall back to standard IMDB date formats
        return parseImdbReleaseDate(cleaned);
    }
}
