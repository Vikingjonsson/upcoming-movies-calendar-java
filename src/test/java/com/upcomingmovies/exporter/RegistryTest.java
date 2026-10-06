package com.upcomingmovies.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RegistryTest {

  @Nested
  class TestNormalizeFormat {
    @Test
    void testNormalizeCards() {
      assertThat(ExportFormat.fromString("cards")).contains(ExportFormat.CARDS);
      assertThat(ExportFormat.fromString("card")).contains(ExportFormat.CARDS);
      assertThat(ExportFormat.fromString("CARD")).contains(ExportFormat.CARDS);
      assertThat(ExportFormat.fromString("markdown-cards")).contains(ExportFormat.CARDS);
    }

    @Test
    void testNormalizeTerminal() {
      assertThat(ExportFormat.fromString("terminal")).contains(ExportFormat.TERMINAL);
      assertThat(ExportFormat.fromString("terminal-cards")).contains(ExportFormat.TERMINAL);
      assertThat(ExportFormat.fromString("card-view")).contains(ExportFormat.TERMINAL);
      assertThat(ExportFormat.fromString("term")).contains(ExportFormat.TERMINAL);
    }

    @Test
    void testNormalizeStandardFormats() {
      assertThat(ExportFormat.fromString("json")).contains(ExportFormat.JSON);
      assertThat(ExportFormat.fromString("ics")).contains(ExportFormat.ICS);
      assertThat(ExportFormat.fromString("JSON")).contains(ExportFormat.JSON);
    }
  }

  @Nested
  class TestResolveOutputFilename {
    @Test
    void testCustomOutputPreserved() {
      assertThat(ExporterRegistry.resolveOutputFilename("custom.json", ExportFormat.JSON))
          .isEqualTo(Paths.get("custom.json"));
      assertThat(ExporterRegistry.resolveOutputFilename("custom.ics", ExportFormat.ICS))
          .isEqualTo(Paths.get("custom.ics"));
      assertThat(ExporterRegistry.resolveOutputFilename("custom.md", ExportFormat.CARDS))
          .isEqualTo(Paths.get("custom.md"));
      assertThat(ExporterRegistry.resolveOutputFilename("custom.txt", ExportFormat.TERMINAL))
          .isEqualTo(Paths.get("custom.txt"));
    }

    @Test
    void testDefaultFilenames() {
      assertThat(ExporterRegistry.resolveOutputFilename(null, ExportFormat.ICS))
          .isEqualTo(Paths.get("upcoming_movies.ics"));
      assertThat(ExporterRegistry.resolveOutputFilename("", ExportFormat.JSON))
          .isEqualTo(Paths.get("upcoming_movies.json"));
      assertThat(ExporterRegistry.resolveOutputFilename(null, ExportFormat.CARDS))
          .isEqualTo(Paths.get("upcoming_movies.md"));
      assertThat(ExporterRegistry.resolveOutputFilename(null, ExportFormat.TERMINAL))
          .isEqualTo(Paths.get("upcoming_movies_cards.txt"));
    }
  }
}
