package com.upcomingmovies.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import com.upcomingmovies.model.MovieCalendarEvent;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CardExporterTest {

    private MovieCalendarEvent sampleMovie() {
        return new MovieCalendarEvent(
            "Dune: Part Two",
            LocalDate.of(2026, 3, 1),
            URI.create("https://www.imdb.com/title/tt15239678/"),
            "Paul Atreides unites with Chani and the Fremen",
            Optional.of(URI.create("https://example.com/dune.jpg")),
            List.of("Action", "Adventure", "Sci-Fi")
        );
    }

    @Test
    void testFormatMovieCard() {
        var movie = sampleMovie();
        String card = MarkdownCardExporter.formatMovieCard(movie);

        assertThat(card).contains("### 🎬 [Dune: Part Two](https://www.imdb.com/title/tt15239678/)");
        assertThat(card).contains("**Release Date**:");
        assertThat(card).contains("`Action` `Adventure` `Sci-Fi`");
        assertThat(card).contains("Paul Atreides unites with Chani and the Fremen");
        assertThat(card).contains("🖼 Image: [Dune: Part Two Poster]");
    }

    @Test
    void testCarouselExport() {
        var movie = sampleMovie();
        MarkdownCardExporter exporter = new MarkdownCardExporter("Upcoming Movies", true);
        String output = exporter.renderString(List.of(movie));

        assertThat(output).contains("````carousel");
        assertThat(output).contains("Dune: Part Two");
        assertThat(output).contains("````");
    }

    @Test
    void testTerminalCardExport() {
        var movie = sampleMovie();
        TerminalCardExporter exporter = new TerminalCardExporter(60);
        String output = exporter.renderString(List.of(movie));

        assertThat(output).contains("[1] 🎬 Dune: Part Two  [MOVIE]");
        assertThat(output).contains("📅 Release:");
        assertThat(output).contains("🎭 Genres:  Action, Adventure, Sci-Fi");
        assertThat(output).contains("🔗 Link:    https://www.imdb.com/title/tt15239678/");
    }
}
