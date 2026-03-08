package RNGs.sections;

import RNGs.sections.intervals.UniformInterval;

import java.util.LinkedList;
import java.util.List;

public class Red {
    private final List<UniformInterval> intervals = new LinkedList<>();

    public Red() {
        this.intervals.add(new UniformInterval(55, 75));
    }

    public List<UniformInterval> getIntervals() {
        return intervals;
    }
}
