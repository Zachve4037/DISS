package RNGs.uniform;

import sections.intervals.UniformInterval;

import java.util.Random;

public abstract class UniformGenerator {
    private final Random valueGenerator;
    private final UniformInterval interval;

    public UniformGenerator(Random seedGenerator, UniformInterval interval) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.interval = interval;
    }

    public Random getValueGenerator() {
        return valueGenerator;
    }

    public UniformInterval getInterval() {
        return interval;
    }
}
