package com.upcomingmovies.scraper;

import com.upcomingmovies.config.AppConfig;
import com.upcomingmovies.model.MovieCalendarEvent;
import com.upcomingmovies.model.ScheduledMovie;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImdbScraper {

  private static final Logger logger = LoggerFactory.getLogger(ImdbScraper.class);

  public static final String IMDB_CALENDAR_URL_TEMPLATE =
      "https://www.imdb.com/calendar/?ref_=rlm&region=%s&type=MOVIE";
  public static final String DEFAULT_DESCRIPTION = "No description available";

  public static final String CALENDAR_SECTION_SELECTOR = "[data-testid=\"calendar-section\"]";
  public static final String MOVIE_ENTRY_SELECTOR = "[data-testid=\"coming-soon-entry\"]";
  public static final String MOVIE_TITLE_CLASS_NAME = "ipc-metadata-list-summary-item__t";
  public static final String RELEASE_DATE_CLASS_NAME = "ipc-title__text";
  public static final String PLOT_SELECTOR = "[data-testid=\"plot-xl\"]";
  public static final String POSTER_IMAGE_SELECTOR = "[data-testid=\"hero-media__poster\"] img";
  public static final String GENRES_SELECTOR = "[data-testid=\"genres\"] a, a.ipc-chip--on-base";

  private final AppConfig config;

  public ImdbScraper(AppConfig config) {
    this.config = config != null ? config : AppConfig.defaultConfig();
  }

  public ImdbScraper() {
    this(AppConfig.defaultConfig());
  }

  public List<MovieCalendarEvent> scrapeUpcomingMovies(String region) {
    String targetRegion =
        (region != null && !region.isBlank()) ? region.trim().toUpperCase() : config.region();
    String calendarUrl = String.format(IMDB_CALENDAR_URL_TEMPLATE, targetRegion);
    logger.info("Scraping upcoming movies for region: {}", targetRegion);

    WebDriver driver = null;
    try {
      driver = WebDriverFactory.createDriver(config);
      driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
      driver.get(calendarUrl);
      logger.debug("Loaded IMDB calendar page for region {}", targetRegion);

      WebDriverWait wait = new WebDriverWait(driver, config.timeout());
      String scriptCheck =
          "return !!document.getElementById('__NEXT_DATA__') || "
              + "document.querySelectorAll('[data-testid=\"calendar-section\"]').length > 0";
      wait.until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript(scriptCheck)));

      // 1. Try fast-path via embedded NEXT_DATA
      List<MovieCalendarEvent> fastEvents = scrapeViaNextData(driver);
      if (fastEvents != null && !fastEvents.isEmpty()) {
        logger.info("Successfully scraped {} movies via fast-path", fastEvents.size());
        return fastEvents;
      }

      // 2. Fallback to DOM-based extraction
      logger.info("NEXT_DATA not available; falling back to DOM extraction");
      List<ScheduledMovie> movieLinks = collectMovieLinksFromCalendarPage(driver);
      List<MovieCalendarEvent> scrapedEvents = scrapeAllMovieDetails(driver, movieLinks);

      logger.info("Successfully scraped {} movies via fallback", scrapedEvents.size());
      return scrapedEvents;

    } catch (WebDriverException error) {
      logger.error("WebDriver error scraping IMDB: {}", error.getMessage(), error);
      return List.of();
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected List<MovieCalendarEvent> scrapeViaNextData(WebDriver driver) {
    String jsScript =
        """
        const callback = arguments[arguments.length - 1];
        let nextData = null;
        try {
            const el = document.getElementById("__NEXT_DATA__");
            if (el && el.textContent) {
                nextData = JSON.parse(el.textContent);
            }
        } catch (e) {
            callback({ error: "Failed to parse __NEXT_DATA__" });
            return;
        }

        const pageProps = nextData && nextData.props && nextData.props.pageProps;
        if (!pageProps || !pageProps.groups) {
            callback({ error: "No groups found in __NEXT_DATA__" });
            return;
        }

        const groups = pageProps.groups;
        const movies = [];
        for (const g of groups) {
            for (const e of g.entries || []) {
                if (!e.id || !e.titleText) continue;
                movies.push({
                    id: e.id,
                    title: e.titleText,
                    release_date_str: e.releaseDate || "",
                    genres: e.genres || [],
                    poster: e.imageModel ? e.imageModel.url : null,
                    imdb_url: "https://www.imdb.com/title/" + e.id + "/"
                });
            }
        }

        if (movies.length === 0) {
            callback({ error: "No movies found in groups" });
            return;
        }

        // Parallel fetch plots for upcoming releases (up to first 50)
        const toFetch = movies.slice(0, 50);
        async function fetchPlots() {
            const results = {};
            const chunkSize = 25;
            for (let i = 0; i < toFetch.length; i += chunkSize) {
                const chunk = toFetch.slice(i, i + chunkSize);
                const chunkRes = await Promise.all(chunk.map(async m => {
                    const controller = new AbortController();
                    const timer = setTimeout(() => controller.abort(), 3500);
                    try {
                        const r = await fetch(
                            "/title/" + m.id + "/", { signal: controller.signal }
                        );
                        clearTimeout(timer);
                        const html = await r.text();
                        const doc = new DOMParser().parseFromString(html, "text/html");
                        const el = doc.querySelector("[data-testid='plot-xl']");
                        return { id: m.id, plot: el ? el.innerText.trim() : null };
                    } catch (e) {
                        clearTimeout(timer);
                        return { id: m.id, plot: null };
                    }
                }));
                for (const item of chunkRes) {
                    results[item.id] = item.plot;
                }
            }
            return results;
        }

        fetchPlots().then(plotMap => {
            for (const m of movies) {
                m.plot = plotMap[m.id] || "No description available";
            }
            callback({ movies: movies });
        }).catch(err => {
            callback({ movies: movies, warning: String(err) });
        });
        """;

    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;
      Object rawResult = js.executeAsyncScript(jsScript);
      if (!(rawResult instanceof Map<?, ?> rawMap)) {
        return null;
      }

      if (rawMap.containsKey("error")) {
        logger.debug("NEXT_DATA extraction error: {}", rawMap.get("error"));
        return null;
      }

      List<Map<String, Object>> rawMovies = (List<Map<String, Object>>) rawMap.get("movies");
      if (rawMovies == null || rawMovies.isEmpty()) {
        return null;
      }

      List<MovieCalendarEvent> events = new ArrayList<>();
      for (Map<String, Object> item : rawMovies) {
        String dateStr = (String) item.get("release_date_str");
        if (dateStr == null || dateStr.isBlank()) {
          continue;
        }

        LocalDate relDate;
        try {
          relDate = ScraperUtils.parseFlexibleReleaseDate(dateStr);
        } catch (Exception e) {
          logger.debug("Could not parse date '{}' for '{}'", dateStr, item.get("title"));
          continue;
        }

        String title = (String) item.get("title");
        String imdbUrl = (String) item.get("imdb_url");
        String plot = (String) item.get("plot");
        String poster = (String) item.get("poster");
        List<String> genres = (List<String>) item.get("genres");

        events.add(
            MovieCalendarEvent.builder()
                .title(title)
                .releaseDate(relDate)
                .imdbUrl(imdbUrl != null ? URI.create(imdbUrl) : null)
                .plotDescription(plot != null ? plot : DEFAULT_DESCRIPTION)
                .posterImageUrl(poster)
                .genres(genres != null ? genres : List.of())
                .build());
      }

      logger.info("Extracted {} movies via NEXT_DATA fast path", events.size());
      return events.isEmpty() ? null : events;

    } catch (WebDriverException e) {
      logger.debug("executeAsyncScript failed for NEXT_DATA: {}", e.getMessage());
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected List<ScheduledMovie> collectMovieLinksFromCalendarPage(WebDriver driver) {
    String jsScript =
        """
        const sections = document.querySelectorAll(arguments[0]);
        const results = [];
        for (const section of sections) {
            const dateEl = section.querySelector('.' + arguments[1]);
            if (!dateEl) continue;
            const dateText = dateEl.innerText.trim();

            const entries = section.querySelectorAll(arguments[2]);
            for (const entry of entries) {
                const titleEl = entry.querySelector('.' + arguments[3]);
                if (titleEl && titleEl.href) {
                    const titleText = titleEl.innerText.trim();
                    if (titleText && dateText) {
                        results.push({
                            title: titleText,
                            release_date_text: dateText,
                            imdb_url: titleEl.href.trim()
                        });
                    }
                }
            }
        }
        return results;
        """;

    JavascriptExecutor js = (JavascriptExecutor) driver;
    List<Map<String, String>> rawData =
        (List<Map<String, String>>)
            js.executeScript(
                jsScript,
                CALENDAR_SECTION_SELECTOR,
                RELEASE_DATE_CLASS_NAME,
                MOVIE_ENTRY_SELECTOR,
                MOVIE_TITLE_CLASS_NAME);

    List<ScheduledMovie> scheduledMovies = new ArrayList<>();
    if (rawData != null) {
      for (Map<String, String> row : rawData) {
        scheduledMovies.add(
            new ScheduledMovie(
                row.get("title"), row.get("release_date_text"), URI.create(row.get("imdb_url"))));
      }
    }
    logger.info("Found {} movies on calendar page via DOM", scheduledMovies.size());
    return scheduledMovies;
  }

  protected List<MovieCalendarEvent> scrapeAllMovieDetails(
      WebDriver driver, List<ScheduledMovie> movieLinks) {
    List<MovieCalendarEvent> events = new ArrayList<>();
    Map<String, MovieCalendarEvent> cache = new HashMap<>();

    for (int i = 0; i < movieLinks.size(); i++) {
      ScheduledMovie scheduledMovie = movieLinks.get(i);
      LocalDate parsedDate;
      try {
        parsedDate = ScraperUtils.parseImdbReleaseDate(scheduledMovie.releaseDateText());
      } catch (Exception e) {
        logger.error(
            "Could not parse date '{}' for '{}', skipping",
            scheduledMovie.releaseDateText(),
            scheduledMovie.title());
        continue;
      }

      String baseUrl = scheduledMovie.imdbUrl().toString().split("\\?")[0];
      if (cache.containsKey(baseUrl)) {
        MovieCalendarEvent cached = cache.get(baseUrl);
        events.add(
            MovieCalendarEvent.builder()
                .title(scheduledMovie.title())
                .releaseDate(parsedDate)
                .imdbUrl(scheduledMovie.imdbUrl())
                .plotDescription(cached.plotDescription())
                .posterImageUrl(cached.posterImageUrl().orElse(null))
                .genres(cached.genres())
                .build());
      } else {
        logger.debug(
            "Scraping details for movie {}/{}: {}",
            i + 1,
            movieLinks.size(),
            scheduledMovie.title());
        MovieCalendarEvent event = scrapeMovieDetailPage(driver, scheduledMovie, parsedDate);
        events.add(event);
        cache.put(baseUrl, event);
      }
    }
    return events;
  }

  @SuppressWarnings("unchecked")
  protected MovieCalendarEvent scrapeMovieDetailPage(
      WebDriver driver, ScheduledMovie movie, LocalDate releaseDate) {
    String plotDescription = DEFAULT_DESCRIPTION;
    String posterImageUrl = null;
    List<String> genres = List.of();

    try {
      driver.get(movie.imdbUrl().toString());

      try {
        WebDriverWait wait = new WebDriverWait(driver, config.timeout());
        wait.until(d -> d.findElement(By.cssSelector(PLOT_SELECTOR)));
      } catch (Exception ignored) {
      }

      String jsScript =
          """
          const plotEl = document.querySelector(arguments[0]);
          const posterEl = document.querySelector(arguments[1]);
          const genreEls = document.querySelectorAll(arguments[2]);
          const genres = Array.from(genreEls).map(el => el.innerText.trim()).filter(Boolean);
          return {
              plot: plotEl ? plotEl.innerText.trim() : null,
              poster: posterEl ? posterEl.src : null,
              genres: genres
          };
          """;

      JavascriptExecutor js = (JavascriptExecutor) driver;
      Map<String, Object> details =
          (Map<String, Object>)
              js.executeScript(jsScript, PLOT_SELECTOR, POSTER_IMAGE_SELECTOR, GENRES_SELECTOR);

      if (details != null) {
        if (details.get("plot") instanceof String p && !p.isBlank()) {
          plotDescription = p;
        }
        if (details.get("poster") instanceof String post && !post.isBlank()) {
          posterImageUrl = post;
        }
        if (details.get("genres") instanceof List<?> g) {
          genres = (List<String>) g;
        }
      }
    } catch (WebDriverException e) {
      logger.warn("Could not load detail page for '{}': {}", movie.title(), e.getMessage());
    }

    return MovieCalendarEvent.builder()
        .title(movie.title())
        .releaseDate(releaseDate)
        .imdbUrl(movie.imdbUrl())
        .plotDescription(plotDescription)
        .posterImageUrl(posterImageUrl)
        .genres(genres)
        .build();
  }
}
