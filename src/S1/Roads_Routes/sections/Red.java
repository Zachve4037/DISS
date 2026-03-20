package S1.Roads_Routes.sections;

import S1.RNGs.intervals.UniformInterval;

import java.util.List;

public class Red {
    private static final List<UniformInterval> intervals = List.of(
            new UniformInterval(55, 76)
    );

    public static List<UniformInterval> getIntervals() {
        return intervals;
    }
}
