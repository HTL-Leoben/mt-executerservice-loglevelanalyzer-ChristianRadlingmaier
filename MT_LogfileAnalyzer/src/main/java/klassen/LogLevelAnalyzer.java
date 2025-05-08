package klassen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class LogLevelAnalyzer {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Path> logFiles = getLogFiles(".");
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (Path logFile : logFiles) {
            futures.add(executor.submit(new LogAnalyzerTask(logFile)));
        }

        Map<String, Integer> totalCounts = new HashMap<>();
        Map<String, Integer> totalErrorTypes = new HashMap<>();
        List<String> allErrors = new ArrayList<>();

        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get();
                Map<String, Integer> logCounts = (Map<String, Integer>) result.get("logLevels");
                List<String> errors = (List<String>) result.get("errors");
                Map<String, Integer> errorTypes = (Map<String, Integer>) result.get("errorTypes");

                logCounts.forEach((key, value) -> totalCounts.merge(key, value, Integer::sum));
                errorTypes.forEach((key, value) -> totalErrorTypes.merge(key, value, Integer::sum));
                allErrors.addAll(errors);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        System.out.println("Gesamt LogLevel-Zählung: " + totalCounts);
        System.out.println("Fehlertypen-Zählung: " + totalErrorTypes);
        System.out.println("WARN/ERROR Logs (Begrenzte Anzeige): ");
        allErrors.stream().limit(10).forEach(System.out::println);
    }

    private static List<Path> getLogFiles(String directory) {
        try {
            return Files.list(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

