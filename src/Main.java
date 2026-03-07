import GUI.RouteGUI;

import javax.swing.*;
import java.util.Random;

public class Main {
    private static final Random seedRng = new Random();
    private static final Random distanceRng = new Random(seedRng.nextLong());
    private static final Random degreeRng = new Random(seedRng.nextLong());
    private static final int TRIALS = 1_000_000;
    private static final double NEEDLE_LENGTH = 1.0;
    private static final double LINE_SPACING = 2.0;

    public static void main(String[] args) {
//        buffonNeedle();
        SwingUtilities.invokeLater(() -> {
            RouteGUI frame = new RouteGUI();
            frame.setVisible(true);
        });
    }

    public static void buffonNeedle() {
        	int crosses = 0;

        	for (int i = 0; i < TRIALS; i++) {
        		double distanceFromLine = distanceRng.nextDouble() * (LINE_SPACING / 2);
        		double angle = degreeRng.nextDouble() * Math.PI / 2;

        		if (distanceFromLine < (NEEDLE_LENGTH / 2) * Math.sin(angle)) {
        			crosses++;
        		}
        	}

        	double piEstimate = (2.0 * NEEDLE_LENGTH * TRIALS) / (crosses * LINE_SPACING);
        	System.out.println("Estimated Pi: " + piEstimate);
    }
}