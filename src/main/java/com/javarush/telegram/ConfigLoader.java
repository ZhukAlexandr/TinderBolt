package com.javarush.telegram;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading config.properties", ex);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}