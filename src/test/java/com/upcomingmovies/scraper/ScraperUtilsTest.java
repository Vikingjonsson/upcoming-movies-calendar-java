package com.upcomingmovies.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScraperUtilsTest {

    @Nested
    class TestParseImdbReleaseDate {
        @Test
        void testStandardDate() {
            assertThat(ScraperUtils.parseImdbReleaseDate("Mar 29, 2026"))
                .isEqualTo(LocalDate.of(2026, 3, 29));
        }

        @Test
        void testJanuaryFirst() {
            assertThat(ScraperUtils.parseImdbReleaseDate("Jan 1, 2025"))
                .isEqualTo(LocalDate.of(2025, 1, 1));
        }

        @Test
        void testDecemberEnd() {
            assertThat(ScraperUtils.parseImdbReleaseDate("Dec 31, 2024"))
                .isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        void testInvalidFormatRaises() {
            assertThatThrownBy(() -> ScraperUtils.parseImdbReleaseDate("2026-03-29"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void testNonsenseRaises() {
            assertThatThrownBy(() -> ScraperUtils.parseImdbReleaseDate("not a date"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class TestParseFlexibleReleaseDate {
        @Test
        void testRfc2822Format() {
            assertThat(ScraperUtils.parseFlexibleReleaseDate("Fri, 02 Oct 2026 00:00:00 GMT"))
                .isEqualTo(LocalDate.of(2026, 10, 2));
        }

        @Test
        void testIsoFormat() {
            assertThat(ScraperUtils.parseFlexibleReleaseDate("2026-10-02"))
                .isEqualTo(LocalDate.of(2026, 10, 2));
        }

        @Test
        void testImdbTextualFormat() {
            assertThat(ScraperUtils.parseFlexibleReleaseDate("Oct 2, 2026"))
                .isEqualTo(LocalDate.of(2026, 10, 2));
        }

        @Test
        void testInvalidRaisesIllegalArgumentException() {
            assertThatThrownBy(() -> ScraperUtils.parseFlexibleReleaseDate("completely-unparseable"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
