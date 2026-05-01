package S3.RNGs.triangle;

import S2.RNGs.intervals.TriangleInterval;

import java.util.Random;

public abstract class TriangleGenerator {
    private final Random valueGenerator;
    private final TriangleInterval interval;

    public TriangleGenerator(Random seedGenerator, TriangleInterval interval) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.interval = interval;
    }

    protected Random getValueGenerator() {
        return valueGenerator;
    }

    protected TriangleInterval getInterval() {
        return interval;
    }
}
