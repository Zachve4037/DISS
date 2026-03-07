package GUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class RouteGUI extends JFrame {
    private JButton pauseButton;
    private JButton stopButton;
    private JButton startButton;
    private JLabel routeLabel;
    private JLabel replicationLabel;
    private JLabel avgTimeLabel;
    private ChartPanel chartPanel;
    private JComboBox routeComboBox;
    private final XYSeries series = new XYSeries("Priemerný čas");

    private static final String[] ROUTES = {
            "Ž→S→R→D→Ž",
            "Ž→S→D→R→Ž",
            "Ž→R→S→D→Ž",
            "Ž→R→D→S→Ž",
            "Ž→D→S→R→Ž",
            "Ž→D→R→S→Ž"
    };


    public RouteGUI() {
        setTitle("Monte Carlo - priemery");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUIComponents();
        JPanel controlPanel = new JPanel();
        controlPanel.add(routeComboBox);
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(routeLabel);
        controlPanel.add(replicationLabel);
        controlPanel.add(avgTimeLabel);
        add(controlPanel, "North");
        add(chartPanel, "Center");
    }

    private void createUIComponents() {
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monte Carlo - priemery",
                "Replikácia",
                "Priemerný čas",
                dataset
        );
        chartPanel = new ChartPanel(chart);
        routeComboBox = new JComboBox<>();
        for (String route : ROUTES) {
            routeComboBox.addItem(route);
        }
    }

    public void addDataPoint(int replication, double avgTime) {
        SwingUtilities.invokeLater(() -> series.add(replication, avgTime));
    }

    public void updateRoute(String route) {
        routeLabel.setText("Trasa: " + route);
    }

    public void updateReplicationCount(int count) {
        replicationLabel.setText("Počet replikácií: " + count);
    }

    public void updateAvgTime(String time) {
        avgTimeLabel.setText("Priemerný čas: " + time);
    }

    public String getSelectedRoute() {
        return (String) routeComboBox.getSelectedItem();
    }

    public JButton getPauseButton() { return pauseButton; }
    public JButton getStopButton() { return stopButton; }
    public JButton getStartButton() { return startButton; }
    public JComboBox getRouteComboBox() { return routeComboBox; }
}
