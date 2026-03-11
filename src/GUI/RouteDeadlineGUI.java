package GUI;

import MonteCarlo.RouteDeadlineAnalysisService;

import javax.swing.*;
import java.awt.*;

public class RouteDeadlineGUI extends JFrame {
    private static final String[] ROUTES = {
            "Z_S_RT_D_Z",
            "Z_S_D_RT_Z",
            "Z_RT_S_D_Z",
            "Z_RT_D_S_Z",
            "Z_D_S_RT_Z",
            "Z_D_RT_S_Z"
    };

    private final JComboBox<String> routeComboBox = new JComboBox<>(ROUTES);
    private final JTextField startTimeField = new JTextField("06:00:00", 10);
    private final JButton probabilityButton = new JButton("Vyhodnotit % stihnutia");
    private final JButton latest80Button = new JButton("Najst najneskorsi start");

    private final JLabel deadlineLabel = new JLabel("Deadline: 07:35:00");
    private final JLabel replicationsLabel = new JLabel("Replikacie: " + RouteDeadlineAnalysisService.REPLICATIONS);
    private final JLabel probabilityLabel = new JLabel("Pravdepodobnost: -");
    private final JLabel latestStartLabel = new JLabel("Najneskorsi start pre 80%: -");
    private final JLabel statusLabel = new JLabel(" ");

    private final RouteDeadlineAnalysisService service = new RouteDeadlineAnalysisService();

    public RouteDeadlineGUI() {
        setTitle("Uloha 2 - Deadline 07:35");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 280);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        root.add(new JLabel("Trasa:"), c);
        c.gridx = 1;
        root.add(routeComboBox, c);

        c.gridx = 0;
        c.gridy = 1;
        root.add(new JLabel("Start (HH:mm alebo HH:mm:ss):"), c);
        c.gridx = 1;
        root.add(startTimeField, c);

        c.gridx = 0;
        c.gridy = 2;
        root.add(probabilityButton, c);
        c.gridx = 1;
        root.add(latest80Button, c);

        c.gridx = 0;
        c.gridy = 3;
        root.add(deadlineLabel, c);
        c.gridx = 1;
        root.add(replicationsLabel, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        root.add(probabilityLabel, c);

        c.gridy = 5;
        root.add(latestStartLabel, c);

        c.gridy = 6;
        statusLabel.setForeground(new Color(0, 70, 140));
        root.add(statusLabel, c);

        add(root, BorderLayout.CENTER);

        wireActions();
    }

    private void wireActions() {
        probabilityButton.addActionListener(e -> runProbability());
        latest80Button.addActionListener(e -> runBisection80());
    }

    private void runProbability() {
        final String route = (String) routeComboBox.getSelectedItem();
        final int startSeconds;

        try {
            startSeconds = RouteDeadlineAnalysisService.parseTimeToSeconds(startTimeField.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Neplatny cas", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        statusLabel.setText("Pocitam pravdepodobnost...");

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() {
                return service.estimateOnTimeProbability(route, startSeconds);
            }

            @Override
            protected void done() {
                try {
                    double p = get();
                    probabilityLabel.setText(String.format(
                            "Pravdepodobnost pre start %s: %.2f%%",
                            RouteDeadlineAnalysisService.formatHHmmss(startSeconds),
                            p * 100.0
                    ));
                    statusLabel.setText("Hotovo.");
                } catch (Exception ex) {
                    statusLabel.setText("Chyba: " + ex.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void runBisection80() {
        final String route = (String) routeComboBox.getSelectedItem();

        setControlsEnabled(false);
        statusLabel.setText("Pocitam najneskorsi start cez P80 (bisekcia)...");

        SwingWorker<RouteDeadlineAnalysisService.PercentileSearchResult, Void> worker = new SwingWorker<>() {
            @Override
            protected RouteDeadlineAnalysisService.PercentileSearchResult doInBackground() {
                return service.findLatestStartByPercentile(route, RouteDeadlineAnalysisService.TARGET_PROBABILITY);
            }

            @Override
            protected void done() {
                try {
                    RouteDeadlineAnalysisService.PercentileSearchResult result = get();

                    if (!result.exists()) {
                        latestStartLabel.setText("Najneskorsi start pre P80 <= 07:35:00: neexistuje.");
                    } else {
                        latestStartLabel.setText(String.format(
                                "Najneskorsi start (P80): %s | P80 prichod: %s",
                                RouteDeadlineAnalysisService.formatHHmmss(result.latestStartSeconds()),
                                RouteDeadlineAnalysisService.formatHHmmss(result.percentileArrivalSeconds())
                        ));
                    }

                    statusLabel.setText("Hotovo.");
                } catch (Exception ex) {
                    statusLabel.setText("Chyba: " + ex.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        };

        worker.execute();
    }


    private void setControlsEnabled(boolean enabled) {
        routeComboBox.setEnabled(enabled);
        startTimeField.setEnabled(enabled);
        probabilityButton.setEnabled(enabled);
        latest80Button.setEnabled(enabled);
    }
}
