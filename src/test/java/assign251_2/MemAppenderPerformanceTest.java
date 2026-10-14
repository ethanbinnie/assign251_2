package assign251_2;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemAppenderPerformanceTest {

    private static final int[] MAX_SIZES = {100, 1_000, 10_000};
    private static final int WARMUP_ITERATIONS = 20_000;
    private static final int TEST_ITERATIONS = 100_000;
    private static final int MEASURED_ROUNDS = 5;

    @Test
    void compareArrayListAndLinkedListPerformanceAndMemory() {
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("PerformanceTest")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Benchmark message"))
                .build();

        System.out.println("\n--- MemAppender Performance and Allocation ---");
        System.out.println("Iterations per round: " + TEST_ITERATIONS);
        System.out.println("Measured rounds: " + MEASURED_ROUNDS);

        for (int maxSize : MAX_SIZES) {
            runBenchmark(ArrayList::new, event, WARMUP_ITERATIONS, maxSize);
            runBenchmark(LinkedList::new, event, WARMUP_ITERATIONS, maxSize);

            long[] arrayTimes = new long[MEASURED_ROUNDS];
            long[] linkedTimes = new long[MEASURED_ROUNDS];
            long[] arrayAllocated = new long[MEASURED_ROUNDS];
            long[] linkedAllocated = new long[MEASURED_ROUNDS];

            for (int round = 0; round < MEASURED_ROUNDS; round++) {
                BenchmarkResult arrayResult;
                BenchmarkResult linkedResult;

                if (round % 2 == 0) {
                    arrayResult = runBenchmark(
                            ArrayList::new, event, TEST_ITERATIONS, maxSize);
                    linkedResult = runBenchmark(
                            LinkedList::new, event, TEST_ITERATIONS, maxSize);
                } else {
                    linkedResult = runBenchmark(
                            LinkedList::new, event, TEST_ITERATIONS, maxSize);
                    arrayResult = runBenchmark(
                            ArrayList::new, event, TEST_ITERATIONS, maxSize);
                }

                arrayTimes[round] = arrayResult.elapsedNanos;
                linkedTimes[round] = linkedResult.elapsedNanos;
                arrayAllocated[round] = arrayResult.allocatedBytes;
                linkedAllocated[round] = linkedResult.allocatedBytes;
            }

            Arrays.sort(arrayTimes);
            Arrays.sort(linkedTimes);
            Arrays.sort(arrayAllocated);
            Arrays.sort(linkedAllocated);

            int median = MEASURED_ROUNDS / 2;

            System.out.println("\nMaximum stored logs: " + maxSize);
            printResults("ArrayList", arrayTimes[median], arrayAllocated[median]);
            printResults("LinkedList", linkedTimes[median], linkedAllocated[median]);
        }
    }

    private BenchmarkResult runBenchmark(
            Supplier<List<LogEvent>> listFactory,
            LogEvent event,
            int iterations,
            int maxSize) {

        MemAppender.resetInstanceForTesting();

        try {
            MemAppender appender = MemAppender.getInstance(
                    listFactory.get(),
                    PatternLayout.newBuilder()
                            .withPattern("%m%n")
                            .build()
            );

            appender.setMaxSize(maxSize);

            long allocatedBefore = getCurrentThreadAllocatedBytes();
            long start = System.nanoTime();

            for (int i = 0; i < iterations; i++) {
                appender.append(event);
            }

            long elapsed = System.nanoTime() - start;
            long allocatedAfter = getCurrentThreadAllocatedBytes();

            assertEquals(
                    Math.min(iterations, maxSize),
                    appender.getCurrentLogs().size()
            );
            assertEquals(
                    Math.max(0, iterations - maxSize),
                    appender.getDiscardedLogCount()
            );

            long allocated = -1;
            if (allocatedBefore >= 0 && allocatedAfter >= allocatedBefore) {
                allocated = allocatedAfter - allocatedBefore;
            }

            return new BenchmarkResult(elapsed, allocated);
        } finally {
            MemAppender.resetInstanceForTesting();
        }
    }

    private void printResults(String name, long elapsedNanos, long allocatedBytes) {
        System.out.println(name + " median time: "
                + elapsedNanos / 1_000_000.0 + " ms");

        if (allocatedBytes >= 0) {
            System.out.println(name + " median allocated memory: "
                    + allocatedBytes / 1024.0 + " KiB");
        } else {
            System.out.println(name + " allocated memory: unsupported");
        }
    }

    private long getCurrentThreadAllocatedBytes() {
        java.lang.management.ThreadMXBean bean =
                ManagementFactory.getThreadMXBean();

        if (bean instanceof com.sun.management.ThreadMXBean) {
            com.sun.management.ThreadMXBean allocationBean =
                    (com.sun.management.ThreadMXBean) bean;

            if (allocationBean.isThreadAllocatedMemorySupported()) {
                if (!allocationBean.isThreadAllocatedMemoryEnabled()) {
                    allocationBean.setThreadAllocatedMemoryEnabled(true);
                }

                return allocationBean.getThreadAllocatedBytes(
                        Thread.currentThread().getId()
                );
            }
        }

        return -1;
    }

    private static class BenchmarkResult {
        private final long elapsedNanos;
        private final long allocatedBytes;

        private BenchmarkResult(long elapsedNanos, long allocatedBytes) {
            this.elapsedNanos = elapsedNanos;
            this.allocatedBytes = allocatedBytes;
        }
    }
}