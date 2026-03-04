package RNGs.uniform;

import sections.intervals.UniformInterval;

import java.util.Random;

public class ContinuousUniformDistRNG extends UniformGenerator {
    public ContinuousUniformDistRNG(Random seedGenerator, UniformInterval interval) {
        super(seedGenerator, interval);
    }

    public double sample() {
        return this.getValueGenerator().nextDouble(
                this.getInterval().getLow(),
                this.getInterval().getHigh()
        );
    }
}
