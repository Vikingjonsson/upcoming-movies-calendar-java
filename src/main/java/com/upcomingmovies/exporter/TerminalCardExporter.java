package com.upcomingmovies.exporter;

import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TerminalCardExporter implements MovieExporter {

    private static final Logger logger = LoggerFactory.getLogger(TerminalCardExporter.class);

    public static final int DEFAULT_CARD_WIDTH = 70;

    private static final DateTimeFormatter TERMINAL_DATE_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy (EEEE)", Locale.US);

    private final int width;

    public TerminalCardExporter(int width) {
        this.width = width > 20 ? width : DEFAULT_CARD_WIDTH;
    }

    public TerminalCardExporter() {
        this(DEFAULT_CARD_WIDTH);
    }

    @Override
    public ExportFormat format() {
        return ExportFormat.TERMINAL;
    }

    public static String wrapText(String text, int width, String indent) {
        if (text == null || text.isBlank()) {
            return indent;
        }
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        StringBuilder currentLine = new StringBuilder(indent);

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > width && currentLine.length() > indent.length()) {
                sb.append(currentLine).append("\n");
                currentLine = new StringBuilder(indent).append(word);
            } else {
                if (currentLine.length() > indent.length()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            sb.append(currentLine);
        }
        return sb.toString();
    }

    public static String formatTerminalCard(MovieCalendarEvent event, int index, int width) {
        String border = "─".repeat(width);
        String formattedDate = event.releaseDate().format(TERMINAL_DATE_FORMAT);
        String genresText = event.genres().isEmpty() ? "N/A" : String.join(", ", event.genres());
        String posterText = event.posterImageUrl().map(uri -> uri.toString()).orElse("N/A");
        String imdbUrlStr = event.imdbUrl() != null ? event.imdbUrl().toString() : "N/A";

        List<String> lines = new ArrayList<>();
        lines.add(border);
        lines.add(String.format("[%d] 🎬 %s  [MOVIE]", index, event.title()));
        lines.add(String.format("   📅 Release: %s", formattedDate));
        lines.add(String.format("   🎭 Genres:  %s", genresText));
        lines.add(String.format("   🔗 Link:    %s", imdbUrlStr));
        lines.add(String.format("   🖼️  Poster:  %s", posterText));
        lines.add("");

        String plot = (event.plotDescription() != null && !event.plotDescription().isBlank())
            ? event.plotDescription().trim()
            : "No description available";

        int wrapWidth = Math.max(20, width - 3);
        String wrappedPlot = wrapText(plot, wrapWidth, "   ");
        lines.add(wrappedPlot);
        lines.add(border);

        return String.join("\n", lines);
    }

    @Override
    public String renderString(List<MovieCalendarEvent> events) {
        if (events == null || events.isEmpty()) {
            String border = "─".repeat(width);
            return String.format("%s%n* No upcoming movies found *%n%s%n", border, border);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < events.size(); i++) {
            sb.append(formatTerminalCard(events.get(i), i + 1, width)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void export(List<MovieCalendarEvent> events, Path outputPath) throws IOException {
        String content = renderString(events);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.writeString(outputPath, content, StandardCharsets.UTF_8);
        logger.info("Terminal cards saved to {} ({} movies)", outputPath, events.size());
    }
}
