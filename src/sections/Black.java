package sections;

import sections.intervals.EmpiricInterval;

import java.util.LinkedList;
import java.util.List;

public class Black {
    private final List<EmpiricInterval> intervals = new LinkedList<>();

    public Black() {
        this.intervals.add(new EmpiricInterval(10, 20, 0.1));
        this.intervals.add(new EmpiricInterval(20, 32, 0.5));
        this.intervals.add(new EmpiricInterval(32, 45, 0.2));
        this.intervals.add(new EmpiricInterval(45, 75, 0.15));
        this.intervals.add(new EmpiricInterval(75, 85, 0.05));
    }

    public List<EmpiricInterval> getIntervals() {
        return intervals;
    }
}
