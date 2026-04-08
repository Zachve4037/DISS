package S2;

import S2.GUI.GUI_Airport;
import S2.SimCores.Airport;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class AirportSimulation {
    private static volatile Airport airport;
    private static volatile Thread workerThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI_Airport gui = new GUI_Airport();

            gui.getStartSimButton().addActionListener(e -> {
                if (isWorkerRunning()) {
                    JOptionPane.showMessageDialog(null, "Simulation is already running.");
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
                gui.setSensitivityControlsEnabled(false);

                workerThread = new Thread(() -> {
                    try {
                        airport.runSimulation(10000);
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            gui.requestRefresh();
                            gui.getStartSimButton().setEnabled(true);
                            gui.getStopSimButton().setEnabled(false);
                            gui.setSensitivityControlsEnabled(true);
                        });
                        workerThread = null;
                    }
                }, "airport-sim-thread");

                workerThread.setDaemon(true);
                workerThread.start();
            });

            gui.getStopSimButton().addActionListener(e -> {
                Airport a = airport;
                if (a != null) {
                    a.setRun(false);
                }
            });

            gui.getSensitivityRunButton().addActionListener(e -> {
                if (isWorkerRunning()) {
                    JOptionPane.showMessageDialog(null, "Another simulation is already running.");
                    return;
                }

                final int baseCapacity;
                final int classes;
                final int reps;
                try {
                    baseCapacity = gui.getSensitivityBaseCapacity();
                    classes = gui.getSensitivityClasses();
                    reps = gui.getSensitivityReplications();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Capacity, classes and replications must be integer numbers.");
                    return;
                }

                if (baseCapacity <= 0) {
                    JOptionPane.showMessageDialog(null, "Capacity/day must be > 0.");
                    return;
                }
                if (classes < 2) {
                    JOptionPane.showMessageDialog(null, "Classes must be >= 2.");
                    return;
                }
                if (reps <= 0) {
                    JOptionPane.showMessageDialog(null, "Replications per point must be > 0.");
                    return;
                }

                final int cratesIn = gui.getCratesInCapacity();
                final int cratesOut = gui.getCratesOutCapacity();

                final List<Integer> capacities;
                try {
                    capacities = buildCapacities(baseCapacity, classes);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    return;
                }

                gui.getStartSimButton().setEnabled(false);
                gui.getStopSimButton().setEnabled(false);
                gui.setSensitivityControlsEnabled(false);
                gui.setSensitivityProgress("Progress: starting...");

                workerThread = new Thread(() -> {
                    ExecutorService pool = null;
                    try {
                        final ConcurrentHashMap<Integer, Double> liveAvgByCapacity = new ConcurrentHashMap<>();
                        final ConcurrentHashMap<Integer, Boolean> kpiByCapacity = new ConcurrentHashMap<>();

                        int parallelism = Math.min(
                                capacities.size(),
                                Math.max(1, Runtime.getRuntime().availableProcessors() - 1)
                        );

                        pool = Executors.newFixedThreadPool(parallelism);
                        CompletionService<SensitivityPointResult> completion = new ExecutorCompletionService<>(pool);

                        final int totalPoints = capacities.size();

                        Random seed = new Random();
                        int pointIndex = 0;
                        for (int cap : capacities) {
                            pointIndex++;
                            final int currentPoint = pointIndex;
                            final long pointSeed = seed.nextLong();

                            completion.submit(() -> {
                                double lambda = Airport.capacityPerDayToLambda(cap);
                                Airport local = new Airport(new Random(pointSeed), cratesIn, cratesOut, lambda);

                                local.setPacingSuppliers(() -> true, () -> 200);

                                local.setOnReplicationFinished(() -> {
                                    long doneRep = local.getCompletedReplications();
                                    double partialAvg = local.getStats().getSimAvgCustomerSystemTime().getValue();

                                    if (!Double.isNaN(partialAvg) && !Double.isInfinite(partialAvg)) {
                                        liveAvgByCapacity.put(cap, partialAvg);
                                    }

                                    Integer currentBest = computeBestCapacity(liveAvgByCapacity, kpiByCapacity);

                                    Map<Integer, Double> snapshot = new LinkedHashMap<>(new TreeMap<>(liveAvgByCapacity));
                                    SwingUtilities.invokeLater(() -> {
                                        gui.setSensitivityProgress(
                                                "Progress: point " + currentPoint + "/" + totalPoints +
                                                        " (cap=" + cap + "), rep " + doneRep + "/" + reps
                                        );
                                        gui.updateSensitivityHistogram(snapshot, currentBest);
                                    });
                                });

                                local.runSimulation(reps);

                                double finalAvg = local.getStats().getSimAvgCustomerSystemTime().getValue();
                                boolean finalKpi = allKpiSatisfied(local.getStats());

                                return new SensitivityPointResult(cap, finalAvg, finalKpi);
                            });
                        }

                        for (int done = 1; done <= capacities.size(); done++) {
                            Future<SensitivityPointResult> f = completion.take();
                            SensitivityPointResult r = f.get();

                            liveAvgByCapacity.put(r.capacity(), r.avgSecurityTime());
                            kpiByCapacity.put(r.capacity(), r.kpiSatisfied());

                            Integer best = computeBestCapacity(liveAvgByCapacity, kpiByCapacity);
                            Map<Integer, Double> snapshot = new LinkedHashMap<>(new TreeMap<>(liveAvgByCapacity));

                            int finished = done;
                            SwingUtilities.invokeLater(() -> {
                                gui.setSensitivityProgress("Progress: finished points " + finished + "/" + capacities.size());
                                gui.updateSensitivityHistogram(snapshot, best);
                            });
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(null, "Sensitivity computation failed: " + ex.getCause())
                        );
                    } finally {
                        if (pool != null) {
                            pool.shutdownNow();
                        }
                        SwingUtilities.invokeLater(() -> {
                            gui.getStartSimButton().setEnabled(true);
                            gui.getStopSimButton().setEnabled(false);
                            gui.setSensitivityControlsEnabled(true);
                            gui.setSensitivityProgress("Progress: done");
                        });
                        workerThread = null;
                    }
                }, "airport-sensitivity-thread");

                workerThread.setDaemon(true);
                workerThread.start();
            });
        });
    }

    private static boolean isWorkerRunning() {
        return workerThread != null && workerThread.isAlive();
    }

    private record SensitivityPointResult(int capacity, double avgSecurityTime, boolean kpiSatisfied) {
    }

    private static Integer computeBestCapacity(Map<Integer, Double> avgByCap, Map<Integer, Boolean> kpiByCap) {
        Integer best = null;
        for (Map.Entry<Integer, Boolean> e : kpiByCap.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue()) && avgByCap.containsKey(e.getKey())) {
                if (best == null || e.getKey() > best) {
                    best = e.getKey();
                }
            }
        }
        return best;
    }

    private static List<Integer> buildCapacities(int base, int classes) {
        if (base <= 0 || classes < 2) {
            throw new IllegalArgumentException("Capacity must be > 0 and classes must be >= 2.");
        }

        int min = (int) Math.round(base * 0.9);
        int max = (int) Math.round(base * 1.1);

        List<Integer> out = new ArrayList<>();
        if (classes % 2 == 1) {
            for (int i = 0; i < classes; i++) {
                double v = min + i * (max - min) / (double) (classes - 1);
                out.add((int) Math.round(v));
            }
        } else {
            int lowerMiddle = classes / 2 - 1;
            for (int i = 0; i <= lowerMiddle; i++) {
                double v = (lowerMiddle == 0)
                        ? base
                        : (min + i * (base - min) / (double) lowerMiddle);
                out.add((int) Math.round(v));
            }
            int rightCount = classes - (lowerMiddle + 1);
            for (int j = 1; j <= rightCount; j++) {
                double v = base + j * (max - base) / (double) rightCount;
                out.add((int) Math.round(v));
            }
        }

        for (int i = 1; i < out.size(); i++) {
            if (out.get(i) <= out.get(i - 1)) {
                out.set(i, out.get(i - 1) + 1);
            }
        }

        return out;
    }

    private static boolean allKpiSatisfied(S2.Statistics.StatsForAirport s) {
        return s.getSimAvgCustomerSystemTime().getValue() <= 600.0
                && s.getSimAvgPickupQueueLen().getValue() <= 10.0
                && s.getSimAvgMetalQueueLen().getValue() <= 20.0;
    }
}
