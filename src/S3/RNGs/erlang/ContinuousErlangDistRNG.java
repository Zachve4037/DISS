package S3.RNGs.erlang;

import java.util.Random;

public class ContinuousErlangDistRNG extends ErlangGenerator {
    public ContinuousErlangDistRNG(Random seedGenerator, int k, double lambda) {
        super(seedGenerator, k, lambda);
    }

    public double generate() {
        double sum = 0.0;

        for (int i = 0; i < this.getK(); i++) {
            double u = 1.0 - this.getValueGenerator().nextDouble();
            sum += -Math.log(u);
        }

        return sum / this.getLambda();
    }
}