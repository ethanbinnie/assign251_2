
package assign251_2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemAppenderTest {

    private static List<LogEvent> events;
    private static MemAppender appender;

    @BeforeAll
    static void setUpOnce() {
        events = new ArrayList<>();

        appender = MemAppender.getInstance(
                events,
                PatternLayout.newBuilder()
                        .withPattern("%m")
                        .build()
        );
    }

    @BeforeEach
    void clearBeforeEachTest() {
        appender.clearLogs();
        appender.setMaxSize(1000);
    }

    @Test
    void storesLogEvents() {
        LogEvent event = createEvent("First message");

        appender.append(event);

        assertEquals(1, appender.getCurrentLogs().size());
        assertEquals(
                "First message",
                appender.getCurrentLogs().get(0)
                        .getMessage().getFormattedMessage()
        );
    }

    @Test
    void discardsOldestEventWhenMaximumSizeIsReached() {
        appender.setMaxSize(2);

        appender.append(createEvent("First"));
        appender.append(createEvent("Second"));
        appender.append(createEvent("Third"));

        List<LogEvent> logs = appender.getCurrentLogs();

        assertEquals(2, logs.size());
        assertEquals(
                "Second",
                logs.get(0).getMessage().getFormattedMessage()
        );
        assertEquals(
                "Third",
                logs.get(1).getMessage().getFormattedMessage()
        );
        assertEquals(1, appender.getDiscardedLogCount());
    }

    private LogEvent createEvent(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .build();
    }
}
