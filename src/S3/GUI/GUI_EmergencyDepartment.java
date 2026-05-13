package S3.GUI;

import OSPAnimator.Animator;
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
    private final JComboBox<String> staffingScenarioCombo;
    private final JCheckBox ambulanceCleanupBlockCheckBox;
    private final JCheckBox reserveInitialClinicsCheckBox;
    private final JCheckBox reservePriority12ClinicCheckBox;
    private final JCheckBox dynamicInitialOverflowCheckBox;
    private final JSpinner overflowThresholdSpinner;
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

    private final JPanel animatorTab;
    private final ConvergenceChartPanel convergenceChartPanel;

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
        staffingScenarioCombo = new JComboBox<>(new String[]{
                "Default GUI values",
                "6 doctors / 6 nurses",
                "6 doctors / 8 nurses",
                "6 doctors / 10 nurses"
        });
        startSimButton = new JButton("Start Simulation");
        stopSimButton = new JButton("Stop");
        stopSimButton.setEnabled(false);

        row1.add(new JLabel("Doctors count:"));
        row1.add(doctorsCountCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(new JLabel("Nurses count:"));
        row1.add(nursesCountCombo);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(new JLabel("Staffing scenario:"));
        row1.add(staffingScenarioCombo);
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

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.setBorder(BorderFactory.createTitledBorder("Experiments"));
        ambulanceCleanupBlockCheckBox = new JCheckBox("Ambulance cleanup block");
        reserveInitialClinicsCheckBox = new JCheckBox("Reserve 2 clinics for initial exam");
        reservePriority12ClinicCheckBox = new JCheckBox("Reserve 1 A clinic for priority 1-2");
        dynamicInitialOverflowCheckBox = new JCheckBox("Dynamic initial overflow");
        overflowThresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));

        row3.add(ambulanceCleanupBlockCheckBox);
        row3.add(Box.createHorizontalStrut(8));
        row3.add(reserveInitialClinicsCheckBox);
        row3.add(Box.createHorizontalStrut(8));
        row3.add(reservePriority12ClinicCheckBox);
        row3.add(Box.createHorizontalStrut(8));
        row3.add(dynamicInitialOverflowCheckBox);
        row3.add(new JLabel("Threshold:"));
        row3.add(overflowThresholdSpinner);

        controls.add(row1);
        controls.add(row2);
        controls.add(row3);

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

        animatorTab = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Animator will be shown after simulation is started", SwingConstants.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(Font.PLAIN, 16f));
        animatorTab.add(placeholder, BorderLayout.CENTER);

        tabbedPane.addTab("Animator", animatorTab);

        JPanel convergenceTab = new JPanel(new BorderLayout(10, 10));
        JLabel recommendation = new JLabel("Recommended minimal staffing: 6 doctors / 8 nurses. Limits: ambulance <= 15 min, walk-in < 30 min.", SwingConstants.CENTER);
        recommendation.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        convergenceChartPanel = new ConvergenceChartPanel();
        convergenceTab.add(recommendation, BorderLayout.NORTH);
        convergenceTab.add(convergenceChartPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Convergence", convergenceTab);

        frame.setContentPane(tabbedPane);

        initListeners();
        initRefreshTimer();

        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initListeners() {
        staffingScenarioCombo.addActionListener(e -> updateStaffingControls());
        updateStaffingControls();

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
            int doctorsCount = getEffectiveDoctorsCount();
            int nursesCount = getEffectiveNursesCount();
            boolean cleanupBlock = isAmbulanceCleanupBlockEnabled();
            boolean reserveInitial = isReserveInitialClinicsEnabled();
            boolean reservePriority12 = isReservePriority12ClinicEnabled();
            boolean dynamicOverflow = isDynamicInitialOverflowEnabled();
            int overflowThreshold = getOverflowThreshold();

            simulation.setExperimentSettings(
                    cleanupBlock,
                    reserveInitial,
                    reservePriority12,
                    dynamicOverflow,
                    overflowThreshold
            );
            if (!isMaxSpeed()) {
                simulation.createAnimator();
                if (simulation.animator() instanceof Animator) {
                    ((Animator) simulation.animator()).setSynchronizedTime(false);
                }
            }
            installAnimatorCanvas(simulation);
            bindSimulation(simulation);
            simulation.setParameters(doctorsCount, nursesCount);
            changeSimSpeed();

            System.out.printf("[GUI] params: doctors=%d, nurses=%d, speed=%d, maxSpeed=%s, cleanup=%s, initialOnly=%s, p12Only=%s, overflow=%s(threshold=%d)%n",
                    doctorsCount, nursesCount, getSpeedFactor(), isMaxSpeed(),
                    cleanupBlock, reserveInitial, reservePriority12, dynamicOverflow, overflowThreshold);

            startSimButton.setEnabled(false);
            stopSimButton.setEnabled(true);

            workerThread = new Thread(() -> {
                try {
                    System.out.println("[SIM] Worker thread started");
                    simulation.setParameters(doctorsCount, nursesCount);
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

    private void installAnimatorCanvas(EmergencyDepartmentSimulation sim) {
        animatorTab.removeAll();

        try {
            if (sim != null && sim.animatorExists() && sim.animator() != null) {
                JComponent canvas = sim.animator().canvas();
                animatorTab.add(canvas, BorderLayout.CENTER);

                SwingUtilities.invokeLater(() -> {
                    try {
                        if (sim.animator() instanceof Animator) {
                            ((Animator) sim.animator()).setView(0, 0, 1400, 720);
                        }
                    } catch (Throwable ignored) {
                    }
                    canvas.revalidate();
                    canvas.repaint();
                });
            } else {
                animatorTab.add(new JLabel("Animator is disabled in max-speed mode. Turn off max speed and start again to view animation.", SwingConstants.CENTER), BorderLayout.CENTER);
            }
        } catch (Throwable t) {
            animatorTab.add(new JLabel("Animator could not be initialized: " + t.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
        }

        animatorTab.revalidate();
        animatorTab.repaint();
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
        convergenceChartPanel.setData(
                simulation.getDoctorWaitAmbulanceConvergence(),
                simulation.getDoctorWaitWalkInConvergence()
        );

        setSimTimeSeconds((int) Math.round(simulation.currentTime()));
        setReplicationsLabel(simulation.currentReplication());
    }

    private void refreshSimulationStats(int waitingInitial, int waitingDoctors, int busyEmployees) {
        simulationStatsModel.setRowCount(0);
        replicationStatsModel.setRowCount(0);

        long inSystem = Math.max(simulation.getReplicationArrivals() - simulation.getReplicationDepartures(), 0);

        replicationStatsModel.addRow(new Object[]{"--- CURRENT STATE ---", ""});
        replicationStatsModel.addRow(new Object[]{"Patients currently in system", safeNum(inSystem)});
        replicationStatsModel.addRow(new Object[]{"Waiting for initial exam", safeNum(waitingInitial)});
        replicationStatsModel.addRow(new Object[]{"Waiting for doctors exam", safeNum(waitingDoctors)});
        replicationStatsModel.addRow(new Object[]{"Busy employees", safeNum(busyEmployees)});

        simulationStatsModel.addRow(new Object[]{"--- SIMULATION STATS ---", ""});
        simulationStatsModel.addRow(new Object[]{"Patients entered system", formatCi(simulation.getSimulationMeanArrivalsPerReplication(), simulation.getSimulationArrivalsCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Patients departed", formatCi(simulation.getSimulationMeanDeparturesPerReplication(), simulation.getSimulationDeparturesCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Ambulance arrivals", formatCi(simulation.getSimulationMeanAmbulanceArrivalsPerReplication(), simulation.getSimulationAmbulanceArrivalsCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Walk-in arrivals", formatCi(simulation.getSimulationMeanWalkInArrivalsPerReplication(), simulation.getSimulationWalkInArrivalsCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time initial exam (s)", formatCi(simulation.getSimulationMeanInitialExamWait(), simulation.getSimulationInitialExamWaitCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - ambulance (s)", formatCi(simulation.getSimulationMeanDoctorWaitAmbulance(), simulation.getSimulationDoctorWaitAmbulanceCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Avg waiting time doctor exam - walk-in (s)", formatCi(simulation.getSimulationMeanDoctorWaitWalkIn(), simulation.getSimulationDoctorWaitWalkInCiHalfWidth())});
        simulationStatsModel.addRow(new Object[]{"Avg time in system (s)", formatCi(simulation.getSimulationMeanSystemTime(), simulation.getSimulationSystemTimeCiHalfWidth())});

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

    private String formatCi(double mean, double halfWidth) {
        if (Double.isNaN(mean) || Double.isInfinite(mean)) {
            return "-";
        }
        if (Double.isNaN(halfWidth) || Double.isInfinite(halfWidth)) {
            return formatDouble(mean) + " [CI pending]";
        }
        double low = mean - halfWidth;
        double high = mean + halfWidth;
        return String.format("%.4f [%.4f; %.4f]", mean, low, high);
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

    private int getEffectiveDoctorsCount() {
        return switch (staffingScenarioCombo.getSelectedIndex()) {
            case 1, 2, 3 -> 6;
            default -> getDoctorsCount();
        };
    }

    private int getEffectiveNursesCount() {
        return switch (staffingScenarioCombo.getSelectedIndex()) {
            case 1 -> 6;
            case 2 -> 8;
            case 3 -> 10;
            default -> getNursesCount();
        };
    }

    private boolean isAmbulanceCleanupBlockEnabled() {
        return ambulanceCleanupBlockCheckBox.isSelected();
    }

    private boolean isReserveInitialClinicsEnabled() {
        return reserveInitialClinicsCheckBox.isSelected();
    }

    private boolean isReservePriority12ClinicEnabled() {
        return reservePriority12ClinicCheckBox.isSelected();
    }

    private boolean isDynamicInitialOverflowEnabled() {
        return dynamicInitialOverflowCheckBox.isSelected();
    }

    private int getOverflowThreshold() {
        return ((Number) overflowThresholdSpinner.getValue()).intValue();
    }

    private void updateStaffingControls() {
        boolean defaultScenario = staffingScenarioCombo.getSelectedIndex() == 0;
        doctorsCountCombo.setEnabled(defaultScenario);
        nursesCountCombo.setEnabled(defaultScenario);
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

    private static class ConvergenceChartPanel extends JPanel {
        private static final double AMBULANCE_LIMIT_MIN = 15.0;
        private static final double WALK_IN_LIMIT_MIN = 30.0;

        private List<Double> ambulanceSeconds = Collections.emptyList();
        private List<Double> walkInSeconds = Collections.emptyList();

        ConvergenceChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }

        void setData(List<Double> ambulanceSeconds, List<Double> walkInSeconds) {
            this.ambulanceSeconds = ambulanceSeconds == null ? Collections.emptyList() : List.copyOf(ambulanceSeconds);
            this.walkInSeconds = walkInSeconds == null ? Collections.emptyList() : List.copyOf(walkInSeconds);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int left = 78;
                int right = 28;
                int top = 34;
                int bottom = 58;
                int plotW = Math.max(1, w - left - right);
                int plotH = Math.max(1, h - top - bottom);

                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(left, top, plotW, plotH);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRect(left, top, plotW, plotH);

                int points = Math.max(ambulanceSeconds.size(), walkInSeconds.size());
                if (points == 0) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString("Convergence graph will appear after simulation replications finish.", left + 18, top + 30);
                    g2.dispose();
                    return;
                }

                double maxMinutes = Math.max(WALK_IN_LIMIT_MIN, maxMinutes(ambulanceSeconds, walkInSeconds));
                maxMinutes = Math.ceil(maxMinutes * 1.12 / 5.0) * 5.0;

                drawGrid(g2, left, top, plotW, plotH, maxMinutes);
                drawLimit(g2, left, top, plotW, plotH, maxMinutes, AMBULANCE_LIMIT_MIN, new Color(220, 38, 38), "Ambulance limit 15 min");
                drawLimit(g2, left, top, plotW, plotH, maxMinutes, WALK_IN_LIMIT_MIN, new Color(234, 88, 12), "Walk-in limit 30 min");
                drawSeries(g2, ambulanceSeconds, left, top, plotW, plotH, maxMinutes, new Color(37, 99, 235));
                drawSeries(g2, walkInSeconds, left, top, plotW, plotH, maxMinutes, new Color(22, 163, 74));
                drawLabels(g2, left, top, plotW, plotH, points);
            } finally {
                g2.dispose();
            }
        }

        private double maxMinutes(List<Double> first, List<Double> second) {
            double max = 0.0;
            for (double value : first) {
                max = Math.max(max, value / 60.0);
            }
            for (double value : second) {
                max = Math.max(max, value / 60.0);
            }
            return max;
        }

        private void drawGrid(Graphics2D g2, int left, int top, int plotW, int plotH, double maxMinutes) {
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
            for (int i = 0; i <= 6; i++) {
                double value = maxMinutes * i / 6.0;
                int y = top + plotH - (int) Math.round((value / maxMinutes) * plotH);
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(left, y, left + plotW, y);
                g2.setColor(new Color(71, 85, 105));
                g2.drawString(String.format("%.0f", value), left - 42, y + 4);
            }
        }

        private void drawLimit(Graphics2D g2, int left, int top, int plotW, int plotH, double maxMinutes, double limit, Color color, String label) {
            int y = top + plotH - (int) Math.round((limit / maxMinutes) * plotH);
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{7, 5}, 0));
            g2.setColor(color);
            g2.drawLine(left, y, left + plotW, y);
            g2.setStroke(oldStroke);
            g2.drawString(label, left + plotW - 150, y - 4);
        }

        private void drawSeries(Graphics2D g2, List<Double> seconds, int left, int top, int plotW, int plotH, double maxMinutes, Color color) {
            if (seconds.size() < 2) {
                return;
            }

            int maxIndex = seconds.size() - 1;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f));
            int prevX = left;
            int prevY = yFor(seconds.get(0), top, plotH, maxMinutes);

            for (int i = 1; i < seconds.size(); i++) {
                int x = left + (int) Math.round((i / (double) maxIndex) * plotW);
                int y = yFor(seconds.get(i), top, plotH, maxMinutes);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        private int yFor(double seconds, int top, int plotH, double maxMinutes) {
            double minutes = seconds / 60.0;
            return top + plotH - (int) Math.round((minutes / maxMinutes) * plotH);
        }

        private void drawLabels(Graphics2D g2, int left, int top, int plotW, int plotH, int points) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Stabilization of average waiting time for start of doctor examination", left, 18);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            g2.drawString("Replications", left + plotW / 2 - 35, top + plotH + 40);
            g2.rotate(-Math.PI / 2);
            g2.drawString("Minutes", -(top + plotH / 2 + 18), 22);
            g2.rotate(Math.PI / 2);

            g2.setColor(new Color(37, 99, 235));
            g2.fillRect(left + 8, top + 10, 12, 12);
            g2.drawString("Ambulance patients", left + 26, top + 21);

            g2.setColor(new Color(22, 163, 74));
            g2.fillRect(left + 160, top + 10, 12, 12);
            g2.drawString("Walk-in patients", left + 178, top + 21);

            g2.setColor(new Color(71, 85, 105));
            g2.drawString("1", left - 3, top + plotH + 22);
            g2.drawString(String.valueOf(points), left + plotW - 8, top + plotH + 22);
        }
    }
}
