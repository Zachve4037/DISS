package RNGs.tester;

import RNGs.empirical.ContinuousEmpiricalDistRNG;
import RNGs.empirical.DiscreteEmpiricalDistRNG;
import RNGs.uniform.ContinuousUniformDistRNG;
import RNGs.uniform.DiscreteUniformDistRNG;
import sections.intervals.EmpiricInterval;
import sections.intervals.UniformInterval;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Tester {

    private static final int SAMPLES    = 10_000_000;
    private static final int HIST_BINS  = 20;        // počet stĺpcov histogramu
    private static final Random SEED_RNG = new Random(0); // fixný seed → reprodukovateľné výsledky

    public static void main(String[] args) {

        System.out.println("GENERATOR TESTER  –  " + SAMPLES + " vzoriek na generátor");

        testDiscreteUniform();
        testContinuousUniform();
        testContinuousEmpirical();
        testDiscreteEmpirical();
    }

    private static void testDiscreteUniform() {
        System.out.println("ČERVENÝ úsek: Diskrétne rovnomerné <55;75)");

        DiscreteUniformDistRNG rng = new DiscreteUniformDistRNG(
                new Random(SEED_RNG.nextInt()),
                new UniformInterval(55, 75)
        );

        int[] counts = new int[76];
        long sum = 0;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            int v = rng.sample();
            counts[v]++;
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double mean = (double) sum / SAMPLES;
        double expectedMean = (55 + 75) / 2.0;

        System.out.printf("  Min: %d  Max: %d  Priemer: %.4f  (ocakavany: %.4f)%n",
                min, max, mean, expectedMean);
        System.out.println("  Hodnota\tPočet\t\tRelat. četnosť\tOčakávaná p");
        double expectedP = 1.0 / 21; // 21 hodnôt: 55..75 vrátane
        for (int v = 55; v <= 75; v++) {
            double relFreq = (double) counts[v] / SAMPLES;
            System.out.printf("  %d\t\t%d\t\t%.4f\t\t%.4f%n", v, counts[v], relFreq, expectedP);
        }
    }

    private static void testContinuousUniform() {
        System.out.println("ZELENÝ úsek: Spojité rovnomerné <50;80)");

        ContinuousUniformDistRNG rng = new ContinuousUniformDistRNG(
                new Random(SEED_RNG.nextInt()),
                new UniformInterval(50, 80)
        );

        double low = 50, high = 80, width = high - low;
        double[] bins = new double[HIST_BINS];
        double binSize = width / HIST_BINS;
        double sum = 0;
        double minV = Double.MAX_VALUE, maxV = Double.MIN_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            double v = rng.sample();
            sum += v;
            if (v < minV) minV = v;
            if (v > maxV) maxV = v;
            int bin = (int) ((v - low) / binSize);
            if (bin >= HIST_BINS) bin = HIST_BINS - 1;
            bins[bin]++;
        }

        double mean = sum / SAMPLES;
        double expectedMean = (low + high) / 2.0;

        System.out.printf("  Min: %.4f  Max: %.4f  Priemer: %.4f  (očakávaný: %.4f)%n",
                minV, maxV, mean, expectedMean);
        System.out.println("  Od\t\tDo\t\tRelat. četnosť\tOčakávaná p");
        double expectedBinP = 1.0 / HIST_BINS;
        for (int b = 0; b < HIST_BINS; b++) {
            double from = low + b * binSize;
            double to   = from + binSize;
            System.out.printf("  %.1f\t\t%.1f\t\t%.4f\t\t%.4f%n",
                    from, to, bins[b] / SAMPLES, expectedBinP);
        }
    }

    private static void testContinuousEmpirical() {
        System.out.println("ČIERNY úsek: Spojité empirické");

        List<EmpiricInterval> intervals = new LinkedList<>();
        intervals.add(new EmpiricInterval(10, 20, 0.10));
        intervals.add(new EmpiricInterval(20, 32, 0.50));
        intervals.add(new EmpiricInterval(32, 45, 0.20));
        intervals.add(new EmpiricInterval(45, 75, 0.15));
        intervals.add(new EmpiricInterval(75, 85, 0.05));

        ContinuousEmpiricalDistRNG rng = new ContinuousEmpiricalDistRNG(
                new Random(SEED_RNG.nextInt()), intervals
        );

        double[] boundaries = {10, 20, 32, 45, 75, 85};
        double[] expectedP   = {0.10, 0.50, 0.20, 0.15, 0.05};
        int[]    counts      = new int[5];
        double   sum = 0;
        double   minV = Double.MAX_VALUE, maxV = -Double.MAX_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            double v = rng.sample();
            sum += v;
            if (v < minV) minV = v;
            if (v > maxV) maxV = v;
            for (int b = 0; b < 5; b++) {
                if (v >= boundaries[b] && v < boundaries[b + 1]) {
                    counts[b]++;
                    break;
                }
            }
        }

        double mean = sum / SAMPLES;
        System.out.printf("  Min: %.4f  Max: %.4f  Priemer: %.4f%n", minV, maxV, mean);
        System.out.println("  Interval\t\tRelat. četnosť\tOčakávaná p");
        for (int b = 0; b < 5; b++) {
            System.out.printf("  <%.0f; %.0f)\t\t%.4f\t\t%.4f%n",
                    boundaries[b], boundaries[b + 1],
                    (double) counts[b] / SAMPLES, expectedP[b]);
        }
    }

    private static void testDiscreteEmpirical() {
        System.out.println("MODRÝ úsek: Diskrétne empirické");

        List<EmpiricInterval> intervals = new LinkedList<>();
        intervals.add(new EmpiricInterval(15, 29, 0.20));
        intervals.add(new EmpiricInterval(29, 45, 0.40));
        intervals.add(new EmpiricInterval(45, 65, 0.40));

        DiscreteEmpiricalDistRNG rng = new DiscreteEmpiricalDistRNG(
                new Random(SEED_RNG.nextInt()), intervals
        );

        int[] groupCounts = new int[3];
        double[] expectedP = {0.20, 0.40, 0.40};

        double sum = 0;
        int minV = Integer.MAX_VALUE, maxV = Integer.MIN_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            int v = rng.sample();
            sum += v;
            if (v < minV) minV = v;
            if (v > maxV) maxV = v;
            if (v >= 15 && v <= 29)      groupCounts[0]++;
            else if (v >= 29 && v <= 45) groupCounts[1]++;
            else if (v >= 45 && v <= 65) groupCounts[2]++;
        }

        double mean = sum / SAMPLES;
        System.out.printf("  Min: %d  Max: %d  Priemer: %.4f%n", minV, maxV, mean);

        System.out.println("  Skupinová kontrola:");
        System.out.println("  Interval\t\tRelat. četnosť\tOčakávaná p");
        String[] labels = {"<15;29)", "<29;45)", "<45;65)"};
        for (int b = 0; b < 3; b++) {
            System.out.printf("  %-12s\t\t%.4f\t\t%.4f%n",
                    labels[b], (double) groupCounts[b] / SAMPLES, expectedP[b]);
        }
    }
}
