package com.upcomingmovies.scraper;

import com.upcomingmovies.config.AppConfig;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebDriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    public static final String DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    public static final Duration DEFAULT_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);

    private WebDriverFactory() {}

    public static ChromeOptions buildChromeOptions(boolean headless, String windowSize) {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=" + (windowSize != null ? windowSize : AppConfig.DEFAULT_WINDOW_SIZE));
        options.addArguments("--user-agent=" + DEFAULT_USER_AGENT);
        options.addArguments("--disable-blink-features=AutomationControlled");

        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        Map<String, Object> prefs = new HashMap<>();
        // Disable image loading for high speed
        prefs.put("profile.managed_default_content_settings.images", 2);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    public static WebDriver createDriver(boolean headless, String windowSize) {
        logger.debug("Creating ChromeDriver (headless: {}, windowSize: {})", headless, windowSize);
        ChromeOptions options = buildChromeOptions(headless, windowSize);
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(DEFAULT_PAGE_LOAD_TIMEOUT);
        return driver;
    }

    public static WebDriver createDriver(AppConfig config) {
        return createDriver(config.headless(), config.windowSize());
    }
}
