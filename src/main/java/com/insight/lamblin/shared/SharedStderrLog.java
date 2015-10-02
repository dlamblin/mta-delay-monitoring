package com.insight.lamblin.shared;

/**
 * Shared methods to log to stderr from these cli Java apps.
 */
public class SharedStderrLog {
    /**
     * Outputs strings passed almost verbatim to stderr, changing newlines to spaces.
     *
     * @param s the string to output, try to prefix with "Info:", "Error:" etc.
     */
    public static void log(String s) {
        System.err.println(s.replace('\n', ' '));
    }

    /**
     * Exits with an error code of 1. Will log a string formatted with option parameter.
     *  @param format The format for the output string
     * @param option The string to make part of the format's "%s" portion
     */
    public static void die(String format, String option) {
        log(String.format(format, option));
        System.exit(1);
    }
}
