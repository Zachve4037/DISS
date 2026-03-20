package S1.timeSamplers;

import S1.Roads_Routes.RoadPath;
import S1.timeTracker.TimeTracker;

import java.util.List;

public class RoadTimeSampler {
    private final RoadPathTimeSampler roadPathTimeSampler;
    private static final String K = "k";
    private static final String SHARED = "shared";

    public RoadTimeSampler() {
        this.roadPathTimeSampler = new RoadPathTimeSampler();
    }

    public double sampleRoadMinutes(List<RoadPath> road, TimeTracker currentTime) {
        int startIndex = 0;
        double sharedTime = 0.0;
        if (SHARED.equals(road.get(0).getName())) {
            sharedTime = sampleRoadPathMinutes(road.get(0), currentTime.copy());
            startIndex = 1;
        }
        double minTime = Double.MAX_VALUE;
        for (int i = startIndex; i < road.size(); i++) {
            RoadPath roadPath = road.get(i);
            double totalTime;

            if (K.equals(roadPath.getName())) {
                totalTime = sampleRoadPathMinutes(roadPath, currentTime.copy());
            } else {
                TimeTracker afterShared = currentTime.copy();
                afterShared.addMinutes(sharedTime);

                double privateTime = sampleRoadPathMinutes(roadPath, afterShared);
                totalTime = privateTime + sharedTime;
            }
            if (totalTime < minTime) {
                minTime = totalTime;
            }
        }
        return minTime;
    }

    private double sampleRoadPathMinutes(RoadPath roadPath, TimeTracker currentTime) {
        return roadPathTimeSampler.sampleRoadPathMinutes(roadPath, currentTime);
    }
}
