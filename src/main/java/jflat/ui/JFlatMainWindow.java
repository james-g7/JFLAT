package jflat.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JFlatMainWindow extends JFrame {

    private JTabbedPane tabbedPane;
    private JMenu functionsMenu;

    public JFlatMainWindow() {
        setTitle("JFLAT Workspace");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel.");
        }

        initComponents();
    }

    private void initComponents() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // --- File Menu ---
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New Automaton");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newItem.addActionListener(this::handleNewTab);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openItem.addActionListener(e -> openFile());

        // --- NEW: Close Tab Action ---
        JMenuItem closeTabItem = new JMenuItem("Close Tab");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        closeTabItem.addActionListener(e -> closeCurrentTab());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveItem.addActionListener(e -> saveCurrentTab(false));

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> saveCurrentTab(true));

        JMenuItem exportItem = new JMenuItem("Export");
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportItem.addActionListener(e -> exportCurrentTab());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(closeTabItem); // Added to menu
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(exportItem);

        // --- View Menu ---
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem nextTabItem = new JMenuItem("Next Tab");
        nextTabItem.addActionListener(e -> cycleTabs(1));

        JMenuItem prevTabItem = new JMenuItem("Previous Tab");
        prevTabItem.addActionListener(e -> cycleTabs(-1));

        JMenuItem detangleItem = new JMenuItem("Detangle layout");
        detangleItem.addActionListener(e -> triggerDetangle());

        viewMenu.add(nextTabItem);
        viewMenu.add(prevTabItem);
        viewMenu.add(detangleItem);

        // --- Functions Menu (Dynamic) ---
        functionsMenu = new JMenu("Functions");
        functionsMenu.setMnemonic(KeyEvent.VK_U);
        functionsMenu.setEnabled(false);

        // --- Help Menu ---
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem websiteItem = new JMenuItem("JFLAT Website");
        websiteItem.addActionListener(e -> openWebpage());
        helpMenu.add(websiteItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(functionsMenu);
        menuBar.add(helpMenu);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Listen for tab changes to trigger polymorphism
        tabbedPane.addChangeListener(e -> updateFunctionsMenu());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    //       DYNAMIC MENU GENERATION
    // ==========================================

    private void updateFunctionsMenu() {
        functionsMenu.removeAll();

        Component selectedTab = tabbedPane.getSelectedComponent();
        if (!(selectedTab instanceof AbstractAutomatonCanvas<?, ?, ?> canvas)) {
            functionsMenu.setEnabled(false);
            return;
        }

        functionsMenu.setEnabled(true);

        // DELEGATE TO THE SPECIFIC CANVAS VIA POLYMORPHISM
        canvas.populateFunctionsMenu(functionsMenu, this);

        // Fallback if the canvas adds no functions
        if (functionsMenu.getItemCount() == 0) {
            JMenuItem noFunctionsItem = new JMenuItem("No functions available for this type.");
            noFunctionsItem.setEnabled(false);
            functionsMenu.add(noFunctionsItem);
        }

        functionsMenu.revalidate();
        functionsMenu.repaint();
    }

    // ==========================================
    //          GENERAL WINDOW METHODS
    // ==========================================

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void addAutomatonTab(String tabTitle, AbstractAutomatonCanvas<?, ?, ?> canvas) {
        tabbedPane.addTab(tabTitle, canvas);
        tabbedPane.setSelectedComponent(canvas);
    }

    private void handleNewTab(ActionEvent e) {
        NewAutomatonDialog dialog = new NewAutomatonDialog(this);
        dialog.setVisible(true);

        AbstractAutomatonCanvas<?, ?, ?> newCanvas = dialog.getCreatedCanvas();
        if (newCanvas != null) {
            String title = dialog.getSelectedTitle();
            addAutomatonTab(title, newCanvas);
        }
    }

    private void closeCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            tabbedPane.remove(selectedIndex);
        }
    }

    private void cycleTabs(int direction) {
        int tabCount = tabbedPane.getTabCount();
        if (tabCount <= 1) return;

        int currentIndex = tabbedPane.getSelectedIndex();
        int nextIndex = (currentIndex + direction + tabCount) % tabCount;
        tabbedPane.setSelectedIndex(nextIndex);
    }

    private void triggerDetangle() {
        java.awt.Component selectedTab = tabbedPane.getSelectedComponent();
        if (selectedTab instanceof AbstractAutomatonCanvas<?, ?, ?> activeCanvas) {
            activeCanvas.detangleLayout();
        }
    }

    private void openWebpage() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI("https://github.com"));
            } else {
                JOptionPane.showMessageDialog(this, "Opening web links is not supported by your operating system.", "Browser Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open the website.\nError: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showWindow() {
        setVisible(true);
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JFLAT Automata Files (*.jflat)", "jflat");
        chooser.setFileFilter(filter);
        return chooser;
    }

    private void saveCurrentTab(boolean forceSaveAs) {
    }

    private void openFile() {
    }

    private void exportCurrentTab() {
        Component selectedTab = tabbedPane.getSelectedComponent();

        if (!(selectedTab instanceof AbstractAutomatonCanvas<?, ?, ?> canvas)) return;
        if (!canvas.getAutomaton().isValid()) {
            JOptionPane.showMessageDialog(this, "This automaton is not valid (must have only 1 initial state) cannot export.", "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ExportDialog dialog = new ExportDialog(this, canvas);
        dialog.setVisible(true);
    }
}
