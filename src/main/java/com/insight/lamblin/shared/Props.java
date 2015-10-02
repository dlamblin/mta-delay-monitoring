package com.insight.lamblin.shared;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Lets the command line utilities refer to the MtaApiKey stored in a
 * local.properties file.
 */
public class Props {
    public static final String MTA_API_KEY = "MtaApiKey";
    public static final String LOCAL_PROPERTIES = "local.properties";

    /**
     * Will read local.properties for the MtaApiKey and return it, or,
     * add an empty key if it's missing, and create the file if needed.
     *
     * @param CliName The user recognizable name of the program that called this.
     * @throws IOException If the file could not be opened, added to, nor created.
     */
    public static String getKey(final String CliName) throws IOException {
        // Defaults to key in LOCAL_PROPERTIES file if available
        Properties props = new Properties();
        Path path = Paths.get(LOCAL_PROPERTIES);
        if (Files.notExists(path)) {
            props.setProperty(MTA_API_KEY, "");
            try (BufferedWriter w = Files.newBufferedWriter(
                    path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                props.store(w, "Properties for mta-delay-notifications; by " + CliName);
            }
            return "";
        } else {
            props.load(Files.newInputStream(path));
            String key = props.getProperty(MTA_API_KEY);
            if (key == null) {
                props.setProperty(MTA_API_KEY, "");
                try (BufferedWriter w = Files.newBufferedWriter(
                        path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    props.store(w, "Added properties for mta-delay-notifications by " + CliName);
                }
                return "";
            } else {
                return key;
            }
        }

    }
}
