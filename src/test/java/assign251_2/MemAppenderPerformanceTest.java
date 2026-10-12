
package assign251_2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemAppenderPerformanceTest {

    private static final int MAX_SIZE = 1000;
    private static final int WARMUP_ITERATIONS = 20_000;
    private static final int TEST_ITERATIONS = 100_000;
    private static final int MEASURED_ROUNDS = 5;

    @Test
    void compareArrayListAndLinkedListPerformance() {
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("PerformanceTest")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Benchmark message"))
                .build();

        // Warm up both implementations.
        runBenchmark(ArrayList::new, event, WARMUP_ITERATIONS);
        runBenchmark(LinkedList::new, event, WARMUP_ITERATIONS);

        long[] arrayTimes = new long[MEASURED_ROUNDS];
        long[] linkedTimes = new long[MEASURED_ROUNDS];

        // Alternate the order to reduce order-related bias.
        for (int round = 0; round < MEASURED_ROUNDS; round++) {
            if (round % 2 == 0) {
                arrayTimes[round] =
                        runBenchmark(ArrayList::new, event, TEST_ITERATIONS);
                linkedTimes[round] =
                        runBenchmark(LinkedList::new, event, TEST_ITERATIONS);
            } else {
                linkedTimes[round] =
                        runBenchmark(LinkedList::new, event, TEST_ITERATIONS);
                arrayTimes[round] =
                        runBenchmark(ArrayList::new, event, TEST_ITERATIONS);
            }
        }

        Arrays.sort(arrayTimes);
        Arrays.sort(linkedTimes);

        long arrayMedian = arrayTimes[MEASURED_ROUNDS / 2];
        long linkedMedian = linkedTimes[MEASURED_ROUNDS / 2];

        System.out.println("\n--- MemAppender Performance ---");
        System.out.println("Iterations per round: " + TEST_ITERATIONS);
        System.out.println("Measured rounds: " + MEASURED_ROUNDS);
        System.out.println("Maximum stored logs: " + MAX_SIZE);
        System.out.println("ArrayList median:  "
                + arrayMedian / 1_000_000.0 + " ms");
        System.out.println("LinkedList median: "
                + linkedMedian / 1_000_000.0 + " ms");
    }

    private long runBenchmark(
            Supplier<List<LogEvent>> listFactory,
            LogEvent event,
            int iterations) {

        MemAppender.resetInstanceForTesting();

        try {
            List<LogEvent> events = listFactory.get();

            MemAppender appender = MemAppender.getInstance(
                    events,
                    PatternLayout.newBuilder()
                            .withPattern("%m%n")
                            .build()
            );

            appender.setMaxSize(MAX_SIZE);

            long start = System.nanoTime();

            for (int i = 0; i < iterations; i++) {
                appender.append(event);
            }

            long elapsed = System.nanoTime() - start;

            assertEquals(
                    Math.min(iterations, MAX_SIZE),
                    appender.getCurrentLogs().size()
            );

            assertEquals(
                    Math.max(0, iterations - MAX_SIZE),
                    appender.getDiscardedLogCount()
            );

            return elapsed;
        } finally {
            MemAppender.resetInstanceForTesting();
        }
    }
}
