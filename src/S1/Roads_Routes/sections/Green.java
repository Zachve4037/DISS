package S1.Roads_Routes.sections;

import S1.RNGs.intervals.UniformInterval;

import java.util.List;

public class Green {
    private static final List<UniformInterval> intervals = List.of(
            new UniformInterval(50, 80)
    );

    public static List<UniformInterval> getIntervals() {
        return intervals;
    }
}
