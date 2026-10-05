package com.upcomingmovies.exporter;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Attachment;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.Summary;
import biweekly.property.Uid;
import biweekly.property.Url;
import com.upcomingmovies.config.AppConfig;
import com.upcomingmovies.model.MovieCalendarEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IcsExporter implements MovieExporter {

    private static final Logger logger = LoggerFactory.getLogger(IcsExporter.class);

    public static final String EVENT_UID_DOMAIN = "@upcoming-movies";
    public static final int EVENT_UID_HASH_LENGTH = 16;

    private final String calendarName;

    public IcsExporter(String calendarName) {
        this.calendarName = (calendarName != null && !calendarName.isBlank())
            ? calendarName
            : AppConfig.DEFAULT_CALENDAR_NAME;
    }

    public IcsExporter() {
        this(AppConfig.DEFAULT_CALENDAR_NAME);
    }

    @Override
    public ExportFormat format() {
        return ExportFormat.ICS;
    }

    public static String generateCalendarEventUid(String imdbUrl, LocalDate releaseDate) {
        String rawIdentifier = (imdbUrl != null ? imdbUrl : "") + ":" + releaseDate.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawIdentifier.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash);
            String prefix = hex.substring(0, Math.min(EVENT_UID_HASH_LENGTH, hex.length()));
            return prefix + EVENT_UID_DOMAIN;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public ICalendar buildCalendar(List<MovieCalendarEvent> events) {
        ICalendar ical = new ICalendar();
        ical.setProductId("Upcoming Movies Calendar");
        ical.setName(calendarName);

        for (MovieCalendarEvent movieEvent : events) {
            VEvent event = new VEvent();

            String uid = generateCalendarEventUid(
                movieEvent.imdbUrl() != null ? movieEvent.imdbUrl().toString() : "",
                movieEvent.releaseDate()
            );
            event.setUid(new Uid(uid));

            Date startDate = Date.from(movieEvent.releaseDate().atStartOfDay(ZoneOffset.UTC).toInstant());
            Date endDate = Date.from(movieEvent.releaseDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

            event.setDateStart(new DateStart(startDate, false));
            event.setDateEnd(new DateEnd(endDate, false));

            event.setSummary(new Summary(movieEvent.title()));
            event.setDescription(new Description(movieEvent.plotDescription()));

            if (movieEvent.imdbUrl() != null) {
                event.setUrl(new Url(movieEvent.imdbUrl().toString()));
            }

            movieEvent.posterImageUrl().ifPresent(posterUri -> {
                Attachment attachment = new Attachment("image/jpeg", posterUri.toString());
                event.addAttachment(attachment);
            });

            if (!movieEvent.genres().isEmpty()) {
                event.addCategories(movieEvent.genres().toArray(new String[0]));
            }

            ical.addEvent(event);
        }

        return ical;
    }

    @Override
    public void export(List<MovieCalendarEvent> events, Path outputPath) throws IOException {
        String icsContent = renderString(events);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.writeString(outputPath, icsContent, StandardCharsets.UTF_8);
        logger.info("Calendar saved to {} ({} movies)", outputPath, events.size());
    }

    @Override
    public String renderString(List<MovieCalendarEvent> events) {
        ICalendar ical = buildCalendar(events);
        return Biweekly.write(ical).go();
    }
}
