package ru.dzhioev.petstore.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Getter
public class TestConfig {

    private static TestConfig instance;
    private final Properties properties;

    private String baseUrl;
    private String apiKey;
    private int timeout;
    private int maxRetryAttempts;
    private long retryDelay;

    private TestConfig() {
        properties = loadProperties();
        initConfig();
    }

    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                log.warn("Configuration file not found, using default values");
            }
        } catch (IOException e) {
            log.error("Error loading configuration", e);
        }
        return props;
    }

    private void initConfig() {
        this.baseUrl = properties.getProperty("base.url", "https://petstore.swagger.io/v2");
        this.apiKey = properties.getProperty("api.key", "special-key");
        this.timeout = Integer.parseInt(properties.getProperty("test.timeout", "30"));
        this.maxRetryAttempts = Integer.parseInt(properties.getProperty("retry.max.attempts", "3"));
        this.retryDelay = Long.parseLong(properties.getProperty("retry.delay.ms", "1000"));
    }
}