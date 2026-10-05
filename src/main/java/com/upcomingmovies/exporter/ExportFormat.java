package com.upcomingmovies.exporter;

import java.util.Optional;

public enum ExportFormat {
    ICS("ics", "iCalendar (.ics)", ".ics", "upcoming_movies.ics"),
    JSON("json", "JSON (.json)", ".json", "upcoming_movies.json"),
    CARDS("cards", "Markdown Cards (.md)", ".md", "upcoming_movies.md"),
    TERMINAL("terminal", "Terminal Card View", ".txt", "upcoming_movies_cards.txt");

    private final String shorthand;
    private final String displayName;
    private final String extension;
    private final String defaultFilename;

    ExportFormat(String shorthand, String displayName, String extension, String defaultFilename) {
        this.shorthand = shorthand;
        this.displayName = displayName;
        this.extension = extension;
        this.defaultFilename = defaultFilename;
    }

    public String getShorthand() {
        return shorthand;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExtension() {
        return extension;
    }

    public String getDefaultFilename() {
        return defaultFilename;
    }

    public static Optional<ExportFormat> fromString(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.trim().toLowerCase();
        return switch (normalized) {
            case "ics", "ical", "calendar" -> Optional.of(ICS);
            case "json" -> Optional.of(JSON);
            case "cards", "card", "markdown-cards", "md-cards", "md" -> Optional.of(CARDS);
            case "terminal", "terminal-cards", "card-view", "term", "txt" -> Optional.of(TERMINAL);
            default -> Optional.empty();
        };
    }
}
