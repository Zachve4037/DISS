package S1.timeSamplers;

import S1.RNGs.empirical.ContinuousEmpiricalDistRNG;
import S1.RNGs.empirical.DiscreteEmpiricalDistRNG;
import S1.RNGs.intervals.UniformInterval;
import S1.Roads_Routes.RoadColor;
import S1.Roads_Routes.RoadPath;
import S1.Roads_Routes.RoadSegment;
import S1.Roads_Routes.sections.Black;
import S1.Roads_Routes.sections.Blue;
import S1.Roads_Routes.sections.Green;
import S1.Roads_Routes.sections.Red;
import S1.RNGs.uniform.ContinuousUniformDistRNG;
import S1.RNGs.uniform.DiscreteUniformDistRNG;
import S1.timeTracker.TimeTracker;

import java.util.Random;

public class RoadPathTimeSampler {
    private final Random SEED_GENERATOR = new Random();
    private final DiscreteUniformDistRNG red = new DiscreteUniformDistRNG(SEED_GENERATOR, Red.getIntervals().get(0));
    private final ContinuousUniformDistRNG green = new ContinuousUniformDistRNG(SEED_GENERATOR, Green.getIntervals().get(0));
    private final ContinuousEmpiricalDistRNG black = new ContinuousEmpiricalDistRNG(SEED_GENERATOR, Black.getIntervals());
    private final DiscreteEmpiricalDistRNG blue = new DiscreteEmpiricalDistRNG(SEED_GENERATOR, Blue.getIntervals());
    private final ContinuousUniformDistRNG k = new ContinuousUniformDistRNG(SEED_GENERATOR, new UniformInterval(10, 25));

    public RoadPathTimeSampler() {
    }

    public double sampleRoadPathMinutes(RoadPath roadPath, TimeTracker timeTracker) {
        TimeTracker current = timeTracker.copy();
        double totalTime = 0;
        for (RoadSegment segment : roadPath.getSegments()) {
            double speed = sampleSpeed(segment.getColor());
            if (segment.isK() && current.isAfter(6, 30, 0)) {
                double kNum = k.generate();
                speed = speed * ((100 - kNum) / 100);
            }
            double totalSegmentMinutes = (segment.getLength() / speed) * 60;
            totalTime += totalSegmentMinutes;
            current.addMinutes(totalSegmentMinutes);
        }
        return totalTime;
    }

    private double sampleSpeed(RoadColor color) {
        return switch (color) {
            case RED -> red.generate();
            case GREEN -> green.generate();
            case BLACK -> black.generate();
            case BLUE -> blue.generate();
        };
    }
}
