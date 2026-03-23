package S2.RNGs.exponential;

import java.util.Random;

public abstract class ExponentialGenerator {
    private final Random valueGenerator;
    private final int lambda;

    public ExponentialGenerator(Random seedGenerator, int lambda) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.lambda = lambda;
    }

    public Random getValueGenerator() {
        return valueGenerator;
    }

    public int getLambda() {
        return lambda;
    }
}
