package S3.RNGs.erlang;

import java.util.Random;

public abstract class ErlangGenerator {
    private final Random valueGenerator;
    private final int k;
    private final double lambda;

    public ErlangGenerator(Random seedGenerator, int k, double lambda) {
        this.valueGenerator = new Random(seedGenerator.nextInt());
        this.k = k;
        this.lambda = lambda;
    }

    public Random getValueGenerator() {
        return valueGenerator;
    }

    public int getK() {
        return k;
    }

    public double getLambda() {
        return lambda;
    }
}