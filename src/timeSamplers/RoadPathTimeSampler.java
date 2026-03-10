package timeSamplers;

import RNGs.empirical.ContinuousEmpiricalDistRNG;
import RNGs.empirical.DiscreteEmpiricalDistRNG;
import RNGs.intervals.UniformInterval;
import Roads_Routes.RoadColor;
import Roads_Routes.RoadPath;
import Roads_Routes.RoadSegment;
import Roads_Routes.sections.Black;
import Roads_Routes.sections.Blue;
import Roads_Routes.sections.Green;
import Roads_Routes.sections.Red;
import RNGs.uniform.ContinuousUniformDistRNG;
import RNGs.uniform.DiscreteUniformDistRNG;
import timeTracker.TimeTracker;

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
