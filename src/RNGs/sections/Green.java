package RNGs.sections;

import RNGs.sections.intervals.UniformInterval;

import java.util.LinkedList;
import java.util.List;

public class Green {
    private final List<UniformInterval> intervals = new LinkedList<>();

    public Green() {
        this.intervals.add(new UniformInterval(50, 80));
    }

    public List<UniformInterval> getIntervals() {
        return intervals;
    }
}
