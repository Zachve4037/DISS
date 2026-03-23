package S1.RNGs.uniform;

import S1.RNGs.intervals.UniformInterval;

import java.util.Random;

public abstract class UniformGenerator {
    private final Random valueGenerator;
    private final UniformInterval interval;

    public UniformGenerator(Random seedGenerator, UniformInterval interval) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.interval = interval;
    }

    protected Random getValueGenerator() {
        return valueGenerator;
    }

    protected UniformInterval getInterval() {
        return interval;
    }
}
