import jflat.ui.JFlatMainWindow;

import javax.swing.*;

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFlatMainWindow mainWindow = new JFlatMainWindow();
        mainWindow.showWindow();
    });
}