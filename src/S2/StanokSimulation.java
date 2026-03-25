package S2;

import S2.RNGs.intervals.TriangleInterval;
import S2.SimCores.Stanok;

import java.util.Random;

public class StanokSimulation {
    public static void main(String[] args) {
        Stanok sim = new Stanok(new Random(), 8, new TriangleInterval(5, 6, 7));
        sim.runSimulation(10000);
    }
}