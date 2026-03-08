package Roads.sections;

import RNGs.intervals.UniformInterval;

import java.util.LinkedList;
import java.util.List;

public class Red {
    private static final List<UniformInterval> intervals = new LinkedList<>();

    public Red() {
        intervals.add(new UniformInterval(55, 75));
    }

    public static List<UniformInterval> getIntervals() {
        return intervals;
    }
}
