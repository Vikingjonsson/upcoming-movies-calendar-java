package com.upcomingmovies.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record MovieCalendarEvent(
    @JsonProperty("title") String title,
    @JsonProperty("release_date") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate releaseDate,
    @JsonProperty("imdb_url") URI imdbUrl,
    @JsonProperty("plot_description") String plotDescription,
    @JsonProperty("poster_image_url") Optional<URI> posterImageUrl,
    @JsonProperty("genres") List<String> genres) {
  public MovieCalendarEvent {
    if (title == null || title.isBlank()) {
      title = "Untitled";
    }
    if (plotDescription == null || plotDescription.isBlank()) {
      plotDescription = "No description available";
    }
    genres = genres == null ? List.of() : List.copyOf(genres);
    posterImageUrl = posterImageUrl == null ? Optional.empty() : posterImageUrl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String title;
    private LocalDate releaseDate;
    private URI imdbUrl;
    private String plotDescription = "No description available";
    private Optional<URI> posterImageUrl = Optional.empty();
    private List<String> genres = List.of();

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder releaseDate(LocalDate releaseDate) {
      this.releaseDate = releaseDate;
      return this;
    }

    public Builder imdbUrl(URI imdbUrl) {
      this.imdbUrl = imdbUrl;
      return this;
    }

    public Builder imdbUrl(String imdbUrl) {
      this.imdbUrl = imdbUrl != null ? URI.create(imdbUrl) : null;
      return this;
    }

    public Builder plotDescription(String plotDescription) {
      this.plotDescription = plotDescription;
      return this;
    }

    public Builder posterImageUrl(URI posterImageUrl) {
      this.posterImageUrl = Optional.ofNullable(posterImageUrl);
      return this;
    }

    public Builder posterImageUrl(String posterImageUrl) {
      this.posterImageUrl = (posterImageUrl != null && !posterImageUrl.isBlank())
          ? Optional.of(URI.create(posterImageUrl))
          : Optional.empty();
      return this;
    }

    public Builder genres(List<String> genres) {
      this.genres = genres != null ? genres : List.of();
      return this;
    }

    public MovieCalendarEvent build() {
      return new MovieCalendarEvent(title, releaseDate, imdbUrl, plotDescription, posterImageUrl, genres);
    }
  }
}
