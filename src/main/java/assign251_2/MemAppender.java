
package assign251_2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.lang.management.ManagementFactory;
import javax.management.ObjectName;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public class MemAppender extends AbstractAppender {

    private static MemAppender instance;

    private final List<LogEvent> events;
    private long discardedLogCount;
    private int maxSize = 1000;

    private MemAppender(
            List<LogEvent> events,
            Layout<? extends Serializable> layout) {

        super("MemAppender", null, layout, true, Property.EMPTY_ARRAY);
        this.events = Objects.requireNonNull(events, "Events list cannot be null");
    }

    public static synchronized MemAppender getInstance(
            List<LogEvent> events,
            Layout<? extends Serializable> layout) {

        if (instance == null) {
            instance = new MemAppender(events, layout);
        }

        return instance;
    }

    static synchronized void resetInstanceForTesting() {
        instance = null;
    }

    @Override
    public synchronized void append(LogEvent event) {
        Objects.requireNonNull(event, "Log event cannot be null");

        if (maxSize <= 0) {
            discardedLogCount++;
            return;
        }

        if (events.size() >= maxSize) {
            events.remove(0);
            discardedLogCount++;
        }

        events.add(event.toImmutable());
    }

    public synchronized List<LogEvent> getCurrentLogs() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public synchronized List<String> getEventStrings() {
        if (getLayout() == null) {
            throw new IllegalStateException("A layout is required to format events");
        }

        List<String> strings = new ArrayList<>();

        for (LogEvent event : events) {
            strings.add(new String(getLayout().toByteArray(event)));
        }

        return Collections.unmodifiableList(strings);
    }

    public synchronized void printLogs() {
        for (String event : getEventStrings()) {
            System.out.print(event);
        }

        clearLogs();
    }

    public synchronized void clearLogs() {
        events.clear();
    }

    public synchronized void setMaxSize(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be at least 1");
        }

        this.maxSize = maxSize;

        while (events.size() > maxSize) {
            events.remove(0);
            discardedLogCount++;
        }
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized long getDiscardedLogCount() {
        return discardedLogCount;
    }

    public void registerMBean() {
        try {
            ObjectName name = new ObjectName(
                    "assign251_2:type=MemAppender"
            );

            var server = ManagementFactory.getPlatformMBeanServer();

            if (!server.isRegistered(name)) {
                server.registerMBean(
                        new MemAppenderMBeanImpl(this),
                        name
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to register MemAppender MBean", e
            );
        }
    }
}

