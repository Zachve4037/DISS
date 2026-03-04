package RNGs.empirical;

import sections.intervals.EmpiricInterval;

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
        return this.getValueGenerators()[this.getIntervals().size() - 1].nextInt(
          this.getIntervals().getLast().getLow(), this.getIntervals().getLast().getHigh()
        );
    }
}
