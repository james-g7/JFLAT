package ui;

import core.fsa.FSAAutomata;
import core.fst.mealy.MealyMachineAutomata;
import core.fst.moore.MooreMachineAutomata;
import core.tm.TuringMachine1D;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NewAutomatonDialog extends JDialog {
    private AbstractAutomatonCanvas<?, ?, ?> createdCanvas = null;
    private String selectedTitle = "Untitled";

    private interface CanvasFactory {
        String getDisplayName();
        AbstractAutomatonCanvas<?, ?, ?> create(String name);
    }

    private final List<CanvasFactory> supportedAutomata = List.of(
            new CanvasFactory() {
                public String getDisplayName() {
                    return "Finite State Automaton (FSA)";
                }

                public AbstractAutomatonCanvas<?, ?, ?> create(String name) {
                    return new FSAAutomatonCanvas(new FSAAutomata(name));
                }
            },
            new CanvasFactory() {
                public String getDisplayName() {
                    return "Mealy Machine";
                }

                public AbstractAutomatonCanvas<?, ?, ?> create(String name) {
                    return new MealyMachineCanvas(new MealyMachineAutomata(name));
                }
            },
            new CanvasFactory() {
                public String getDisplayName() {
                    return "Moore Machine";
                }

                public AbstractAutomatonCanvas<?, ?, ?> create(String name) {
                    return new MooreMachineCanvas(new MooreMachineAutomata(name));
                }
            },
            new CanvasFactory() {
                public String getDisplayName() { return "Pushdown Automaton (PDA)"; }

                public AbstractAutomatonCanvas<?, ?, ?> create(String name) { return null; }
            },
            new CanvasFactory() {
                public String getDisplayName() { return "Turing Machine (TM)"; }

                public AbstractAutomatonCanvas<?, ?, ?> create(String name) {
                    return new TuringMachineCanvas(new TuringMachine1D(name));
                }
            }
    );

    public NewAutomatonDialog(JFrame parent) {
        super(parent, "Create New Automaton", true);
        setSize(350, 180);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        mainPanel.add(new JLabel("Automaton Type:"));

        // Populate the combo box dynamically from our registry
        String[] types = supportedAutomata.stream()
                .map(CanvasFactory::getDisplayName)
                .toArray(String[]::new);
        JComboBox<String> typeComboBox = new JComboBox<>(types);
        mainPanel.add(typeComboBox);

        mainPanel.add(new JLabel("Tab Name:"));
        JTextField nameField = new JTextField("Untitled");
        mainPanel.add(nameField);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");

        createBtn.addActionListener(e -> {
            selectedTitle = nameField.getText().trim();
            if (selectedTitle.isEmpty()) selectedTitle = "Untitled";

            // Grab the selected factory and let it create the correct Canvas/Automaton pair
            CanvasFactory selectedFactory = supportedAutomata.get(typeComboBox.getSelectedIndex());
            createdCanvas = selectedFactory.create(selectedTitle);

            if (createdCanvas == null) {
                JOptionPane.showMessageDialog(this,
                        selectedFactory.getDisplayName() + " not yet implemented!",
                        "WIP",
                        JOptionPane.WARNING_MESSAGE);
                return; // Return early so the dialog doesn't close
            }

            dispose(); // Close the dialog on success
        });

        cancelBtn.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(createBtn);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(createBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Main window will call this to grab the fully prepped Canvas UI
    public AbstractAutomatonCanvas<?, ?, ?> getCreatedCanvas() {
        return createdCanvas;
    }

    public String getSelectedTitle() {
        return selectedTitle;
    }
}