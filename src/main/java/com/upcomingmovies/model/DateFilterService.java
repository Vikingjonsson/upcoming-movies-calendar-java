package com.upcomingmovies.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public final class DateFilterService {

  private DateFilterService() {}

  /** Filters movie events within an inclusive date range. */
  public static List<MovieCalendarEvent> filterByDateRange(
      List<MovieCalendarEvent> events, LocalDate startDate, LocalDate endDate) {
    if (events == null || events.isEmpty()) {
      return List.of();
    }
    return events.stream()
        .filter(
            event -> {
              if (startDate != null && event.releaseDate().isBefore(startDate)) {
                return false;
              }
              if (endDate != null && event.releaseDate().isAfter(endDate)) {
                return false;
              }
              return true;
            })
        .toList();
  }

  /**
   * Filters movies releasing on or around the upcoming weekend (Friday through Sunday).
   *
   * <p>If referenceDate is Monday-Thursday, returns coming Friday-Sunday. If referenceDate is
   * Friday, Saturday, or Sunday, returns this Friday-Sunday.
   */
  public static List<MovieCalendarEvent> filterWeekendMovies(
      List<MovieCalendarEvent> events, LocalDate referenceDate) {
    LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now();
    DayOfWeek dow = ref.getDayOfWeek();

    long daysToFriday;
    if (dow.getValue() < DayOfWeek.FRIDAY.getValue()) {
      daysToFriday = DayOfWeek.FRIDAY.getValue() - dow.getValue();
    } else if (dow == DayOfWeek.FRIDAY) {
      daysToFriday = 0;
    } else {
      // Saturday (6) -> -1 day to Friday; Sunday (7) -> -2 days to Friday
      daysToFriday = DayOfWeek.FRIDAY.getValue() - dow.getValue();
    }

    LocalDate friday = ref.plusDays(daysToFriday);
    LocalDate sunday = friday.plusDays(2);

    return filterByDateRange(events, friday, sunday);
  }

  public static List<MovieCalendarEvent> filterWeekendMovies(List<MovieCalendarEvent> events) {
    return filterWeekendMovies(events, LocalDate.now());
  }
}
