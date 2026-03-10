package Roads_Routes;

import java.util.List;

public record Route(
        String id,
        List<List<RoadSegment>> paths
) {}
