package com.upcomingmovies.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.upcomingmovies.exporter.JsonExporter;
import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class MovieCliCommandTest {

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;

  @BeforeEach
  void setUp() {
    outContent = new ByteArrayOutputStream();
    errContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  void testListRegions() {
    CommandLine cmd = new CommandLine(new MovieCliCommand());
    int exitCode = cmd.execute("-l");

    assertThat(exitCode).isEqualTo(0);
    assertThat(outContent.toString())
        .contains("Common IMDB Region Codes:")
        .contains("SE   - Sweden (default)")
        .contains("US   - United States");
  }

  @Test
  void testHelpFlag() {
    CommandLine cmd = new CommandLine(new MovieCliCommand());
    int exitCode = cmd.execute("--help");

    assertThat(exitCode).isEqualTo(0);
    assertThat(outContent.toString())
        .contains("Scrape upcoming movies from IMDB and export to ICS/JSON/Cards.")
        .contains("--card-view")
        .contains("-f, --format");
  }

  @Test
  void testLoadFromJsonAndExportCards(@TempDir Path tempDir) throws Exception {
    MovieCalendarEvent movie =
        new MovieCalendarEvent(
            "Gladiator II",
            LocalDate.of(2026, 11, 22),
            URI.create("https://www.imdb.com/title/tt2106476/"),
            "Lucius enters the Colosseum",
            Optional.of(URI.create("https://example.com/gladiator.jpg")),
            List.of("Action", "Drama"));

    Path jsonFile = tempDir.resolve("input.json");
    new JsonExporter().export(List.of(movie), jsonFile);

    Path cardsOut = tempDir.resolve("output.md");

    CommandLine cmd = new CommandLine(new MovieCliCommand());
    int exitCode =
        cmd.execute(
            "-i", jsonFile.toString(), "-f", "cards", "-o", cardsOut.toString(), "--no-prompt");

    assertThat(exitCode).isEqualTo(0);
    assertThat(cardsOut).exists();
    String md = Files.readString(cardsOut);
    assertThat(md).contains("Gladiator II").contains("Lucius enters the Colosseum");
  }

  @Test
  void testLoadFromJsonWithWeekendFilter(@TempDir Path tempDir) throws Exception {
    MovieCalendarEvent weekdayMovie =
        new MovieCalendarEvent(
            "Weekday Movie",
            LocalDate.of(2026, 10, 6), // Tuesday
            URI.create("https://www.imdb.com/title/tt111/"),
            "Weekday plot",
            Optional.empty(),
            List.of());

    Path jsonFile = tempDir.resolve("input.json");
    new JsonExporter().export(List.of(weekdayMovie), jsonFile);

    CommandLine cmd = new CommandLine(new MovieCliCommand());
    int exitCode = cmd.execute("-i", jsonFile.toString(), "--weekend", "--dry-run", "--no-prompt");

    assertThat(exitCode).isEqualTo(0);
    assertThat(outContent.toString()).contains("Filtered to 0 upcoming weekend movies.");
  }
}
