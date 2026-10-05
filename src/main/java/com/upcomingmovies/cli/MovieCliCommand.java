package com.upcomingmovies.cli;

import com.upcomingmovies.config.AppConfig;
import com.upcomingmovies.config.Region;
import com.upcomingmovies.exporter.ExportFormat;
import com.upcomingmovies.exporter.ExporterRegistry;
import com.upcomingmovies.exporter.JsonExporter;
import com.upcomingmovies.exporter.MovieExporter;
import com.upcomingmovies.model.DateFilterService;
import com.upcomingmovies.model.MovieCalendarEvent;
import com.upcomingmovies.scraper.ImdbScraper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "upcoming-movies",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Scrape upcoming movies from IMDB and export to ICS/JSON/Cards.",
    footer = {
        "",
        "Examples:",
        "  upcoming-movies                     # Interactive mode (prompts)",
        "  upcoming-movies --card-view         # View movies as cards in terminal",
        "  upcoming-movies -f terminal         # View movies as terminal cards",
        "  upcoming-movies -i data.json --card-view # Card view from cached JSON",
        "  upcoming-movies -f json             # Export to JSON (Sweden)",
        "  upcoming-movies -f cards --weekend  # Export weekend movies as cards",
        "  upcoming-movies -f ics -r US        # Export to ICS (United States)",
        "  upcoming-movies -l                  # List common region codes"
    }
)
public class MovieCliCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MovieCliCommand.class);

    @Option(names = {"-f", "--format"}, description = "Output format ('ics', 'json', 'cards', 'terminal'). Prompts if omitted.")
    private String formatStr;

    @Option(names = {"--card-view"}, description = "Display movies as visual cards directly in the terminal.")
    private boolean cardView;

    @Option(names = {"-r", "--region"}, description = "IMDB region code (e.g. SE, US, GB). Prompts if omitted.")
    private String region;

    @Option(names = {"-o", "--output"}, description = "Output filename (default: upcoming_movies.<format>).")
    private String outputFilename;

    @Option(names = {"-i", "--from-json"}, paramLabel = "PATH", description = "Load movies from an existing JSON file instead of scraping IMDB.")
    private Path fromJsonPath;

    @Option(names = {"-c", "--calendar-name"}, defaultValue = "Upcoming Movies", description = "Calendar name for ICS export (default: 'Upcoming Movies').")
    private String calendarName;

    @Option(names = {"--today"}, description = "Filter movies releasing today.")
    private boolean filterToday;

    @Option(names = {"--weekend"}, description = "Filter movies releasing on or around the upcoming weekend (Fri-Sun).")
    private boolean filterWeekend;

    @Option(names = {"--from-date"}, paramLabel = "YYYY-MM-DD", description = "Filter movies releasing on or after this date (inclusive).")
    private LocalDate fromDate;

    @Option(names = {"--to-date"}, paramLabel = "YYYY-MM-DD", description = "Filter movies releasing on or before this date (inclusive).")
    private LocalDate toDate;

    @Option(names = {"--carousel"}, description = "Format markdown cards as an Antigravity carousel (cards format only).")
    private boolean carousel;

    @Option(names = {"-l", "--list-regions"}, description = "List common IMDB region codes and exit.")
    private boolean listRegions;

    @Option(names = {"--no-prompt"}, description = "Do not prompt interactively; use default values if flags are omitted.")
    private boolean noPrompt;

    @Option(names = {"--no-headless"}, description = "Run browser in non-headless mode (visible UI window).")
    private boolean noHeadless;

    @Option(names = {"--width"}, defaultValue = "70", description = "Terminal card width (default: 70).")
    private int terminalWidth;

    @Option(names = {"--dry-run"}, description = "Scrape and filter movies without writing to disk.")
    private boolean dryRun;

    @Override
    public Integer call() throws Exception {
        if (listRegions) {
            InteractivePrompt.displayRegions();
            return 0;
        }

        InteractivePrompt prompt = new InteractivePrompt(!noPrompt && System.console() != null);

        // 1. Resolve output format
        ExportFormat exportFormat;
        if (cardView) {
            exportFormat = ExportFormat.TERMINAL;
        } else if (formatStr != null && !formatStr.isBlank()) {
            exportFormat = ExportFormat.fromString(formatStr)
                .orElseThrow(() -> new IllegalArgumentException("Unknown export format: " + formatStr));
        } else if (noPrompt) {
            exportFormat = ExportFormat.ICS;
        } else {
            exportFormat = prompt.promptFormat(ExportFormat.ICS);
        }

        // 2. Resolve region
        String targetRegion;
        if (fromJsonPath != null) {
            targetRegion = null;
        } else if (region != null && !region.isBlank()) {
            targetRegion = region.trim().toUpperCase();
        } else if (noPrompt) {
            targetRegion = AppConfig.DEFAULT_REGION;
        } else {
            targetRegion = prompt.promptRegion(AppConfig.DEFAULT_REGION);
        }

        // 3. Resolve date filtering
        LocalDate effectiveFromDate = fromDate;
        LocalDate effectiveToDate = toDate;
        boolean effectiveWeekend = filterWeekend;

        if (filterToday) {
            effectiveFromDate = LocalDate.now();
            effectiveToDate = LocalDate.now();
        } else if (!filterWeekend && effectiveFromDate == null && effectiveToDate == null && !noPrompt) {
            var filterResult = prompt.promptDateFilter();
            effectiveFromDate = filterResult.startDate();
            effectiveToDate = filterResult.endDate();
            effectiveWeekend = filterResult.isWeekend();
        }

        // 4. Resolve carousel if cards format
        boolean effectiveCarousel = carousel;
        if (exportFormat == ExportFormat.CARDS && !carousel && !noPrompt) {
            effectiveCarousel = prompt.promptCardCarousel();
        }

        // 5. Load or scrape movies
        List<MovieCalendarEvent> movies;
        if (fromJsonPath != null) {
            if (!Files.exists(fromJsonPath)) {
                System.err.println("Error: Specified input JSON file does not exist: " + fromJsonPath);
                return 1;
            }
            System.out.println("Loading movies from " + fromJsonPath + "...");
            movies = JsonExporter.loadFromFile(fromJsonPath);
        } else {
            System.out.println("Scraping upcoming movies for region '" + targetRegion + "'...");
            AppConfig config = new AppConfig(
                targetRegion,
                exportFormat.getDefaultFilename(),
                calendarName,
                !noHeadless,
                AppConfig.DEFAULT_WINDOW_SIZE,
                AppConfig.DEFAULT_TIMEOUT
            );
            ImdbScraper scraper = new ImdbScraper(config);
            movies = scraper.scrapeUpcomingMovies(targetRegion);
        }

        if (movies == null || movies.isEmpty()) {
            System.out.println("No upcoming movies found.");
            return 0;
        }

        System.out.printf("Total movies collected: %d%n", movies.size());

        // 6. Apply date filters
        List<MovieCalendarEvent> filteredMovies = movies;
        if (effectiveWeekend) {
            filteredMovies = DateFilterService.filterWeekendMovies(filteredMovies);
            System.out.printf("Filtered to %d upcoming weekend movies.%n", filteredMovies.size());
        } else if (effectiveFromDate != null || effectiveToDate != null) {
            filteredMovies = DateFilterService.filterByDateRange(filteredMovies, effectiveFromDate, effectiveToDate);
            System.out.printf("Filtered to %d movies within date range.%n", filteredMovies.size());
        }

        if (dryRun) {
            System.out.printf("[DRY-RUN] Processed %d movies for format '%s'. Skipped disk write.%n",
                filteredMovies.size(), exportFormat.getShorthand());
            return 0;
        }

        // 7. Resolve output file path & export
        ExporterRegistry registry = new ExporterRegistry(calendarName, effectiveCarousel, terminalWidth);
        MovieExporter exporter = registry.getExporter(exportFormat);

        if (exportFormat == ExportFormat.TERMINAL && (outputFilename == null || outputFilename.isBlank())) {
            // Direct stdout print for terminal card view
            String rendered = exporter.renderString(filteredMovies);
            System.out.println();
            System.out.print(rendered);
            return 0;
        }

        Path targetOutputPath = ExporterRegistry.resolveOutputFilename(outputFilename, exportFormat);
        exporter.export(filteredMovies, targetOutputPath);
        System.out.printf("Successfully exported %d movies to %s (%s)%n",
            filteredMovies.size(), targetOutputPath.toAbsolutePath(), exportFormat.getDisplayName());

        return 0;
    }
}
