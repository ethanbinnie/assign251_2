
package assign251_2;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemAppenderIntegrationTest {

    @Test
    void receivesMessagesFromLog4jLogger() {
        MemAppender.resetInstanceForTesting();

        LoggerContext context = new LoggerContext("IntegrationTest");

        try {
            Configuration configuration = context.getConfiguration();
            List<LogEvent> events = new ArrayList<>();

            MemAppender appender = MemAppender.getInstance(
                    events,
                    PatternLayout.newBuilder()
                            .withPattern("%m")
                            .build()
            );

            appender.start();
            configuration.addAppender(appender);

            LoggerConfig loggerConfig = new LoggerConfig(
                    "IntegrationLogger",
                    Level.INFO,
                    false
            );
            loggerConfig.addAppender(appender, Level.INFO, null);
            configuration.addLogger("IntegrationLogger", loggerConfig);

            // Apply the updated configuration before logging.
            context.updateLoggers();

            context.getLogger("IntegrationLogger")
                    .info("Integration works");

            assertEquals(1, appender.getCurrentLogs().size());
            assertEquals(
                    "Integration works",
                    appender.getCurrentLogs().get(0)
                            .getMessage().getFormattedMessage()
            );
        } finally {
            context.close();
            MemAppender.resetInstanceForTesting();
        }
    }
}
