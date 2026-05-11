package S3.RNGs.empirical;

import S3.RNGs.empirical.EmpiricGenerator;
import S3.RNGs.intervals.EmpiricInterval;

import java.util.List;
import java.util.Random;

public class DiscreteEmpiricalDistRNG extends EmpiricGenerator {
    public DiscreteEmpiricalDistRNG(Random seedGenerator, List<EmpiricInterval> intervals) {
        super(seedGenerator, intervals);
    }

    public int generate() {
        double r = this.getIntervalGenerator().nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < this.getIntervals().size(); i++) {
            EmpiricInterval row = this.getIntervals().get(i);
            cumulative += row.p();

            if (r < cumulative) {
                return this.getValueGenerators()[i]
                        .nextInt(row.low(), row.high());
            }
        }
        EmpiricInterval last =
                this.getIntervals().get(this.getIntervals().size() - 1);
        return this.getValueGenerators()[this.getIntervals().size() - 1]
                .nextInt(last.low(), last.high());
    }
}
