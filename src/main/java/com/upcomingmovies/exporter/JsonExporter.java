package com.upcomingmovies.exporter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonExporter implements MovieExporter {

  private static final Logger logger = LoggerFactory.getLogger(JsonExporter.class);

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .registerModule(new Jdk8Module())
          .enable(SerializationFeature.INDENT_OUTPUT)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Override
  public ExportFormat format() {
    return ExportFormat.JSON;
  }

  @Override
  public void export(List<MovieCalendarEvent> events, Path outputPath) throws IOException {
    String json = renderString(events);
    if (outputPath.getParent() != null) {
      Files.createDirectories(outputPath.getParent());
    }
    Files.writeString(outputPath, json, StandardCharsets.UTF_8);
    logger.info("JSON data saved to {} ({} movies)", outputPath, events.size());
  }

  @Override
  public String renderString(List<MovieCalendarEvent> events) {
    try {
      return MAPPER.writeValueAsString(events != null ? events : List.of());
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize movies to JSON", e);
    }
  }

  public static List<MovieCalendarEvent> loadFromFile(Path inputPath) throws IOException {
    byte[] bytes = Files.readAllBytes(inputPath);
    List<MovieCalendarEvent> events =
        MAPPER.readValue(bytes, new TypeReference<List<MovieCalendarEvent>>() {});
    logger.info("Loaded {} movies from JSON file {}", events.size(), inputPath);
    return events;
  }
}
