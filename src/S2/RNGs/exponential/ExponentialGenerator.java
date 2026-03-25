package S2.RNGs.exponential;

import java.util.Random;

public abstract class ExponentialGenerator {
    private final Random valueGenerator;
    private final double lambda;

    public ExponentialGenerator(Random seedGenerator, double lambda) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.lambda = lambda;
    }

    public Random getValueGenerator() {
        return valueGenerator;
    }

    public double getLambda() {
        return lambda;
    }
}
