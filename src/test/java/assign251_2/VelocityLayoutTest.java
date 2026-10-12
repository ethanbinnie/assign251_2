
package assign251_2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VelocityLayoutTest {

    @Test
    void formatsMessageAndLevel() {
        VelocityLayout layout =
                new VelocityLayout("[$p] $m");

        LogEvent event = createEvent("Hello world");

        String result = layout.toSerializable(event);

        assertEquals("[INFO] Hello world", result);
    }

    @Test
    void formatsLoggerName() {
        VelocityLayout layout =
                new VelocityLayout("$c");

        LogEvent event = createEvent("Hello");

        assertEquals("TestLogger", layout.toSerializable(event));
    }

    @Test
    void formatsThreadName() {
        VelocityLayout layout =
                new VelocityLayout("$t");

        LogEvent event = createEvent("Hello");

        assertEquals(
                Thread.currentThread().getName(),
                layout.toSerializable(event)
        );
    }

    @Test
    void formatsLineSeparator() {
        VelocityLayout layout =
                new VelocityLayout("Hello$n");

        LogEvent event = createEvent("Test");

        assertEquals(
                "Hello" + System.lineSeparator(),
                layout.toSerializable(event)
        );
    }

    @Test
    void rejectsNullTemplate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VelocityLayout(null)
        );
    }

    @Test
    void formatsDate() {
        VelocityLayout layout = new VelocityLayout("$d");

        long timestamp = 1_700_000_000_000L;

        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Hello"))
                .setTimeMillis(timestamp)
                .build();

        assertEquals(
                new Date(timestamp).toString(),
                layout.toSerializable(event)
        );
    }

    private LogEvent createEvent(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .setThreadName(Thread.currentThread().getName())
                .build();
    }
}
