package S2.SimCores;

import S2.Events.Airport.ArrivalEvent;
import S2.RNGs.empirical.DiscreteEmpiricalDistRNG;
import S2.RNGs.exponential.ContinuousExponentialDistRNG;
import S2.RNGs.intervals.EmpiricInterval;
import S2.RNGs.intervals.TriangleInterval;
import S2.RNGs.intervals.UniformInterval;
import S2.RNGs.triangle.ContinuousTriangularDistRNG;
import S2.RNGs.uniform.ContinuousUniformDistRNG;
import S2.Statistics.StatsForAirport;

import java.util.List;
import java.util.Random;

public class Airport extends EventSimCore {
    private final int TIME_LIMIT = 86_400; // 24 hodin v sekundach
    //generatory
    private final ContinuousExponentialDistRNG arrivalTimeRNG;
    private final ContinuousUniformDistRNG metalDetectorTimeRNG1;
    private final ContinuousUniformDistRNG metalDetectorTimeRNG2;
    private final ContinuousUniformDistRNG xrayTimeRNG1;
    private final ContinuousUniformDistRNG xrayTimeRNG2;
    private final ContinuousTriangularDistRNG securityCheckTimeRNG;
    private final DiscreteEmpiricalDistRNG numberOfCratesRNG;

    //statistiky
    private StatsForAirport stats = new StatsForAirport();

    public Airport(Random seed) {
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(seed, 1.0 / 60.0); // TODO: lambda
        this.metalDetectorTimeRNG1 = new ContinuousUniformDistRNG(seed, new UniformInterval(6, 27));
        this.metalDetectorTimeRNG2 = new ContinuousUniformDistRNG(seed, new UniformInterval(6, 27));
        this.xrayTimeRNG1 = new ContinuousUniformDistRNG(seed, new UniformInterval(9, 46));
        this.xrayTimeRNG2 = new ContinuousUniformDistRNG(seed, new UniformInterval(9, 46));
        this.securityCheckTimeRNG = new ContinuousTriangularDistRNG(seed, new TriangleInterval(10, 35, 120));
        this.numberOfCratesRNG = new DiscreteEmpiricalDistRNG(seed,
                List.of(
                        new EmpiricInterval(0, 1, 0.15),
                        new EmpiricInterval(1, 2, 0.68),
                        new EmpiricInterval(2, 3, 0.17)
                )
        );
    }

    @Override
    protected boolean shouldStop() {
        return this.currentTime >= TIME_LIMIT;
    }

    @Override
    protected void initialEvents() {
        this.scheduleEvent(new ArrivalEvent(this.currentTime, this));
    }

    public StatsForAirport getStats() {
        return stats;
    }
}
