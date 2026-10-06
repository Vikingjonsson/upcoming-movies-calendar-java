package com.upcomingmovies.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IcsExporterTest {

  @Nested
  class TestGenerateCalendarEventUid {
    @Test
    void testSameInputProducesSameUid() {
      String uid1 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 3, 29));
      String uid2 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 3, 29));
      assertThat(uid1).isEqualTo(uid2);
    }

    @Test
    void testDifferentUrlProducesDifferentUid() {
      String uid1 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 3, 29));
      String uid2 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt456", LocalDate.of(2026, 3, 29));
      assertThat(uid1).isNotEqualTo(uid2);
    }

    @Test
    void testDifferentDateProducesDifferentUid() {
      String uid1 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 3, 29));
      String uid2 =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 4, 1));
      assertThat(uid1).isNotEqualTo(uid2);
    }

    @Test
    void testUidEndsWithDomainSuffix() {
      String uid =
          IcsExporter.generateCalendarEventUid(
              "https://imdb.com/title/tt123", LocalDate.of(2026, 3, 29));
      assertThat(uid).endsWith("@upcoming-movies");
    }
  }

  @Nested
  class TestBuildCalendar {
    @Test
    void testCalendarPropertiesAndEvents(@TempDir Path tempDir) throws IOException {
      var movie =
          new MovieCalendarEvent(
              "Inception",
              LocalDate.of(2026, 5, 10),
              URI.create("https://www.imdb.com/title/tt1375666/"),
              "A dream within a dream",
              Optional.of(URI.create("https://example.com/poster.jpg")),
              List.of("Sci-Fi", "Action"));

      IcsExporter exporter = new IcsExporter("My Calendar");
      ICalendar calendar = exporter.buildCalendar(List.of(movie));

      assertThat(calendar.getProductId().getValue()).isEqualTo("Upcoming Movies Calendar");
      assertThat(calendar.getEvents()).hasSize(1);

      VEvent event = calendar.getEvents().get(0);
      assertThat(event.getSummary().getValue()).isEqualTo("Inception");
      assertThat(event.getDescription().getValue()).isEqualTo("A dream within a dream");
      assertThat(event.getUrl().getValue()).isEqualTo("https://www.imdb.com/title/tt1375666/");
      assertThat(event.getAttachments()).hasSize(1);
      assertThat(event.getCategories().get(0).getValues()).containsExactly("Sci-Fi", "Action");

      Path out = tempDir.resolve("test.ics");
      exporter.export(List.of(movie), out);
      assertThat(Files.exists(out)).isTrue();
      assertThat(Files.readString(out)).contains("BEGIN:VCALENDAR").contains("Inception");
    }
  }
}
