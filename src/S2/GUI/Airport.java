package S2.GUI;

import javax.swing.*;
import java.awt.*;

public class Airport {

    private JPanel panel1;
    private JTabbedPane tabbedPane1;

    public Airport() {
        JFrame frame = new JFrame("Airport");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel1 = new JPanel(new BorderLayout());
        tabbedPane1 = new JTabbedPane();

        JPanel general = new JPanel();
        general.add(new JLabel("General tab"));

        JPanel tab2 = new JPanel();
        tab2.add(new JLabel("Tab 2"));

        JPanel tab3 = new JPanel();
        tab3.add(new JLabel("Tab 3"));

        tabbedPane1.addTab("General", general);
        tabbedPane1.addTab("Rontgen", tab2);
        tabbedPane1.addTab("Metal detectors", tab3);

        panel1.add(tabbedPane1, BorderLayout.CENTER);
        frame.setContentPane(panel1);

        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JPanel getPanel1 () {
        return panel1;
    }
}
