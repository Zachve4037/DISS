package S1.GUI;

import S1.MonteCarlo.CourierRouteSimCore;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RouteGUI extends JFrame {
    private static final String[] ROUTES = {
            "Z_S_RT_D_Z",
            "Z_S_D_RT_Z",
            "Z_RT_S_D_Z",
            "Z_RT_D_S_Z",
            "Z_D_S_RT_Z",
            "Z_D_RT_S_Z"
    };

    private static final int REPLICATIONS = 0;
    private static final int UI_PROGRESS_STEP = 10_000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JLabel routeLabel;
    private JLabel replicationLabel;
    private JLabel avgTimeLabel;
    private JComboBox<String> routeComboBox;
    private ChartPanel chartPanel;
    private Future<?> runningTask;
    private CourierRouteSimCore currentSimulation;
    private XYSeries series;
    private boolean paused = false;

    public RouteGUI() {
        this.createUIComponents();

        setTitle("Courier Route Simulation");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        if (routeComboBox == null) routeComboBox = new JComboBox<>();
        routeComboBox.removeAllItems();
        for (String route : ROUTES) {
            routeComboBox.addItem(route);
        }

        if (startButton == null) startButton = new JButton("Start");
        if (pauseButton == null) pauseButton = new JButton("Pause");
        if (stopButton == null) stopButton = new JButton("Stop");
        if (routeLabel == null) routeLabel = new JLabel("Route: -");
        if (replicationLabel == null) replicationLabel = new JLabel("Replications: 0");
        if (avgTimeLabel == null) avgTimeLabel = new JLabel("Average time: -");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Route:"));
        controls.add(routeComboBox);
        controls.add(startButton);
        controls.add(pauseButton);
        controls.add(stopButton);
        controls.add(routeLabel);
        controls.add(replicationLabel);
        controls.add(avgTimeLabel);

        add(controls, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        wireActions();
    }

    private void createUIComponents() {
        if (series == null) {
            series = new XYSeries("Average Time");
            series.setMaximumItemCount(2000);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monte Carlo - Stabilization of Average Time",
                "Replications",
                "Average route time (min)",
                dataset
        );
        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeStickyZero(false);
        yAxis.setLowerMargin(0.05);
        yAxis.setUpperMargin(0.05);
        chartPanel = new ChartPanel(chart);
    }

    private void wireActions() {
        startButton.addActionListener(e -> startSimulation());
        pauseButton.addActionListener(e -> togglePause());
        stopButton.addActionListener(e -> stopSimulation());
    }

    private void startSimulation() {
        if (runningTask != null && !runningTask.isDone()) return;

        series.clear();
        paused = false;
        pauseButton.setText("Pause");

        String route = (String) routeComboBox.getSelectedItem();
        routeLabel.setText("Route: " + route);
        replicationLabel.setText("Replications: 0");
        avgTimeLabel.setText("Average time: -");

        currentSimulation = new CourierRouteSimCore(route, UI_PROGRESS_STEP, new CourierRouteSimCore.ProgressListener() {
            @Override
            public void onProgress(long replication, double avgMinutes) {
                SwingUtilities.invokeLater(() -> {
                    series.add((double) replication, avgMinutes);
                    replicationLabel.setText("Replications: " + replication);
                    avgTimeLabel.setText(String.format("Average time: %.4f min", avgMinutes));
                });
            }

            @Override
            public void onFinished(long replications, double avgMinutes, String avgReturnTime) {
                SwingUtilities.invokeLater(() -> {
                    replicationLabel.setText("Replications: " + replications);
                    avgTimeLabel.setText(String.format("Average: %.4f min | Return: %s", avgMinutes, avgReturnTime));
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    routeComboBox.setEnabled(true);
                });
            }
        });

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
        routeComboBox.setEnabled(false);

        runningTask = executor.submit(() -> currentSimulation.runSimulation(REPLICATIONS));
    }

    private void togglePause() {
        if (currentSimulation == null) return;
        paused = !paused;
        currentSimulation.setPaused(paused);
        pauseButton.setText(paused ? "Resume" : "Pause");
    }

    private void stopSimulation() {
        if (currentSimulation != null) currentSimulation.stopSimulation();
    }
}