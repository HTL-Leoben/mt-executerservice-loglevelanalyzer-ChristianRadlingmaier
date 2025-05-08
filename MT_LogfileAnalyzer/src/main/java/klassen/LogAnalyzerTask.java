package klassen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;


// Callable-Klasse zur Analyse einer Logdatei
class LogAnalyzerTask implements Callable<Map<String, Object>> {
    private final Path filePath;
    private static final String[] LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};
    private static final String[] ERROR_KEYWORDS = {"NullPointerException", "FileNotFoundException", "SQLException"};

    public LogAnalyzerTask(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String, Integer> logLevelCounts = new HashMap<>();
        Map<String, Integer> errorTypes = new HashMap<>();
        List<String> errorWarnings = new ArrayList<>();

        for (String level : LOG_LEVELS) {
            logLevelCounts.put(level, 0);
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                for (String level : LOG_LEVELS) {
                    if (line.contains(level)) {
                        logLevelCounts.put(level, logLevelCounts.get(level) + 1);
                    }
                }

                if (line.contains("ERROR") || line.contains("WARN")) {
                    errorWarnings.add(line);
                }

                for (String keyword : ERROR_KEYWORDS) {
                    if (line.contains(keyword)) {
                        errorTypes.put(keyword, errorTypes.getOrDefault(keyword, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + filePath);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("logLevels", logLevelCounts);
        result.put("errors", errorWarnings);
        result.put("errorTypes", errorTypes);
        return result;
    }
}
