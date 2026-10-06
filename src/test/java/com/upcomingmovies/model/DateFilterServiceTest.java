package com.upcomingmovies.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DateFilterServiceTest {

  private MovieCalendarEvent createMovie(String title, LocalDate date) {
    return new MovieCalendarEvent(
        title,
        date,
        URI.create("https://www.imdb.com/title/tt1234567/"),
        "Description",
        Optional.empty(),
        List.of("Action", "Drama"));
  }

  @Test
  void testFilterByDateRange() {
    var m1 = createMovie("M1", LocalDate.of(2026, 10, 1));
    var m2 = createMovie("M2", LocalDate.of(2026, 10, 5));
    var m3 = createMovie("M3", LocalDate.of(2026, 10, 10));
    var list = List.of(m1, m2, m3);

    var result =
        DateFilterService.filterByDateRange(
            list, LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 6));

    assertThat(result).containsExactly(m2);
  }

  @Test
  void testFilterWeekendFromWednesday() {
    // Wednesday 2026-10-07 -> Weekend is Friday 2026-10-09 to Sunday 2026-10-11
    var wed = LocalDate.of(2026, 10, 7);
    var thuMovie = createMovie("Thu", LocalDate.of(2026, 10, 8));
    var friMovie = createMovie("Fri", LocalDate.of(2026, 10, 9));
    var satMovie = createMovie("Sat", LocalDate.of(2026, 10, 10));
    var sunMovie = createMovie("Sun", LocalDate.of(2026, 10, 11));
    var monMovie = createMovie("Mon", LocalDate.of(2026, 10, 12));

    var list = List.of(thuMovie, friMovie, satMovie, sunMovie, monMovie);
    var result = DateFilterService.filterWeekendMovies(list, wed);

    assertThat(result).containsExactly(friMovie, satMovie, sunMovie);
  }

  @Test
  void testFilterWeekendFromSaturday() {
    // Saturday 2026-10-10 -> Weekend is still Friday 2026-10-09 to Sunday 2026-10-11
    var sat = LocalDate.of(2026, 10, 10);
    var friMovie = createMovie("Fri", LocalDate.of(2026, 10, 9));
    var sunMovie = createMovie("Sun", LocalDate.of(2026, 10, 11));
    var nextFri = createMovie("NextFri", LocalDate.of(2026, 10, 16));

    var list = List.of(friMovie, sunMovie, nextFri);
    var result = DateFilterService.filterWeekendMovies(list, sat);

    assertThat(result).containsExactly(friMovie, sunMovie);
  }
}
