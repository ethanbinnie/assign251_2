
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
        long discardedBefore = appender.getDiscardedLogCount();
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
        assertEquals(discardedBefore + 1, appender.getDiscardedLogCount());
    }

    @Test
    void getEventStringsFormatsMessages() {
        appender.append(createEvent("Hello"));
        appender.append(createEvent("World"));

        List<String> strings = appender.getEventStrings();

        assertEquals(2, strings.size());
        assertEquals("Hello", strings.get(0));
        assertEquals("World", strings.get(1));
    }

    @Test
    void clearingLogsDoesNotIncreaseDiscardedCount() {
        appender.setMaxSize(2);
        long discardedBefore = appender.getDiscardedLogCount();
        appender.append(createEvent("First"));
        appender.append(createEvent("Second"));
        appender.append(createEvent("Third"));

        assertEquals(discardedBefore + 1, appender.getDiscardedLogCount());

        appender.clearLogs();

        assertTrue(appender.getCurrentLogs().isEmpty());
        assertEquals(discardedBefore + 1, appender.getDiscardedLogCount());
    }

    @Test
    void rejectsInvalidMaximumSize() {
        assertThrows(
                IllegalArgumentException.class,
                () -> appender.setMaxSize(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> appender.setMaxSize(-1)
        );
    }


    @Test
    void printLogsPrintsEventsAndClearsThem() {
        appender.append(createEvent("Hello"));
        appender.append(createEvent("World"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            appender.printLogs();

            assertEquals("HelloWorld", output.toString());
            assertTrue(appender.getCurrentLogs().isEmpty());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void getInstanceReturnsSameInstance() {
        List<LogEvent> freshEvents = new ArrayList<>();

        MemAppender.resetInstanceForTesting();

        try {
            MemAppender first = MemAppender.getInstance(
                    freshEvents,
                    PatternLayout.newBuilder().withPattern("%m").build()
            );

            MemAppender second = MemAppender.getInstance(
                    new ArrayList<>(),
                    PatternLayout.newBuilder().withPattern("%m").build()
            );

            assertSame(first, second);
        } finally {
            MemAppender.resetInstanceForTesting();
        }
    }

    @Test
    void storesEventsInInjectedList() {
        List<LogEvent> injectedEvents = new ArrayList<>();

        MemAppender.resetInstanceForTesting();

        try {
            MemAppender testAppender = MemAppender.getInstance(
                    injectedEvents,
                    PatternLayout.newBuilder().withPattern("%m").build()
            );

            testAppender.append(createEvent("Injected event"));

            assertEquals(1, injectedEvents.size());
            assertEquals(
                    "Injected event",
                    injectedEvents.get(0).getMessage().getFormattedMessage()
            );
        } finally {
            MemAppender.resetInstanceForTesting();
        }
    }

    @Test
    void returnedLogsCannotBeModified() {
        appender.append(createEvent("Protected event"));

        List<LogEvent> logs = appender.getCurrentLogs();

        assertThrows(
                UnsupportedOperationException.class,
                () -> logs.clear()
        );

        assertEquals(1, appender.getCurrentLogs().size());
    }

    private LogEvent createEvent(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .build();
    }
}
