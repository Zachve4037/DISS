package Roads_Routes.sections;

import RNGs.intervals.EmpiricInterval;

import java.util.LinkedList;
import java.util.List;

public class Blue {
    private static final List<EmpiricInterval> intervals = new LinkedList<>();

    public Blue() {
        intervals.add(new EmpiricInterval(15, 29, 0.2));
        intervals.add(new EmpiricInterval(29, 45, 0.4));
        intervals.add(new EmpiricInterval(45, 65, 0.4));
    }

    public static List<EmpiricInterval> getIntervals() {
        return intervals;
    }
}
