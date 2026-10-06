package com.upcomingmovies.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonExporterTest {

  @Test
  void testSerializationAndDeserializationRoundtrip(@TempDir Path tempDir) throws IOException {
    MovieCalendarEvent movie =
        new MovieCalendarEvent(
            "Interstellar",
            LocalDate.of(2026, 11, 7),
            URI.create("https://www.imdb.com/title/tt0816692/"),
            "A team of explorers travel through a wormhole",
            Optional.of(URI.create("https://example.com/interstellar.jpg")),
            List.of("Adventure", "Drama", "Sci-Fi"));

    JsonExporter exporter = new JsonExporter();
    Path jsonFile = tempDir.resolve("movies.json");

    exporter.export(List.of(movie), jsonFile);
    assertThat(jsonFile).exists();

    List<MovieCalendarEvent> loaded = JsonExporter.loadFromFile(jsonFile);
    assertThat(loaded).hasSize(1);

    MovieCalendarEvent loadedMovie = loaded.get(0);
    assertThat(loadedMovie.title()).isEqualTo("Interstellar");
    assertThat(loadedMovie.releaseDate()).isEqualTo(LocalDate.of(2026, 11, 7));
    assertThat(loadedMovie.imdbUrl())
        .isEqualTo(URI.create("https://www.imdb.com/title/tt0816692/"));
    assertThat(loadedMovie.plotDescription())
        .isEqualTo("A team of explorers travel through a wormhole");
    assertThat(loadedMovie.posterImageUrl())
        .contains(URI.create("https://example.com/interstellar.jpg"));
    assertThat(loadedMovie.genres()).containsExactly("Adventure", "Drama", "Sci-Fi");
  }
}
