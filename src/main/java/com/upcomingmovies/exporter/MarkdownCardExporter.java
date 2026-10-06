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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkdownCardExporter implements MovieExporter {

  private static final Logger logger = LoggerFactory.getLogger(MarkdownCardExporter.class);

  private static final DateTimeFormatter CARD_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MMMM d, yyyy (EEEE)", Locale.US);

  private static final DateTimeFormatter CAROUSEL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

  private final String title;
  private final boolean asCarousel;

  public MarkdownCardExporter(String title, boolean asCarousel) {
    this.title = (title != null && !title.isBlank()) ? title : "Upcoming Movies";
    this.asCarousel = asCarousel;
  }

  public MarkdownCardExporter() {
    this("Upcoming Movies", false);
  }

  @Override
  public ExportFormat format() {
    return ExportFormat.CARDS;
  }

  public static String formatMovieCard(MovieCalendarEvent event) {
    String formattedDate = event.releaseDate().format(CARD_DATE_FORMAT);
    String imdbUrlStr = event.imdbUrl() != null ? event.imdbUrl().toString() : "#";

    List<String> lines = new ArrayList<>();
    lines.add(String.format("### 🎬 [%s](%s)", event.title(), imdbUrlStr));
    lines.add(String.format("> **Release Date**: %s  ", formattedDate));

    if (!event.genres().isEmpty()) {
      String genreTags =
          event.genres().stream().map(g -> "`" + g + "`").collect(Collectors.joining(" "));
      lines.add(String.format("> **Genres**: %s  ", genreTags));
    }

    lines.add(">");
    lines.add("> " + event.plotDescription());

    event
        .posterImageUrl()
        .ifPresent(
            poster -> {
              lines.add(">");
              lines.add(String.format("> 🖼 Image: [%s Poster](%s)", event.title(), imdbUrlStr));
            });

    lines.add("");
    return String.join("\n", lines);
  }

  public static String formatCarouselSlide(MovieCalendarEvent event) {
    String formattedDate = event.releaseDate().format(CAROUSEL_DATE_FORMAT);
    String imdbUrlStr = event.imdbUrl() != null ? event.imdbUrl().toString() : "#";

    List<String> lines = new ArrayList<>();
    lines.add(String.format("### 🎬 [%s](%s)", event.title(), imdbUrlStr));
    lines.add(String.format("**Release Date**: %s  ", formattedDate));

    if (!event.genres().isEmpty()) {
      lines.add(String.format("**Genres**: %s  ", String.join(", ", event.genres())));
    }

    event
        .posterImageUrl()
        .ifPresent(
            poster -> {
              lines.add(String.format("🖼 Image: [%s Poster](%s)", event.title(), imdbUrlStr));
            });

    lines.add(String.format("%n%s%n", event.plotDescription()));
    return String.join("\n", lines);
  }

  @Override
  public String renderString(List<MovieCalendarEvent> events) {
    if (events == null || events.isEmpty()) {
      return String.format("# %s%n%n*No upcoming movies found.*%n", title);
    }

    if (asCarousel) {
      String carouselBody =
          events.stream()
              .map(MarkdownCardExporter::formatCarouselSlide)
              .collect(Collectors.joining("\n<!-- slide -->\n"));

      return String.format("# %s%n%n````carousel%n%s%n````%n", title, carouselBody);
    }

    String cardsContent =
        events.stream()
            .map(MarkdownCardExporter::formatMovieCard)
            .collect(Collectors.joining("\n---\n\n"));

    return String.format("# %s%n%n%s%n", title, cardsContent);
  }

  @Override
  public void export(List<MovieCalendarEvent> events, Path outputPath) throws IOException {
    String content = renderString(events);
    if (outputPath.getParent() != null) {
      Files.createDirectories(outputPath.getParent());
    }
    Files.writeString(outputPath, content, StandardCharsets.UTF_8);
    logger.info("Markdown cards saved to {} ({} movies)", outputPath, events.size());
  }
}
