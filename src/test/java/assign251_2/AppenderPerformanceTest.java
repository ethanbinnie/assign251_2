
package assign251_2;

import java.io.File;
import java.nio.file.Files;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppenderPerformanceTest {

    @Test
    void compareConsoleAndFileAppenders() throws Exception {
        File logFile = File.createTempFile("log-comparison-", ".log");
        LoggerContext context = new LoggerContext("AppenderComparison");

        ConsoleAppender consoleAppender = null;
        FileAppender fileAppender = null;

        try {
            Configuration configuration = context.getConfiguration();

            PatternLayout layout = PatternLayout.newBuilder()
                    .withPattern("%m%n")
                    .build();

            consoleAppender = ConsoleAppender.newBuilder()
                    .setName("ComparisonConsole")
                    .setLayout(layout)
                    .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                    .setConfiguration(configuration)
                    .build();

            fileAppender = FileAppender.newBuilder()
                    .setName("ComparisonFile")
                    .withFileName(logFile.getAbsolutePath())
                    .setLayout(layout)
                    .setConfiguration(configuration)
                    .build();

            consoleAppender.start();
            fileAppender.start();

            LogEvent[] events = new LogEvent[100];

            for (int i = 0; i < events.length; i++) {
                events[i] = Log4jLogEvent.newBuilder()
                        .setLoggerName("AppenderComparison")
                        .setLevel(Level.INFO)
                        .setMessage(new SimpleMessage(
                                "Performance test message " + i))
                        .build();
            }

            long startConsole = System.nanoTime();

            for (LogEvent event : events) {
                consoleAppender.append(event);
            }

            long consoleTime = System.nanoTime() - startConsole;

            long startFile = System.nanoTime();

            for (LogEvent event : events) {
                fileAppender.append(event);
            }

            long fileTime = System.nanoTime() - startFile;

            fileAppender.stop();

            assertTrue(
                    Files.size(logFile.toPath()) > 0,
                    "FileAppender should write log messages to the file"
            );

            System.out.println("\n--- Appender Comparison ---");
            System.out.println("Messages per appender: " + events.length);
            System.out.println("ConsoleAppender: "
                    + consoleTime / 1_000_000.0 + " ms");
            System.out.println("FileAppender: "
                    + fileTime / 1_000_000.0 + " ms");
            System.out.println("File output bytes: "
                    + Files.size(logFile.toPath()));

        } finally {
            if (consoleAppender != null) {
                consoleAppender.stop();
            }

            if (fileAppender != null) {
                fileAppender.stop();
            }

            context.close();
            Files.deleteIfExists(logFile.toPath());
        }
    }
}
