package S2.RNGs;

import S2.RNGs.exponential.ContinuousExponentialDistRNG;
import S2.RNGs.intervals.TriangleInterval;
import S2.RNGs.intervals.UniformInterval;
import S2.RNGs.triangle.ContinuousTriangularDistRNG;
import S2.RNGs.uniform.ContinuousUniformDistRNG;

import java.util.Locale;
import java.util.Random;

public class Tester {
    private static final int SAMPLES = 10_000;
    private static final Random SEED_RNG = new Random(123456);

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.println("S2 RNG TESTER - " + SAMPLES + " samples per generator");
        System.out.println();

        testContinuousUniform();
//        System.out.println();
//        testContinuousTriangular();
//        System.out.println();
//        testContinuousExponential();
    }

    private static void testContinuousUniform() {
        System.out.println("=== CONTINUOUS UNIFORM ===");
        System.out.println("Distribution: U<50,80)");

        ContinuousUniformDistRNG rng = new ContinuousUniformDistRNG(
                new Random(SEED_RNG.nextInt()),
                new UniformInterval(50, 80)
        );

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;

        System.out.println("value");
        for (int i = 0; i < SAMPLES; i++) {
            double v = rng.generate();
            System.out.println(v);

            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double mean = sum / SAMPLES;
        System.out.printf("STATS;min=%.6f;max=%.6f;mean=%.6f%n", min, max, mean);
    }

    private static void testContinuousTriangular() {
        System.out.println("=== CONTINUOUS TRIANGULAR ===");
        System.out.println("Distribution: TRI<10,50,80>");

        ContinuousTriangularDistRNG rng = new ContinuousTriangularDistRNG(
                new Random(SEED_RNG.nextInt()),
                new TriangleInterval(10, 50, 80)
        );

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;

        System.out.println("value");
        for (int i = 0; i < SAMPLES; i++) {
            double v = rng.generate();
            System.out.println(v);

            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double mean = sum / SAMPLES;
        System.out.printf("STATS;min=%.6f;max=%.6f;mean=%.6f%n", min, max, mean);
    }

    private static void testContinuousExponential() {
        System.out.println("=== CONTINUOUS EXPONENTIAL ===");
        System.out.println("Distribution: EXP(lambda=3)");

        ContinuousExponentialDistRNG rng = new ContinuousExponentialDistRNG(
                new Random(SEED_RNG.nextInt()),
                3
        );

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;

        System.out.println("value");
        for (int i = 0; i < SAMPLES; i++) {
            double v = rng.generate();
            System.out.println(v);

            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double mean = sum / SAMPLES;
        System.out.printf("STATS;min=%.6f;max=%.6f;mean=%.6f%n", min, max, mean);
    }
}
