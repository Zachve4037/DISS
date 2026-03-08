package RNGs.empirical;

import RNGs.intervals.EmpiricInterval;

import java.util.List;
import java.util.Random;

public class ContinuousEmpiricalDistRNG extends EmpiricGenerator {
    public ContinuousEmpiricalDistRNG(Random seedGenerator, List<EmpiricInterval> intervals) {
        super(seedGenerator, intervals);
    }

    public double generate() {
        double intervalProbability = this.getIntervalGenerator().nextDouble();
        for (int i = 0; i < this.getIntervals().size(); i++) {
            EmpiricInterval row = this.getIntervals().get(i);
            if (intervalProbability < row.getP()) {
                return this.getValueGenerators()[i].nextDouble(row.getLow(), row.getHigh());
            }
        }
        EmpiricInterval last = this.getIntervals().get(this.getIntervals().size() - 1);
        return this.getValueGenerators()[this.getIntervals().size() - 1].nextDouble(
                last.getLow(), last.getHigh()
        );
    }
}
