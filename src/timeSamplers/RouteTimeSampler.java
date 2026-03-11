package timeSamplers;

import Roads_Routes.RoadPath;
import Roads_Routes.Routes;
import timeTracker.TimeTracker;

import java.util.List;

public class RouteTimeSampler {
    private final RoadTimeSampler roadTimeSampler;

    public RouteTimeSampler() {
        this.roadTimeSampler = new RoadTimeSampler();
    }

    public double sampleRouteMinutes(TimeTracker currentTime, String route) {
        List<List<RoadPath>> paths = switch (route) {
            case "Z_S_RT_D_Z" -> Routes.Z_S_RT_D_Z();
            case "Z_S_D_RT_Z" -> Routes.Z_S_D_RT_Z();
            case "Z_RT_S_D_Z" -> Routes.Z_RT_S_D_Z();
            case "Z_RT_D_S_Z" -> Routes.Z_RT_D_S_Z();
            case "Z_D_S_RT_Z" -> Routes.Z_D_S_RT_Z();
            case "Z_D_RT_S_Z" -> Routes.Z_D_RT_S_Z();
            default -> throw new IllegalArgumentException("Invalid route: " + route);
        };

        double totalMinutes = 0.0;
        TimeTracker now = currentTime.copy();

        for (List<RoadPath> path : paths) {
            double pathMinutes = roadTimeSampler.sampleRoadMinutes(path, now);
            totalMinutes += pathMinutes;
            now.addMinutes(pathMinutes);
        }

        return totalMinutes;
    }
}
