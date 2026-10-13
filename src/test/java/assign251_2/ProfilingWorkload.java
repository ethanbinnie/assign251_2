
package assign251_2;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

public class ProfilingWorkload {

    public static void main(String[] args) {
        MemAppender.resetInstanceForTesting();

        MemAppender appender = MemAppender.getInstance(
                new ArrayList<>(),
                PatternLayout.newBuilder()
                        .withPattern("%p %c %m%n")
                        .build()
        );

        appender.setMaxSize(1000);

        System.out.println("Profiling workload started.");
        System.out.println("Leave this running while using VisualVM.");

        long counter = 0;

        while (true) {
            LogEvent event = Log4jLogEvent.newBuilder()
                    .setLoggerName("ProfilingWorkload")
                    .setLevel(Level.INFO)
                    .setMessage(new SimpleMessage(
                            "Performance test message " + counter++))
                    .build();

            appender.append(event);

            // Periodically format stored events to exercise the layout.
            if (counter % 100 == 0) {
                appender.getEventStrings();
            }
        }
    }
}
