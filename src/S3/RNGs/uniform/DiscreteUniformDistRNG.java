package S3.RNGs.uniform;

import S3.RNGs.intervals.UniformInterval;

import java.util.Random;

public class DiscreteUniformDistRNG extends UniformGenerator {
    public DiscreteUniformDistRNG(Random seedGenerator, UniformInterval interval) {
        super(seedGenerator, interval);
    }

    public int generate() {
        return this.getValueGenerator().nextInt(
                this.getInterval().low(),
                this.getInterval().high()
        );
    }
}
