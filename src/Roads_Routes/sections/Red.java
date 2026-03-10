package Roads_Routes.sections;

import RNGs.intervals.UniformInterval;

import java.util.List;

public class Red {
    private static final List<UniformInterval> intervals = List.of(
            new UniformInterval(55, 76)
    );

    public static List<UniformInterval> getIntervals() {
        return intervals;
    }
}
