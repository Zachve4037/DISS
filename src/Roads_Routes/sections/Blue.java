package Roads_Routes.sections;

import RNGs.intervals.EmpiricInterval;

import java.util.List;

public class Blue {
    private static final List<EmpiricInterval> intervals = List.of(
            new EmpiricInterval(15, 29, 0.2),
            new EmpiricInterval(29, 45, 0.4),
            new EmpiricInterval(45, 65, 0.4)
    );

    public static List<EmpiricInterval> getIntervals() {
        return intervals;
    }
}
