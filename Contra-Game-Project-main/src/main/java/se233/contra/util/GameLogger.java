package se233.contra.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameLogger {
    private static final Logger logger = LoggerFactory.getLogger("ContraGame");

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void error(String message, Exception e) {
        logger.error(message, e);
    }
}