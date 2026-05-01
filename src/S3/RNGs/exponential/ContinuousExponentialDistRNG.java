package S3.RNGs.exponential;

import java.util.Random;

public class ContinuousExponentialDistRNG extends ExponentialGenerator {
    public ContinuousExponentialDistRNG(Random seedGenerator, double lambda) {
        super(seedGenerator, lambda);
    }

    public double generate() {
        double u = this.getValueGenerator().nextDouble();
        return -Math.log(u) / this.getLambda();
    }
}
