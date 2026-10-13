
package assign251_2;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemAppenderMBeanTest {

    @Test
    void registersMBeanAndExposesAppenderData() throws Exception {
        MemAppender.resetInstanceForTesting();

        ObjectName name = new ObjectName(
                "assign251_2:type=MemAppender"
        );

        MBeanServer server =
                ManagementFactory.getPlatformMBeanServer();

        try {
            if (server.isRegistered(name)) {
                server.unregisterMBean(name);
            }

            MemAppender appender = MemAppender.getInstance(
                    new ArrayList<>(),
                    PatternLayout.newBuilder()
                            .withPattern("%m%n")
                            .build()
            );

            appender.setMaxSize(2);
            appender.append(createEvent("Hello"));
            appender.append(createEvent("World"));
            appender.append(createEvent("Third"));

            appender.registerMBean();

            assertTrue(server.isRegistered(name));

            String[] messages = (String[]) server.getAttribute(
                    name, "LogMessages"
            );

            assertArrayEquals(
                    new String[]{"World", "Third"},
                    messages
            );

            assertEquals(
                    10L,
                    server.getAttribute(name, "EstimatedCachedCharacters")
            );

            assertEquals(
                    1L,
                    server.getAttribute(name, "DiscardedLogCount")
            );

        } finally {
            if (server.isRegistered(name)) {
                server.unregisterMBean(name);
            }

            MemAppender.resetInstanceForTesting();
        }
    }

    private LogEvent createEvent(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("MBeanTest")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .build();
    }
}
