package RNGs.sections;

import RNGs.sections.intervals.EmpiricInterval;

import java.util.LinkedList;
import java.util.List;

public class Blue {
    private final List<EmpiricInterval> intervals = new LinkedList<>();

    public Blue() {
        this.intervals.add(new EmpiricInterval(15, 29, 0.2));
        this.intervals.add(new EmpiricInterval(29, 45, 0.4));
        this.intervals.add(new EmpiricInterval(45, 65, 0.4));
    }

    public List<EmpiricInterval> getIntervals() {
        return intervals;
    }
}
