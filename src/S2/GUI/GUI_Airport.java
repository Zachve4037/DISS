package S2.GUI;

import S2.SimCores.Airport;
import S2.Statistics.StatsForAirport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GUI_Airport {

    private JPanel panel1;
    private JTabbedPane tabbedPane1;

    private JComboBox<Integer> cratesInCombo;
    private JComboBox<Integer> cratesOutCombo;
    private JButton startSimButton;
    private JButton stopSimButton;

    private JSlider timeScaleSlider;
    private JCheckBox maxSpeedCheckBox;
    private JLabel simTimeLabel;

    private JTable statsTable;
    private DefaultTableModel statsModel;

    private Timer uiRefreshTimer;
    private Airport airport;

    public GUI_Airport() {
        JFrame frame = new JFrame("Airport");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel1 = new JPanel(new BorderLayout());
        tabbedPane1 = new JTabbedPane();

        JPanel simulationTab = new JPanel(new BorderLayout());

        // Top controls
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cratesInCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cratesOutCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        startSimButton = new JButton("Start Simulation");
        stopSimButton = new JButton("Stop");
        stopSimButton.setEnabled(false);

        row1.add(new JLabel("Crates In capacity:"));
        row1.add(cratesInCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(new JLabel("Crates Out capacity:"));
        row1.add(cratesOutCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(startSimButton);
        row1.add(stopSimButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeScaleSlider = new JSlider(1, 200, 50); // 1..200
        timeScaleSlider.setPreferredSize(new Dimension(220, 40));
        timeScaleSlider.setMajorTickSpacing(50);
        timeScaleSlider.setMinorTickSpacing(10);
        timeScaleSlider.setPaintTicks(true);
        timeScaleSlider.setPaintLabels(true);

        maxSpeedCheckBox = new JCheckBox("Max speed (no delay)");
        simTimeLabel = new JLabel("Sim time: 06:00:00");

        row2.add(new JLabel("Speed factor:"));
        row2.add(timeScaleSlider);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(maxSpeedCheckBox);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(simTimeLabel);

        controls.add(row1);
        controls.add(row2);

        // Stats table
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

        JPanel throughputTab = new JPanel(new BorderLayout());
        throughputTab.add(new JLabel("Priepustnost - TODO"), BorderLayout.CENTER);

        tabbedPane1.addTab("Simulacia", simulationTab);
        tabbedPane1.addTab("Priepustnost", throughputTab);

        panel1.add(tabbedPane1, BorderLayout.CENTER);
        frame.setContentPane(panel1);

        initListeners();
        initRefreshTimer();
        initEmptyRows();

        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initListeners() {
        maxSpeedCheckBox.addActionListener(e -> {
            boolean max = maxSpeedCheckBox.isSelected();
            timeScaleSlider.setEnabled(!max);
        });
    }

    private void initRefreshTimer() {
        uiRefreshTimer = new Timer(200, e -> refreshFromAirport());
        uiRefreshTimer.start();
    }

    private void initEmptyRows() {
        statsModel.setRowCount(0);
        addRow("Rep: customers entered unload queue", "-", "-");
        addRow("Rep: customers entered metal queue", "-", "-");
        addRow("Rep: avg wait unload (s)", "-", "-");
        addRow("Rep: avg wait metal (s)", "-", "-");
        addRow("Rep: crates entered xray queue", "-", "-");
        addRow("Rep: crates entered pickup queue", "-", "-");
        addRow("Rep: avg wait xray (s)", "-", "-");
        addRow("Rep: avg wait pickup (s)", "-", "-");
        addRow("Rep: avg customer system time (s)", "-", "-");

        addRow("Sim: avg customers unload queue", "-", "-");
        addRow("Sim: avg customers metal queue", "-", "-");
        addRow("Sim: avg crates xray queue", "-", "-");
        addRow("Sim: avg crates pickup queue", "-", "-");
        addRow("Sim: avg unload queue len", "-", "-");
        addRow("Sim: avg metal queue len", "-", "-");
        addRow("Sim: avg xray queue len", "-", "-");
        addRow("Sim: avg pickup queue len", "-", "-");
        addRow("Sim: avg wait unload (s)", "-", "-");
        addRow("Sim: avg wait metal (s)", "-", "-");
        addRow("Sim: avg wait xray (s)", "-", "-");
        addRow("Sim: avg wait pickup (s)", "-", "-");
        addRow("Sim: avg customer system time (s)", "-", "-");

        addRow("Queue NOW: unload people size", "-", "-");
        addRow("Queue NOW: unload people elements", "-", "-");
        addRow("Queue NOW: metal people size", "-", "-");
        addRow("Queue NOW: metal people elements", "-", "-");
        addRow("Queue NOW: xray enqueue crates size", "-", "-");
        addRow("Queue NOW: xray enqueue crates elements", "-", "-");
        addRow("Queue NOW: xray dequeue crates size", "-", "-");
        addRow("Queue NOW: xray dequeue crates elements", "-", "-");
    }

    private void refreshFromAirport() {
        if (airport == null) {
            return;
        }

        // Cas
        int secFromZero = (int) Math.round(airport.getCurrentTimeSeconds());
        simTimeLabel.setText("Sim time: " + formatFrom0600(secFromZero));

        StatsForAirport s = airport.getStats();

        // Replikacia
        setValue(0, safeNum(s.getRepCustomersEnteredUnloadQueue()), "-");
        setValue(1, safeNum(s.getRepCustomersEnteredMetalQueue()), "-");
        setValue(2, safeDouble(s.getRepWaitUnload().getValue()), "-");
        setValue(3, safeDouble(s.getRepWaitMetal().getValue()), "-");
        setValue(4, safeNum(s.getRepCratesEnteredXrayQueue()), "-");
        setValue(5, safeNum(s.getRepCratesEnteredPickupQueue()), "-");
        setValue(6, safeDouble(s.getRepWaitXray().getValue()), "-");
        setValue(7, safeDouble(s.getRepWaitPickup().getValue()), "-");
        setValue(8, safeDouble(s.getRepCustomerSystemTime().getValue()), "-");

        // Simulacia (priemer cez replikacie)
        setValue(9, safeDouble(s.getSimCustomersEnteredUnloadQueue().getValue()), "-");
        setValue(10, safeDouble(s.getSimCustomersEnteredMetalQueue().getValue()), "-");
        setValue(11, safeDouble(s.getSimCratesEnteredXrayQueue().getValue()), "-");
        setValue(12, safeDouble(s.getSimCratesEnteredPickupQueue().getValue()), "-");
        setValue(13, safeDouble(s.getSimAvgUnloadQueueLen().getValue()), "-");
        setValue(14, safeDouble(s.getSimAvgMetalQueueLen().getValue()), "-");
        setValue(15, safeDouble(s.getSimAvgXrayQueueLen().getValue()), "-");
        setValue(16, safeDouble(s.getSimAvgPickupQueueLen().getValue()), "-");
        setValue(17, safeDouble(s.getSimAvgWaitUnload().getValue()), "-");
        setValue(18, safeDouble(s.getSimAvgWaitMetal().getValue()), "-");
        setValue(19, safeDouble(s.getSimAvgWaitXray().getValue()), "-");
        setValue(20, safeDouble(s.getSimAvgWaitPickup().getValue()), "-");
        setValue(21, safeDouble(s.getSimAvgCustomerSystemTime().getValue()), "-");

        // Stav radov + prvky
        List<Integer> unloadIds = airport.getUnloadQueueTravellerIds();
        List<Integer> metalIds = airport.getMetalQueueTravellerIds();
        List<Integer> xrayEnqueueIds = airport.getXrayEnqueueCrateIds();
        List<Integer> xrayDequeueIds = airport.getXrayDequeueCrateIds();

        setValue(22, unloadIds.size(), "-");
        setValue(23, unloadIds.toString(), "traveller IDs");
        setValue(24, metalIds.size(), "-");
        setValue(25, metalIds.toString(), "traveller IDs");
        setValue(26, xrayEnqueueIds.size(), "-");
        setValue(27, xrayEnqueueIds.toString(), "crate IDs");
        setValue(28, xrayDequeueIds.size(), "-");
        setValue(29, xrayDequeueIds.toString(), "crate IDs");
    }

    private void addRow(String metric, Object value, String detail) {
        statsModel.addRow(new Object[]{metric, value, detail});
    }

    private void setValue(int row, Object value, Object detail) {
        statsModel.setValueAt(value, row, 1);
        statsModel.setValueAt(detail, row, 2);
    }

    private String safeDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "-";
        return String.format("%.4f", v);
    }

    private String safeNum(long v) {
        return String.valueOf(v);
    }

    private String formatFrom0600(int secondsFromStart) {
        int total = 6 * 3600 + Math.max(secondsFromStart, 0);
        int hh = (total / 3600) % 24;
        int mm = (total % 3600) / 60;
        int ss = total % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    // ---- API pre napojenie simulacie ----
    public void bindAirport(Airport airport) {
        this.airport = airport;
    }

    public int getCratesInCapacity() {
        return (Integer) cratesInCombo.getSelectedItem();
    }

    public int getCratesOutCapacity() {
        return (Integer) cratesOutCombo.getSelectedItem();
    }

    public boolean isMaxSpeed() {
        return maxSpeedCheckBox.isSelected();
    }

    public int getSpeedFactor() {
        return timeScaleSlider.getValue();
    }

    public JButton getStartSimButton() {
        return startSimButton;
    }

    public JButton getStopSimButton() {
        return stopSimButton;
    }
}
