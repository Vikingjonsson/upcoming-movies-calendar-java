package com.upcomingmovies.exporter;

import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExporterRegistry {

    private final Map<ExportFormat, MovieExporter> exporters = new EnumMap<>(ExportFormat.class);

    public ExporterRegistry(String calendarName, boolean asCarousel, int terminalWidth) {
        exporters.put(ExportFormat.ICS, new IcsExporter(calendarName));
        exporters.put(ExportFormat.JSON, new JsonExporter());
        exporters.put(ExportFormat.CARDS, new MarkdownCardExporter("Upcoming Movies", asCarousel));
        exporters.put(ExportFormat.TERMINAL, new TerminalCardExporter(terminalWidth));
    }

    public ExporterRegistry() {
        this("Upcoming Movies", false, TerminalCardExporter.DEFAULT_CARD_WIDTH);
    }

    public MovieExporter getExporter(ExportFormat format) {
        return exporters.get(format);
    }

    public Optional<MovieExporter> findExporter(String formatName) {
        return ExportFormat.fromString(formatName).map(this::getExporter);
    }

    public static Path resolveOutputFilename(String customPath, ExportFormat format) {
        if (customPath != null && !customPath.isBlank()) {
            return Paths.get(customPath.trim());
        }
        return Paths.get(format.getDefaultFilename());
    }

    public void export(List<MovieCalendarEvent> events, ExportFormat format, Path outputPath) throws IOException {
        MovieExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        exporter.export(events, outputPath);
    }
}
