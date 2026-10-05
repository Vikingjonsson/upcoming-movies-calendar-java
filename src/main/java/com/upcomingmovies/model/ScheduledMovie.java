package com.upcomingmovies.model;

import java.net.URI;

public record ScheduledMovie(
    String title,
    String releaseDateText,
    URI imdbUrl
) {
    public ScheduledMovie {
        if (title == null) {
            title = "";
        }
        if (releaseDateText == null) {
            releaseDateText = "";
        }
    }
}
