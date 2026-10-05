# Upcoming Movies Calendar (Java 17 LTS)

Java 17 port of the [upcoming-movies-calendar](file:///Users/vikjon/git/upcoming-movies-calendar) CLI tool. It scrapes upcoming movie premieres from IMDB by region and exports them to **iCalendar (.ics)**, **JSON (.json)**, **Markdown Cards (.md)**, and **Terminal Card Views**.

---

## Key Features

- **Modern Java 17 LTS**: Implemented using Java 17 Records, Sealed Interfaces, Text Blocks, and modern pattern matching.
- **Selenium 4 with Selenium Manager**: Uses headless Chrome with eager loading, image suppression, and built-in driver management (no manual `chromedriver` download required).
- **Fast-Path Next.js Extraction**: Leverages IMDB's embedded `__NEXT_DATA__` JSON with parallel plot fetching inside the browser to minimize network round-trips.
- **Multi-Format Exporters**:
  - `ics`: RFC 5545 iCalendar (`.ics`) via Biweekly.
  - `json`: ISO-8601 formatted JSON via Jackson.
  - `cards`: Rich Markdown cards with poster previews and Antigravity carousel format (`--carousel`).
  - `terminal`: Visual ASCII cards directly in your terminal (`--card-view` or `-f terminal`).
- **Flexible Date Filtering**: Filter by `--today`, upcoming `--weekend` (Friday through Sunday), or custom date ranges (`--from-date`, `--to-date`).
- **Offline / Cached Mode**: Load and convert previously exported JSON files with `-i <path>`.

---

## Requirements

- **Java 17 LTS** (e.g. Zulu OpenJDK 17, Temurin 17)
- **Google Chrome** installed locally

---

## Quickstart

Use the included `./run.sh` launcher:

```bash
# Display help and usage
./run.sh --help

# List supported region codes
./run.sh -l

# Interactive mode (prompts for region, format, and date filters)
./run.sh

# Display upcoming weekend movies directly as visual cards in terminal
./run.sh -r SE --weekend --card-view

# Export upcoming Swedish premieres to iCalendar (.ics)
./run.sh -r SE -f ics -o sweden_movies.ics

# Export US releases to JSON
./run.sh -r US -f json -o us_movies.json

# Export to Markdown cards with Antigravity carousel block
./run.sh -r SE -f cards --carousel -o movies.md

# Convert cached JSON into terminal card view without scraping
./run.sh -i upcoming_movies.json --card-view
```

---

## CLI Options

| Flag | Description | Default |
| :--- | :--- | :--- |
| `-r`, `--region <CODE>` | IMDB region code (e.g., `SE`, `US`, `GB`, `DE`, `JP`) | `SE` |
| `-f`, `--format <FMT>` | Output format (`ics`, `json`, `cards`, `terminal`) | Prompts / `ics` |
| `-o`, `--output <PATH>` | Output file path | Default filename for format |
| `--card-view` | Print movies as visual cards in the terminal | Disabled |
| `--weekend` | Filter to upcoming weekend (Friday–Sunday) releases | Disabled |
| `--today` | Filter to movies releasing today | Disabled |
| `--from-date <YYYY-MM-DD>` | Filter releases on or after date | None |
| `--to-date <YYYY-MM-DD>` | Filter releases on or before date | None |
| `--carousel` | Format Markdown cards as an Antigravity carousel | Disabled |
| `-i`, `--from-json <PATH>` | Load movies from cached JSON instead of scraping | None |
| `-c`, `--calendar-name <NAME>` | Calendar name in iCalendar export | `Upcoming Movies` |
| `--width <WIDTH>` | Terminal card width | `70` |
| `--no-headless` | Launch browser with visible GUI window | Headless |
| `--dry-run` | Scrape and filter without writing files to disk | Disabled |
| `--no-prompt` | Disable interactive prompts; use flag defaults | Disabled |
| `-l`, `--list-regions` | List common IMDB region codes and exit | - |

---

## Development & Testing

```bash
# Run unit tests
./gradlew test

# Build application distribution
./gradlew installDist

# Run binary directly from distribution
./build/install/upcoming-movies/bin/upcoming-movies -l
```
