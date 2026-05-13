package S3.GUI;

import S3.agents.agentpatients.AgentPatients;
import S3.agents.agentresources.AgentResources;
import S3.entities.*;
import S3.simulation.EmergencyDepartmentSimulation;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GUI_EmergencyDepartment {
    private static final int MAX_DISPLAYED_PATIENT_ROWS = 250;

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
    private final JLabel replicationsLabel;

    private final DefaultTableModel simulationStatsModel;
    private final JTable simulationStatsTable;

    private final DefaultTableModel replicationStatsModel;
    private final JTable replicationStatsTable;

    private final DefaultTableModel doctorsModel;
    private final JTable doctorsTable;

    private final DefaultTableModel nursesModel;
    private final JTable nursesTable;

    private final DefaultTableModel clinicsModel;
    private final JTable clinicsTable;

    private final DefaultTableModel waitingForDoctorsModel;
    private final JTable waitingForDoctorsExaminationTable;

    private final DefaultTableModel waitingForInitialModel;
    private final JTable waitingForInitialExaminationTable;

    private int lastReplicationDay = -1;

    public GUI_EmergencyDepartment() {
        frame = new JFrame("Emergency Department");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1400, 900));

        tabbedPane = new JTabbedPane();

        JPanel simulationTab = new JPanel(new BorderLayout());

        simulationStatsModel = new DefaultTableModel(new Object[]{"Metric", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        simulationStatsTable = createTable(simulationStatsModel);

        replicationStatsModel = new DefaultTableModel(new Object[]{"Metric", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        replicationStatsTable = createTable(replicationStatsModel);

        doctorsModel = new DefaultTableModel(new Object[]{"Id", "CurrentPatient", "WorkTime", "State"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorsTable = createTable(doctorsModel);

        nursesModel = new DefaultTableModel(new Object[]{"Id", "CurrentPatient", "WorkTime", "State"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        nursesTable = createTable(nursesModel);

        clinicsModel = new DefaultTableModel(new Object[]{"Id", "Type", "IsOccupied"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clinicsTable = createTable(clinicsModel);

        waitingForDoctorsModel = new DefaultTableModel(new Object[]{"Id", "Type", "Priority", "WaitStart", "WaitTime", "SystemTime"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        waitingForDoctorsExaminationTable = createTable(waitingForDoctorsModel);

        waitingForInitialModel = new DefaultTableModel(new Object[]{"Id", "Type", "Priority", "WaitStart", "WaitTime", "SystemTime"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        waitingForInitialExaminationTable = createTable(waitingForInitialModel);

        tabbedPane.addTab("Statistics", simulationTab);

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
        timeScaleSlider = new JSlider(1, 200, 1);
        timeScaleSlider.setPreferredSize(new Dimension(220, 40));
        timeScaleSlider.setMajorTickSpacing(50);
        timeScaleSlider.setMinorTickSpacing(10);
        timeScaleSlider.setPaintTicks(true);
        timeScaleSlider.setPaintLabels(true);

        maxSpeedCheckBox = new JCheckBox("Max speed (no delay)");
        simTimeLabel = new JLabel("Sim time: 0 00:00:00");
        replicationsLabel = new JLabel("Replications: 0");

        row2.add(new JLabel("Speed factor:"));
        row2.add(timeScaleSlider);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(maxSpeedCheckBox);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(simTimeLabel);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(replicationsLabel);

        controls.add(row1);
        controls.add(row2);

        simulationTab.add(controls, BorderLayout.NORTH);
        JPanel lower = new JPanel(new GridLayout(1, 3, 10, 10));
        lower.add(createStackedTablesPanel(
                "Doctors", doctorsTable,
                "Nurses", nursesTable
        ));
        lower.add(createTablePanel("Clinics", clinicsTable));
        lower.add(createStackedTablesPanel(
                "Waiting for doctors exam", waitingForDoctorsExaminationTable,
                "Waiting for initial exam", waitingForInitialExaminationTable
        ));

        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        statsPanel.add(createTablePanel("Simulation statistics", simulationStatsTable));
        statsPanel.add(createTablePanel("Replication statistics", replicationStatsTable));

        lower.setMinimumSize(new Dimension(200, 200));
        statsPanel.setMinimumSize(new Dimension(200, 150));

        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsPanel, lower);
        topSplit.setResizeWeight(0.28);
        topSplit.setDividerLocation(382);
        topSplit.setContinuousLayout(true);
        topSplit.setBorder(null);

        center.add(topSplit, BorderLayout.CENTER);

        simulationTab.add(center, BorderLayout.CENTER);

        JPanel secondTab = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Second tab prepared for later use", SwingConstants.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(Font.PLAIN, 16f));
        secondTab.add(placeholder, BorderLayout.CENTER);

        tabbedPane.addTab("Animator", secondTab);

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
            changeSimSpeed();
        });
        startSimButton.addActionListener(e -> {
            System.out.println("[GUI] StartSimulation clicked");

            if (workerThread != null && workerThread.isAlive()) {
                JOptionPane.showMessageDialog(frame, "Simulation is already running.");
                return;
            }

            lastReplicationDay = -1;
            simulation = new EmergencyDepartmentSimulation();
            bindSimulation(simulation);
            simulation.setParameters(getDoctorsCount(), getNursesCount());
            changeSimSpeed();

            System.out.printf("[GUI] params: doctors=%d, nurses=%d, speed=%d, maxSpeed=%s%n",
                    getDoctorsCount(), getNursesCount(), getSpeedFactor(), isMaxSpeed());

            startSimButton.setEnabled(false);
            stopSimButton.setEnabled(true);

            workerThread = new Thread(() -> {
                try {
                    System.out.println("[SIM] Worker thread started");
                    simulation.setParameters(getDoctorsCount(), getNursesCount());
                    simulation.simulate(1000, 8 * 24 * 3600);
                    System.out.println("[SIM] Simulation call finished");
                } catch (Throwable t) {
                    System.err.println("[SIM] Simulation crashed:");
                    t.printStackTrace();
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame, "Simulation error: " + t.getMessage())
                    );
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
            System.out.println("[GUI] Stop clicked");
            if (simulation != null) {
                invokeSimulationStop(simulation);
            }
        });
        ChangeListener changeListener = (ChangeEvent e) -> changeSimSpeed();
        timeScaleSlider.addChangeListener(changeListener);
    }

    private void changeSimSpeed() {
        if (simulation == null) return;

        try {
            if (isMaxSpeed()) {
                simulation.setMaxSimSpeed();
            } else {
                int simSecondsPerRealSecond = getSpeedFactor();
                double realSecondsPerSimSecond = 1.0 / (double) simSecondsPerRealSecond;
                simulation.setSimSpeed(1, realSecondsPerSimSecond);
            }
        } catch (Throwable t) {
            System.err.println("[GUI] changeSimSpeed error:");
            t.printStackTrace();
        }
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

    private void refreshFromEmergencyDepartment() {
        if (simulation == null) {
            return;
        }

        AgentResources resources;
        AgentPatients patientsAgent;

        try {
            resources = simulation.agentResources();
            patientsAgent = simulation.agentPatients();
        } catch (Exception ignored) {
            return;
        }

        List<EmerDepEmployee> doctors = (resources.getDoctors() == null)
                ? Collections.emptyList() : new ArrayList<>(resources.getDoctors());
        List<EmerDepEmployee> nurses = (resources.getNurses() == null)
                ? Collections.emptyList() : new ArrayList<>(resources.getNurses());
        List<Clinic> clinics = (resources.getClinics() == null)
                ? Collections.emptyList() : new ArrayList<>(resources.getClinics());

        Queue<Patient> initialQueue = patientsAgent.getInitialExaminationQueue();
        Queue<Patient> doctorsQueue = patientsAgent.getDoctorsExaminationQueue();

        int waitingInitialCount = initialQueue == null ? 0 : initialQueue.size();
        int waitingDoctorsCount = doctorsQueue == null ? 0 : doctorsQueue.size();
        int busyEmployees = (int) java.util.stream.Stream.concat(doctors.stream(), nurses.stream())
                .filter(EmerDepEmployee::isBusy)
                .count();

        List<Patient> waitingDoctors = doctorsQueue == null
                ? Collections.emptyList()
                : doctorsQueue.stream()
                        .filter(Patient::isWaitingForDoctorsExam)
                        .sorted(Comparator.comparingInt(Patient::getPriority)
                                .thenComparingDouble(Patient::getArrivalTime)
                                .thenComparingInt(Patient::getId))
                        .limit(MAX_DISPLAYED_PATIENT_ROWS)
                        .collect(Collectors.toList());

        List<Patient> waitingInitial = initialQueue == null
                ? Collections.emptyList()
                : initialQueue.stream()
                        .filter(Patient::isWaitingForInitialExam)
                        .sorted(Comparator
                                .comparingInt((Patient p) -> p.getType() == PatientType.AMBULANCE ? 0 : 1)
                                .thenComparingDouble(Patient::getArrivalTime)
                                .thenComparingInt(Patient::getId))
                        .limit(MAX_DISPLAYED_PATIENT_ROWS)
                        .collect(Collectors.toList());

        refreshSimulationStats(waitingInitialCount, waitingDoctorsCount, busyEmployees);
        refreshEmployees(doctorsModel, doctors, EmployeeType.DOCTOR);
        refreshEmployees(nursesModel, nurses, EmployeeType.NURSE);
        refreshClinics(clinicsModel, clinics);
        refreshPatients(waitingForDoctorsModel, waitingDoctors);
        refreshPatients(waitingForInitialModel, waitingInitial);

        setSimTimeSeconds((int) Math.round(simulation.currentTime()));
        setReplicationsLabel(simulation.currentReplication());
    }

    private void refreshSimulationStats(int waitingInitial, int waitingDoctors, int busyEmployees) {
        simulationStatsModel.setRowCount(0);
        replicationStatsModel.setRowCount(0);

        long entered = simulation.getSimulationArrivals();
        long departed = simulation.getSimulationDepartures();

        long inSystem = Math.max(simulation.getReplicationArrivals() - simulation.getReplicationDepartures(), 0);

        replicationStatsModel.addRow(new Object[]{"--- CURRENT STATE ---", ""});
        replicationStatsModel.addRow(new Object[]{"Patients currently in system", safeNum(inSystem)});
        replicationStatsModel.addRow(new Object[]{"Waiting for initial exam", safeNum(waitingInitial)});
        replicationStatsModel.addRow(new Object[]{"Waiting for doctors exam", safeNum(waitingDoctors)});
        replicationStatsModel.addRow(new Object[]{"Busy employees", safeNum(busyEmployees)});

        simulationStatsModel.addRow(new Object[]{"--- SIMULATION STATS ---", ""});
        simulationStatsModel.addRow(new Object[]{"Patients entered system", safeNum(entered)});
        simulationStatsModel.addRow(new Object[]{"Patients departed", safeNum(departed)});
        simulationStatsModel.addRow(new Object[]{"Ambulance arrivals", safeNum(simulation.getSimulationAmbulanceArrivals())});
        simulationStatsModel.addRow(new Object[]{"Walk-in arrivals", safeNum(simulation.getSimulationWalkInArrivals())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time initial exam (s)", formatDouble(simulation.getSimulationAverageInitialExamWait())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - ambulance (s)", formatDouble(simulation.getSimulationAverageDoctorWaitAmbulance())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - walk-in (s)", formatDouble(simulation.getSimulationAverageDoctorWaitWalkIn())});
        simulationStatsModel.addRow(new Object[]{"Avg time in system (s)", formatDouble(simulation.getSimulationAverageSystemTime())});

        replicationStatsModel.addRow(new Object[]{"--- REPLICATION STATS ---", ""});
        replicationStatsModel.addRow(new Object[]{"Patients entered system", safeNum(simulation.getReplicationArrivals())});
        replicationStatsModel.addRow(new Object[]{"Patients departed", safeNum(simulation.getReplicationDepartures())});
        replicationStatsModel.addRow(new Object[]{"Ambulance arrivals", safeNum(simulation.getReplicationAmbulanceArrivals())});
        replicationStatsModel.addRow(new Object[]{"Walk-in arrivals", safeNum(simulation.getReplicationWalkInArrivals())});
        replicationStatsModel.addRow(new Object[]{"Avg waiting time initial exam (s)", formatDouble(simulation.getReplicationAverageInitialExamWait())});
        replicationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - ambulance (s)", formatDouble(simulation.getReplicationAverageDoctorWaitAmbulance())});
        replicationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - walk-in (s)", formatDouble(simulation.getReplicationAverageDoctorWaitWalkIn())});
        replicationStatsModel.addRow(new Object[]{"Avg time in system (s)", formatDouble(simulation.getReplicationAverageSystemTime())});
    }

    private void refreshEmployees(DefaultTableModel model, List<EmerDepEmployee> employees, EmployeeType type) {
        model.setRowCount(0);

        for (EmerDepEmployee e : employees) {
            if (e.getType() != type) {
                continue;
            }

            Patient cp = e.getCurrentPatient();
            String currentPatient = (cp == null) ? "-" : cp.getId() + " (" + cp.getType() + ")";
            double workTime;

            if (e.getCurrentPatient() != null) {
                workTime = simulation.currentTime() - e.getWorkStartTime();
            } else {
                workTime = e.getTotalWorkTime();
            }

            model.addRow(new Object[]{
                    e.getId(),
                    currentPatient,
                    formatDouble(workTime),
                    e.getState()
            });
        }
    }

    private void refreshClinics(DefaultTableModel model, List<Clinic> clinics) {
        model.setRowCount(0);

        for (Clinic clinic : clinics) {
            model.addRow(new Object[]{
                    clinic.getId(),
                    clinic.getType(),
                    clinic.isOccupied()
            });
        }
    }

    private void refreshPatients(DefaultTableModel model, List<Patient> patients) {
        model.setRowCount(0);

        for (Patient p : patients) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getType(),
                    p.getPriority(),
                    formatDouble(p.getWaitStartTime()),
                    formatDouble(p.getTotalWaitTime()),
                    formatDouble(p.getTotalTimeInSystem())
            });
        }
    }

    private String formatDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "-";
        return String.format("%.4f", v);
    }

    private String safeNum(long v) {
        return String.valueOf(v);
    }

    public void setSimTimeSeconds(int secondsFromStart) {
        simTimeLabel.setText("Sim time: " + formatTime(secondsFromStart));
    }

    public void setReplicationsLabel(int replications) {
        replicationsLabel.setText("Replications: " + replications);
    }

    private String formatTime(int secondsFromStart) {
        int total = Math.max(secondsFromStart, 0);
        int DD = total / (24 * 3600);
        int hh = (total / 3600) % 24;
        int mm = (total % 3600) / 60;
        int ss = total % 60;
        return String.format("%d %02d:%02d:%02d", DD , hh, mm, ss);
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

    private JPanel controlsPanel() {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        return controls;
    }

    private JPanel createTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStackedTablesPanel(String topTitle, JTable topTable, String bottomTitle, JTable bottomTable) {
        JPanel top = createTablePanel(topTitle, topTable);
        JPanel bottom = createTablePanel(bottomTitle, bottomTable);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.5);
        split.setDividerLocation(250);
        split.setContinuousLayout(true);
        split.setBorder(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        return table;
    }
}