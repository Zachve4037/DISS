package S2;

import S2.GUI.GUI_Airport;
import S2.SimCores.Airport;

import javax.swing.*;
import java.util.Random;

public class AirportSimulation {
    private static volatile Airport airport;
    private static volatile Thread simThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI_Airport gui = new GUI_Airport();

            gui.getStartSimButton().addActionListener(e -> {
                if (simThread != null && simThread.isAlive()) {
                    return;
                }

                int cratesIn = gui.getCratesInCapacity();
                int cratesOut = gui.getCratesOutCapacity();

                airport = new Airport(new Random(), cratesIn, cratesOut);
                gui.bindAirport(airport);
                airport.setPacingSuppliers(gui::isMaxSpeed, gui::getSpeedFactor);
                airport.setOnReplicationFinished(gui::requestRefresh);

                gui.getStartSimButton().setEnabled(false);
                gui.getStopSimButton().setEnabled(true);

                simThread = new Thread(() -> {
                    try {
                        // 0 = bez limitu replikacii, bezi kym nedas Stop.
                        airport.runSimulation(10000);
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            gui.requestRefresh();
                            gui.getStartSimButton().setEnabled(true);
                            gui.getStopSimButton().setEnabled(false);
                        });
                    }
                }, "airport-sim-thread");

                simThread.setDaemon(true);
                simThread.start();
            });

            gui.getStopSimButton().addActionListener(e -> {
                Airport a = airport;
                if (a != null) {
                    a.setRun(false);
                }
            });
        });
    }
}
