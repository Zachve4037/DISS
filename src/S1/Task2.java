package S1;

import S1.GUI.RouteDeadlineGUI;

import javax.swing.*;

public class Task2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RouteDeadlineGUI gui = new RouteDeadlineGUI();
            gui.setVisible(true);
        });
    }
}
