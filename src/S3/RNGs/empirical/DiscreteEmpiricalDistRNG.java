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

        // EmpiricGenerator already converts probabilities to cumulative probabilities.
        // Do not accumulate row.p() again here.
        for (int i = 0; i < this.getIntervals().size(); i++) {
            EmpiricInterval row = this.getIntervals().get(i);

            if (r < row.p()) {
                return this.getValueGenerators()[i]
                        .nextInt(row.low(), row.high());
            }
        }

        EmpiricInterval last = this.getIntervals().get(this.getIntervals().size() - 1);
        return this.getValueGenerators()[this.getIntervals().size() - 1]
                .nextInt(last.low(), last.high());
    }
}
