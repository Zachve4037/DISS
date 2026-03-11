package MonteCarlo;

import timeSamplers.RouteTimeSampler;
import timeTracker.TimeTracker;

import java.util.Arrays;

public class RouteDeadlineAnalysisService {
    public static final int REPLICATIONS = 100_000;
    public static final int DEADLINE_SECONDS = 7 * 3600 + 35 * 60; // 07:35:00
    public static final double TARGET_PROBABILITY = 0.80;

    private final RouteTimeSampler routeTimeSampler = new RouteTimeSampler();

    public double estimateOnTimeProbability(String routeId, int startSecondsFromMidnight) {
        int success = 0;

        for (int i = 0; i < REPLICATIONS; i++) {
            int arrivalSec = sampleArrivalSeconds(routeId, startSecondsFromMidnight);
            if (arrivalSec <= DEADLINE_SECONDS) {
                success++;
            }
        }

        return success / (double) REPLICATIONS;
    }

    public int estimateArrivalPercentileSeconds(String routeId, int startSecondsFromMidnight, double percentile) {
        int[] arrivals = new int[REPLICATIONS];

        for (int i = 0; i < REPLICATIONS; i++) {
            arrivals[i] = sampleArrivalSeconds(routeId, startSecondsFromMidnight);
        }

        Arrays.sort(arrivals);
        int idx = (int) Math.floor(percentile * arrivals.length);
        if (idx < 0) idx = 0;
        if (idx >= arrivals.length) idx = arrivals.length - 1;

        return arrivals[idx];
    }

    public PercentileSearchResult findLatestStartByPercentile(String routeId, double percentile) {
        int low = 0;
        int high = DEADLINE_SECONDS;

        int pAtLow = estimateArrivalPercentileSeconds(routeId, low, percentile);
        if (pAtLow > DEADLINE_SECONDS) {
            return new PercentileSearchResult(false, 0, pAtLow);
        }

        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            int pArrival = estimateArrivalPercentileSeconds(routeId, mid, percentile);

            if (pArrival <= DEADLINE_SECONDS) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        int pAtBest = estimateArrivalPercentileSeconds(routeId, low, percentile);
        return new PercentileSearchResult(true, low, pAtBest);
    }

    private int sampleArrivalSeconds(String routeId, int startSecondsFromMidnight) {
        TimeTracker start = fromSeconds(startSecondsFromMidnight);
        double routeMinutes = routeTimeSampler.sampleRouteMinutes(start, routeId);

        int travelSec = (int) Math.round(routeMinutes * 60.0);
        return startSecondsFromMidnight + travelSec;
    }

    public static TimeTracker fromSeconds(int totalSeconds) {
        int clamped = Math.max(0, totalSeconds);
        int h = clamped / 3600;
        int m = (clamped % 3600) / 60;
        int s = clamped % 60;
        return new TimeTracker(h, m, s);
    }

    public static String formatHHmmss(int totalSeconds) {
        int clamped = Math.max(0, totalSeconds);
        int h = clamped / 3600;
        int m = (clamped % 3600) / 60;
        int s = clamped % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static int parseTimeToSeconds(String input) {
        String txt = input == null ? "" : input.trim();
        if (txt.isEmpty()) {
            throw new IllegalArgumentException("Time is empty.");
        }

        String[] parts = txt.split(":");
        if (parts.length != 2 && parts.length != 3) {
            throw new IllegalArgumentException("Use HH:mm or HH:mm:ss.");
        }

        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;

        if (h < 0 || h > 23 || m < 0 || m > 59 || s < 0 || s > 59) {
            throw new IllegalArgumentException("Invalid time range.");
        }

        return h * 3600 + m * 60 + s;
    }

    public static final class PercentileSearchResult {
        private final boolean exists;
        private final int latestStartSeconds;
        private final int percentileArrivalSeconds;

        public PercentileSearchResult(boolean exists, int latestStartSeconds, int percentileArrivalSeconds) {
            this.exists = exists;
            this.latestStartSeconds = latestStartSeconds;
            this.percentileArrivalSeconds = percentileArrivalSeconds;
        }

        public boolean exists() {
            return exists;
        }

        public int latestStartSeconds() {
            return latestStartSeconds;
        }

        public int percentileArrivalSeconds() {
            return percentileArrivalSeconds;
        }
    }
}
