package GUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class RouteGUI {
    private JButton pauseButton;
    private JButton stopButton;
    private JTextArea textArea1;
    private JLabel routeLabel;
    private JLabel replicationLabel;
    private JLabel avgTimeLabel;
    private ChartPanel chartPanel;

    private XYSeries series = new XYSeries("Priemerný čas");

    public RouteGUI() {
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

    public void appendLog(String message) {
        textArea1.append(message + "\n");
    }

    public JButton getPauseButton() { return pauseButton; }
    public JButton getStopButton() { return stopButton; }
}
