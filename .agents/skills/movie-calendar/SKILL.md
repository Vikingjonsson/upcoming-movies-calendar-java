---
name: movie-calendar
description: >-
  Workflows for scraping upcoming movie releases from IMDB, running the interactive or flag-driven CLI, and exporting to ICS, JSON, Markdown Cards, or Terminal Cards.
---

# Movie Calendar Workflow Skill

Use this skill when developing, testing, scraping, or exporting upcoming movie calendar releases using the Java 17 toolchain.

## CLI Usage

```bash
# Interactive mode (prompts for region, format, date filter, style, and output file)
./run.sh

# Display movies as visual cards in terminal
./run.sh --card-view
./run.sh -f terminal --weekend
./run.sh -f terminal -r US

# Export Swedish releases to JSON
./run.sh -f json -r SE

# Export weekend releases as Markdown Cards
./run.sh -f cards -r SE --weekend

# Export as Antigravity carousel
./run.sh -f cards -r SE --weekend --carousel

# Date filtering flags
./run.sh -f json --today
./run.sh -f json --from-date 2026-05-01 --to-date 2026-05-31

# Export to ICS (United States)
./run.sh -f ics -r US -o us_movies.ics

# Offline mode from cached JSON
./run.sh -i upcoming_movies.json --card-view

# List supported regions
./run.sh -l
```

## Verification Workflows

```bash
./mvnw spotless:check                 # Verify Java code formatting
./mvnw spotless:apply                 # Automatically format code
./mvnw test                           # Run unit test suite
./mvnw verify                         # Run full validation checks (spotless + tests)
./mvnw package                        # Build executable fat JAR
```
