package S1.Roads_Routes;

import java.util.List;

public class RoadPath {
    private final String name;
    private final List<RoadSegment> segments;

    public RoadPath(String name, List<RoadSegment> segments) {
        this.name = name;
        this.segments = segments;
    }

    public String getName() {
        return name;
    }

    public List<RoadSegment> getSegments() {
        return segments;
    }
}
