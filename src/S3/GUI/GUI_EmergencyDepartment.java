package S3.GUI;

import S3.simulation.EmergencyDepartmentSimulation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GUI_EmergencyDepartment {
    private EmergencyDepartmentSimulation simulation;
    private Thread workerThread;

    private final JFrame frame;
    private final JTabbedPane tabbedPane;

    private final JComboBox<Integer> doctorsCountCombo;
    private final JComboBox<Integer> nursesCountCombo;
    private final JButton startSimButton;
    private final JButton stopSimButton;

    private final JSlider timeScaleSlider;
    private final JCheckBox maxSpeedCheckBox;
    private final JLabel simTimeLabel;

    private final DefaultTableModel statsModel;
    private final JTable statsTable;

    public GUI_EmergencyDepartment() {
        frame = new JFrame("Emergency Department");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        JPanel simulationTab = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        doctorsCountCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        nursesCountCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        startSimButton = new JButton("Start Simulation");
        stopSimButton = new JButton("Stop");
        stopSimButton.setEnabled(false);

        row1.add(new JLabel("Doctors count:"));
        row1.add(doctorsCountCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(new JLabel("Nurses count:"));
        row1.add(nursesCountCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(startSimButton);
        row1.add(stopSimButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeScaleSlider = new JSlider(1, 200, 50);
        timeScaleSlider.setPreferredSize(new Dimension(220, 40));
        timeScaleSlider.setMajorTickSpacing(50);
        timeScaleSlider.setMinorTickSpacing(10);
        timeScaleSlider.setPaintTicks(true);
        timeScaleSlider.setPaintLabels(true);

        maxSpeedCheckBox = new JCheckBox("Max speed (no delay)");
        simTimeLabel = new JLabel("Sim time: 00:00:00");

        row2.add(new JLabel("Speed factor:"));
        row2.add(timeScaleSlider);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(maxSpeedCheckBox);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(simTimeLabel);

        controls.add(row1);
        controls.add(row2);

        statsModel = new DefaultTableModel(new Object[]{"Metric", "Value", "Detail"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statsTable = new JTable(statsModel);
        statsTable.setFillsViewportHeight(true);
        statsTable.setRowHeight(22);

        JScrollPane tableScroll = new JScrollPane(statsTable);

        simulationTab.add(controls, BorderLayout.NORTH);
        simulationTab.add(tableScroll, BorderLayout.CENTER);

        initEmptyRows();

        JPanel secondTab = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Second tab prepared for later use", SwingConstants.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(Font.PLAIN, 16f));
        secondTab.add(placeholder, BorderLayout.CENTER);

        tabbedPane.addTab("Simulation", simulationTab);
        tabbedPane.addTab("Tab 2", secondTab);

        frame.setContentPane(tabbedPane);

        initListeners();
        initRefreshTimer();

        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initListeners() {
        maxSpeedCheckBox.addActionListener(e -> {
            boolean max = maxSpeedCheckBox.isSelected();
            timeScaleSlider.setEnabled(!max);
        });
        startSimButton.addActionListener(e -> {
            if (workerThread != null && workerThread.isAlive()) {
                JOptionPane.showMessageDialog(null, "Simulation is already running.");
            }

            simulation = new EmergencyDepartmentSimulation();
            bindSimulation(simulation);

            simulation.setParams(getDoctorsCount(), getNursesCount());

            startSimButton.setEnabled(false);
            stopSimButton.setEnabled(true);

            workerThread = new Thread(() -> {
                try {
                    simulation.simulate(10000);
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        requestRefresh();
                        startSimButton.setEnabled(true);
                        stopSimButton.setEnabled(false);
                    });
                    workerThread = null;
                }
            }, "ed-sim-thread");

            workerThread.setDaemon(true);
            workerThread.start();
        });
        stopSimButton.addActionListener(e -> {
            if (simulation != null) {
                simulation.stopSimulation();
            }
        });
    }

    private void initRefreshTimer() {
        Timer uiRefreshTimer = new Timer(1000, e -> {
            try {
                refreshFromEmergencyDepartment();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        uiRefreshTimer.start();
    }

    private void initEmptyRows() {
        statsModel.setRowCount(0);
        addRow("People entered system", "0", "-");
    }

    private void addRow(String metric, Object value, String detail) {
        statsModel.addRow(new Object[]{metric, value, detail});
    }

    private void setValue(int row, Object value, Object detail) {
        statsModel.setValueAt(value, row, 1);
        statsModel.setValueAt(detail, row, 2);
    }

    private void refreshFromEmergencyDepartment() {
        if (simulation == null) return;
        setPeopleEnteredSystem(simulation.getTotalArrivals());
        setSimTimeSeconds((int) Math.round(simulation.currentTime()));
    }

    public void setPeopleEnteredSystem(long value) {
        if (statsModel.getRowCount() > 0) {
            setValue(0, String.valueOf(value), "-");
        }
    }

    public void setSimTimeSeconds(int secondsFromStart) {
        simTimeLabel.setText("Sim time: " + formatTime(secondsFromStart));
    }

    private String formatTime(int secondsFromStart) {
        int total = Math.max(secondsFromStart, 0);
        int hh = (total / 3600) % 24;
        int mm = (total % 3600) / 60;
        int ss = total % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    public boolean isMaxSpeed() {
        return maxSpeedCheckBox.isSelected();
    }

    public int getSpeedFactor() {
        return timeScaleSlider.getValue();
    }

    public int getDoctorsCount() {
        return (Integer) doctorsCountCombo.getSelectedItem();
    }

    public int getNursesCount() {
        return (Integer) nursesCountCombo.getSelectedItem();
    }

    public JButton getStartSimButton() {
        return startSimButton;
    }

    public JButton getStopSimButton() {
        return stopSimButton;
    }

    public void requestRefresh() {
        if (SwingUtilities.isEventDispatchThread()) {
            refreshFromEmergencyDepartment();
        } else {
            SwingUtilities.invokeLater(this::refreshFromEmergencyDepartment);
        }
    }

    public void bindSimulation(EmergencyDepartmentSimulation mySim) {
        this.simulation = mySim;
    }

    private void invokeSimulationStart(EmergencyDepartmentSimulation sim) {
        if (tryInvoke(sim, "simulate")) return;
        if (tryInvoke(sim, "run")) return;
        tryInvoke(sim, "startSimulation");
    }

    private void invokeSimulationStop(EmergencyDepartmentSimulation sim) {
        if (tryInvoke(sim, "stopSimulation")) return;
        tryInvoke(sim, "stop");
    }

    private boolean tryInvoke(Object target, String method) {
        try {
            target.getClass().getMethod(method).invoke(target);
            return true;
        } catch (Exception ignored) {
        }
        try {
            var m = target.getClass().getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(target);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}