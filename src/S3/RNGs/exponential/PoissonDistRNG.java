package S3.RNGs.exponential;

import java.util.Random;

public class PoissonDistRNG extends ExponentialGenerator {
    public PoissonDistRNG(Random seedGenerator, double lambda) {
        super(seedGenerator, lambda);
    }

    public int generate() {
        double L = Math.exp(-this.getLambda());
        int k = 0;
        double p = 1.0;
        do {
            k++;
            p *= this.getValueGenerator().nextDouble();
        } while (p > L);
        return k - 1;
    }
}
