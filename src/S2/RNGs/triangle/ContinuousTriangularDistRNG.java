package S2.RNGs.triangle;

import S2.RNGs.intervals.TriangleInterval;

import java.util.Random;

public class ContinuousTriangularDistRNG extends TriangleGenerator {
    public ContinuousTriangularDistRNG(Random seedGenerator, TriangleInterval interval) {
        super(seedGenerator, interval);
    }

    public double generate() {
        double a = this.getInterval().getLow();
        double b = this.getInterval().getHigh();
        double c = this.getInterval().getMid();

        double F = (c - a) / (b - a);
        double rand = this.getValueGenerator().nextDouble();
        if (rand < F) {
            return a + Math.sqrt(rand * (b - a) * (c - a));
        } else {
            return b - Math.sqrt((1 - rand) * (b - a) * (b - c));
        }
    }
}
