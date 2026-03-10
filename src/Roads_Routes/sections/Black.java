package Roads_Routes.sections;

import RNGs.intervals.EmpiricInterval;

import java.util.List;

public class Black {
    private static final List<EmpiricInterval> intervals = List.of(
            new EmpiricInterval(10, 20, 0.1),
            new EmpiricInterval(20, 32, 0.5),
            new EmpiricInterval(32, 45, 0.2),
            new EmpiricInterval(45, 75, 0.15),
            new EmpiricInterval(75, 85, 0.05)
    );

    public static List<EmpiricInterval> getIntervals() {
        return intervals;
    }
}
