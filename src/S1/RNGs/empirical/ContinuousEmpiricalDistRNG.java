package S1.RNGs.empirical;

import S1.RNGs.intervals.EmpiricInterval;

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
            if (intervalProbability < row.p()) {
                return this.getValueGenerators()[i].nextDouble(row.low(), row.high());
            }
        }
        EmpiricInterval last = this.getIntervals().get(this.getIntervals().size() - 1);
        return this.getValueGenerators()[this.getIntervals().size() - 1].nextDouble(
                last.low(), last.high()
        );
    }
}
