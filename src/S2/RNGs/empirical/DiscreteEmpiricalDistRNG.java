package S2.RNGs.empirical;

import S2.RNGs.intervals.EmpiricInterval;

import java.util.List;
import java.util.Random;

public class DiscreteEmpiricalDistRNG extends EmpiricGenerator {
    public DiscreteEmpiricalDistRNG(Random seedGenerator, List<EmpiricInterval> intervals) {
        super(seedGenerator, intervals);
    }

    public int generate() {
        double intervalProbability = this.getIntervalGenerator().nextDouble();
        for (int i = 0; i < this.getIntervals().size(); i++) {
            EmpiricInterval row = this.getIntervals().get(i);
            if (intervalProbability < row.getP()) {
                return this.getValueGenerators()[i].nextInt(row.getLow(), row.getHigh());
            }
        }
        EmpiricInterval last = this.getIntervals().get(this.getIntervals().size() - 1);
        return this.getValueGenerators()[this.getIntervals().size() - 1].nextInt(
                last.getLow(), last.getHigh()
        );
    }
}
