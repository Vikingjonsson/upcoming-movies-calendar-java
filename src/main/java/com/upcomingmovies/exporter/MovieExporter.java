package com.upcomingmovies.exporter;

import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public sealed interface MovieExporter permits
    IcsExporter,
    JsonExporter,
    MarkdownCardExporter,
    TerminalCardExporter {

    ExportFormat format();

    void export(List<MovieCalendarEvent> events, Path outputPath) throws IOException;

    String renderString(List<MovieCalendarEvent> events);
}
