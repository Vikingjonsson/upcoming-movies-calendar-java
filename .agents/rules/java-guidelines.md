---
description: Java 17 standards, records, strict typing, and scraping performance guidelines.
globs: "**/*.java"
---

# Upcoming Movies Java Guidelines

- **Modern Java 17 LTS Idioms**: Use Java 17 features such as records for immutable data transfer, sealed types, pattern matching, and text blocks. Avoid Lombok or unnecessary boilerplate.
- **CLI Conventions**: Use Picocli annotations for options/arguments and JLine3 for interactive console prompts. Follow the `./run.sh` executable wrapper pattern.
- **Type Safety & Null Handling**: Use `java.util.Optional` for optional fields/parameters and avoid nullable collections (prefer `List.of()` or empty immutable collections). Ensure strict compiler cleanliness (`-Xlint:unchecked`, `-Xlint:deprecation`).
- **DOM Extraction Performance**: Leverage IMDB Next.js JSON data (`__NEXT_DATA__`) and batch JavaScript execution in Chrome to minimize Selenium IPC overhead. Keep browser image loading disabled (`profile.managed_default_content_settings.images=2`) and set `page_load_strategy='eager'`.
- **Verification Before Handoff**: Always verify with `./mvnw test` and `./mvnw verify` before completing tasks.
