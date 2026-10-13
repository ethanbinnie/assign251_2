
package assign251_2;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

class LayoutPerformanceTest {

    private static final int WARMUP_ITERATIONS = 5000;
    private static final int TEST_ITERATIONS = 20000;
    private static final int ROUNDS = 5;

    @Test
    void comparePatternAndVelocityLayouts() {
        PatternLayout patternLayout = PatternLayout.newBuilder()
                .withPattern("%p %c %m%n")
                .build();

        VelocityLayout velocityLayout =
                new VelocityLayout("$p $c $m$n");

        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("LayoutPerformanceTest")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Performance test message"))
                .build();

        // Warm up both layouts before measuring.
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            patternLayout.toSerializable(event);
            velocityLayout.toSerializable(event);
        }

        long[] patternTimes = new long[ROUNDS];
        long[] velocityTimes = new long[ROUNDS];

        for (int round = 0; round < ROUNDS; round++) {
            if (round % 2 == 0) {
                patternTimes[round] =
                        measure(patternLayout, event);
                velocityTimes[round] =
                        measure(velocityLayout, event);
            } else {
                velocityTimes[round] =
                        measure(velocityLayout, event);
                patternTimes[round] =
                        measure(patternLayout, event);
            }
        }

        Arrays.sort(patternTimes);
        Arrays.sort(velocityTimes);

        double patternMedian =
                patternTimes[ROUNDS / 2] / 1_000_000.0;
        double velocityMedian =
                velocityTimes[ROUNDS / 2] / 1_000_000.0;

        System.out.println("\n--- Layout Performance Comparison ---");
        System.out.println("Messages per round: " + TEST_ITERATIONS);
        System.out.println("Rounds: " + ROUNDS);
        System.out.println("PatternLayout median: "
                + patternMedian + " ms");
        System.out.println("VelocityLayout median: "
                + velocityMedian + " ms");
        System.out.println("Velocity / Pattern ratio: "
                + velocityMedian / patternMedian);
    }

    private long measure(
            org.apache.logging.log4j.core.Layout<?> layout,
            LogEvent event) {

        long start = System.nanoTime();

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            layout.toSerializable(event);
        }

        return System.nanoTime() - start;
    }
}
