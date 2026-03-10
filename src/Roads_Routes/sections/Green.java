package Roads_Routes.sections;

import RNGs.intervals.UniformInterval;

import java.util.LinkedList;
import java.util.List;

public class Green {
    private static final List<UniformInterval> intervals = new LinkedList<>();

    public Green() {
        intervals.add(new UniformInterval(50, 80));
    }

    public static List<UniformInterval> getIntervals() {
        return intervals;
    }
}
