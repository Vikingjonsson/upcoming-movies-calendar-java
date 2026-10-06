# Antigravity Workspace Guidelines: Upcoming Movies Calendar & Exporter (Java)

This repository provides a Java 17 CLI tool to scrape upcoming movie releases from IMDB by region and export them to iCalendar (`.ics`), JSON (`.json`), Markdown Cards (`.md`), and terminal visual card views.

## Architecture

- **`src/main/java/com/upcomingmovies/Main.java`**: Application entrypoint configuring Picocli command execution.
- **`src/main/java/com/upcomingmovies/cli/`**: CLI and prompt layer.
  - `MovieCliCommand.java`: Picocli CLI command definition, options parsing, and dispatching.
  - `InteractivePrompt.java`: JLine3 interactive console prompt for format, region, and date filtering.
- **`src/main/java/com/upcomingmovies/scraper/`**: Selenium 4 web scraping engine.
  - `ImdbScraper.java`: Core scraper extracting embedded Next.js `__NEXT_DATA__` JSON with parallel plot extraction.
  - `WebDriverFactory.java`: Headless Chrome driver configuration (eager page load, image suppression, automation evasion).
  - `ScraperUtils.java`: Date parsing routines, month maps, and release date formatters.
- **`src/main/java/com/upcomingmovies/exporter/`**: Multi-format exporters.
  - `MovieExporter.java`: Base sealed interface for format exporters.
  - `IcsExporter.java`: RFC 5545 iCalendar builder using Biweekly.
  - `JsonExporter.java`: JSON serializer and deserializer using Jackson.
  - `MarkdownCardExporter.java`: Markdown card renderer with Antigravity carousel support.
  - `TerminalCardExporter.java`: Terminal ASCII visual card renderer.
  - `ExporterRegistry.java`: Exporter format resolution and default file routing.
  - `ExportFormat.java`: Enum representing supported export formats (`ics`, `json`, `cards`, `terminal`).
- **`src/main/java/com/upcomingmovies/model/`**: Strongly typed domain models.
  - `MovieCalendarEvent.java`: Immutable record representing an upcoming movie event.
  - `ScheduledMovie.java`: Record representing raw schedule entries.
  - `DateFilterService.java`: Filtering routines for today, upcoming weekend, and custom date windows.
- **`src/main/java/com/upcomingmovies/config/`**: Configuration and regional data.
  - `AppConfig.java`: Configuration record with default timeouts, window dimensions, and region settings.
  - `Region.java`: Enum and lookup mapping for ISO country codes and IMDB regions.

## Developer Workflows

Use the Gradle wrapper (`./gradlew`) or the `./run.sh` launcher:

```bash
# Build & install binary distribution
./gradlew installDist

# Run CLI via launcher script
./run.sh --help
./run.sh -l                           # List common region codes
./run.sh                              # Interactive mode (prompts)
./run.sh -r SE -f json -o movies.json # Export Swedish releases to JSON
./run.sh -r SE --weekend --card-view  # Display weekend releases as visual terminal cards
./run.sh -r US -f ics                 # Export US releases to ICS
./run.sh -i movies.json --card-view   # Offline view from cached JSON

# Verification
./gradlew spotlessCheck               # Verify Java code formatting
./gradlew spotlessApply               # Automatically format code
./gradlew test                        # Run unit test suite
./gradlew check                       # Run full validation checks (spotless + tests)
```

## Agent Rules & Conventions

1. **Modern Java 17 LTS Idioms**:
   - Prefer Java 17 records for immutable data carriers and domain models (`MovieCalendarEvent`, `AppConfig`).
   - Use sealed interfaces (`MovieExporter`), pattern matching, switch expressions, and text blocks where applicable.
   - Do not introduce Lombok or heavy bytecode manipulation libraries.

2. **Clean CLI & Interactive UX**:
   - Use Picocli for command options and flags.
   - Use JLine3 for interactive terminal prompts when flags are omitted.
   - Support both interactive and non-interactive (`--no-prompt`) modes.

3. **Strict Type Safety & Compiler Cleanliness**:
   - Maintain 0 compiler warnings (`-Xlint:unchecked`, `-Xlint:deprecation`).
   - Use `java.util.Optional` for absent values rather than unchecked nulls.
   - Avoid mutable collections in public API boundaries (use `List.of()`, `List.copyOf()`).

4. **Scraping Performance**:
   - Batch DOM extractions in `ImdbScraper` by reading `__NEXT_DATA__` JSON or executing browser-side JavaScript to minimize synchronous Selenium IPC roundtrips.
   - Keep browser image loading disabled (`profile.managed_default_content_settings.images=2`) and set `page_load_strategy='eager'`.

5. **Verification Before Handoff**:
   - Always run `./gradlew test` and `./gradlew check` after making code changes.
