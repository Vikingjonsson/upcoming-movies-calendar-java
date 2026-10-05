package com.upcomingmovies.cli;

import com.upcomingmovies.config.AppConfig;
import com.upcomingmovies.config.Region;
import com.upcomingmovies.exporter.ExportFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class InteractivePrompt {

    private final Scanner scanner;
    private final boolean isInteractive;

    public InteractivePrompt() {
        this(System.console() != null);
    }

    public InteractivePrompt(boolean isInteractive) {
        this.isInteractive = isInteractive;
        this.scanner = new Scanner(System.in);
    }

    public static void displayRegions() {
        String defaultRegion = AppConfig.DEFAULT_REGION;
        System.out.println("Common IMDB Region Codes:");
        for (Region r : Region.values()) {
            String suffix = r.getCode().equals(defaultRegion) ? " (default)" : "";
            System.out.printf("  %-4s - %s%s%n", r.getCode(), r.getCountryName(), suffix);
        }
        System.out.println("\nUse with: upcoming-movies -r <CODE> (e.g. upcoming-movies -r US)");
    }

    public String promptRegion(String defaultRegion) {
        String def = (defaultRegion != null && !defaultRegion.isBlank()) ? defaultRegion.toUpperCase() : AppConfig.DEFAULT_REGION;
        if (!isInteractive) {
            return def;
        }

        System.out.println("\nSelect IMDB region:");
        List<Region> regions = List.of(Region.values());
        for (int i = 0; i < regions.size(); i++) {
            Region r = regions.get(i);
            String suffix = r.getCode().equals(def) ? " (default)" : "";
            System.out.printf("  %2d) %s - %s%s%n", i + 1, r.getCode(), r.getCountryName(), suffix);
        }
        System.out.println("      Or enter any country code (e.g. US, JP, IT, NO)");

        System.out.printf("Select region [1-%d or code] (default: %s): ", regions.size(), def);
        String input = readLine();
        if (input == null || input.isBlank()) {
            return def;
        }

        String trimmed = input.trim();
        try {
            int index = Integer.parseInt(trimmed) - 1;
            if (index >= 0 && index < regions.size()) {
                return regions.get(index).getCode();
            }
        } catch (NumberFormatException ignored) {
        }

        return trimmed.toUpperCase();
    }

    public ExportFormat promptFormat(ExportFormat defaultFormat) {
        ExportFormat def = defaultFormat != null ? defaultFormat : ExportFormat.ICS;
        if (!isInteractive) {
            return def;
        }

        System.out.println("\nSelect output format:");
        System.out.println("  1) iCalendar (.ics) [default]");
        System.out.println("  2) JSON (.json)");
        System.out.println("  3) Markdown Cards (.md)");
        System.out.println("  4) Terminal Card View (direct print or .txt)");

        System.out.print("Select format [1-4] (default: 1): ");
        String input = readLine();
        if (input == null || input.isBlank()) {
            return def;
        }

        String trimmed = input.trim();
        return switch (trimmed) {
            case "1", "ics" -> ExportFormat.ICS;
            case "2", "json" -> ExportFormat.JSON;
            case "3", "cards" -> ExportFormat.CARDS;
            case "4", "terminal" -> ExportFormat.TERMINAL;
            default -> ExportFormat.fromString(trimmed).orElse(def);
        };
    }

    public record DateFilterResult(LocalDate startDate, LocalDate endDate, boolean isWeekend) {}

    public DateFilterResult promptDateFilter() {
        if (!isInteractive) {
            return new DateFilterResult(null, null, false);
        }

        System.out.println("\nSelect date filter:");
        System.out.println("  1) All upcoming releases (default)");
        System.out.println("  2) This weekend (Friday - Sunday)");
        System.out.println("  3) Today's releases");
        System.out.println("  4) Custom date range");

        System.out.print("Select filter [1-4] (default: 1): ");
        String input = readLine();
        if (input == null || input.isBlank() || "1".equals(input.trim())) {
            return new DateFilterResult(null, null, false);
        }

        String trimmed = input.trim();
        if ("2".equals(trimmed)) {
            return new DateFilterResult(null, null, true);
        }
        if ("3".equals(trimmed)) {
            LocalDate today = LocalDate.now();
            return new DateFilterResult(today, today, false);
        }
        if ("4".equals(trimmed)) {
            LocalDate fromDate = null;
            LocalDate toDate = null;

            System.out.print("  From date (YYYY-MM-DD) [empty for any]: ");
            String fromStr = readLine();
            if (fromStr != null && !fromStr.isBlank()) {
                try {
                    fromDate = LocalDate.parse(fromStr.trim());
                } catch (DateTimeParseException e) {
                    System.out.println("  Invalid date format, skipping start date limit.");
                }
            }

            System.out.print("  To date (YYYY-MM-DD) [empty for any]: ");
            String toStr = readLine();
            if (toStr != null && !toStr.isBlank()) {
                try {
                    toDate = LocalDate.parse(toStr.trim());
                } catch (DateTimeParseException e) {
                    System.out.println("  Invalid date format, skipping end date limit.");
                }
            }
            return new DateFilterResult(fromDate, toDate, false);
        }

        return new DateFilterResult(null, null, false);
    }

    public boolean promptCardCarousel() {
        if (!isInteractive) {
            return false;
        }
        System.out.println("\nSelect card presentation style:");
        System.out.println("  1) Standard markdown cards (default)");
        System.out.println("  2) Antigravity carousel");

        System.out.print("Select style [1-2] (default: 1): ");
        String input = readLine();
        return "2".equals(input != null ? input.trim() : "");
    }

    public String promptOutputFilepath(String defaultFilename, boolean isTerminal) {
        if (!isInteractive) {
            return isTerminal ? null : defaultFilename;
        }

        String promptLabel = isTerminal
            ? "\nOutput file path [press Enter to print to terminal]: "
            : String.format("\nOutput file path (default: %s): ", defaultFilename);

        System.out.print(promptLabel);
        String input = readLine();
        if (input == null || input.isBlank()) {
            return isTerminal ? null : defaultFilename;
        }
        return input.trim();
    }

    private String readLine() {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
