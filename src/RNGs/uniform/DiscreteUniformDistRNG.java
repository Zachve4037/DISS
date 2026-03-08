package RNGs.uniform;

import RNGs.sections.intervals.UniformInterval;

import java.util.Random;

public class DiscreteUniformDistRNG extends UniformGenerator {
    public DiscreteUniformDistRNG(Random seedGenerator, UniformInterval interval) {
        super(seedGenerator, interval);
    }

    public int generate() {
        return this.getValueGenerator().nextInt(
                this.getInterval().getLow(),
                this.getInterval().getHigh()
        );
    }
}
