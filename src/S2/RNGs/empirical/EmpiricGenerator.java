package S2.RNGs.empirical;

import S1.RNGs.intervals.EmpiricInterval;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class EmpiricGenerator {
    private final Random intervalGenerator;
    private final Random[] valueGenerators;
    private final List<EmpiricInterval> intervals;

    public EmpiricGenerator(Random seedGenerator, List<EmpiricInterval> intervals) {
        this.valueGenerators = new Random[intervals.size()];
        this.intervalGenerator = new Random(seedGenerator.nextInt());
        for (int i = 0; i < intervals.size(); i++) {
            this.valueGenerators[i] = new Random(seedGenerator.nextInt());
        }
        this.intervals = new LinkedList<>();
        double cumulativeP = 0;
        for (int i = 0; i < intervals.size(); i++) {
            EmpiricInterval interval = intervals.get(i);
            cumulativeP += interval.getP();
            this.intervals.add(new EmpiricInterval(interval.getLow(), interval.getHigh(), cumulativeP));
        }
        if (Math.abs(cumulativeP - 1.0) > 1e-9) {
            throw new IllegalArgumentException("The sum of probabilities must be 1");
        }
    }

    protected Random getIntervalGenerator() {
        return this.intervalGenerator;
    }

    protected Random[] getValueGenerators() {
        return this.valueGenerators;
    }

    protected List<EmpiricInterval> getIntervals() {
        return this.intervals;
    }
}
