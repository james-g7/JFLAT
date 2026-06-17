package ui;

import core.fst.mealy.MealyMachineAutomata;
import core.fst.moore.MooreMachineAutomata;
import core.fst.moore.MooreMachineFunctions;
import core.fst.moore.MooreMachineState;
import core.fst.moore.MooreMachineTransition;

import javax.swing.*;
import java.util.Objects;

public class MooreMachineCanvas extends AbstractAutomatonCanvas <MooreMachineState, MooreMachineTransition, MooreMachineAutomata> {
    public MooreMachineCanvas(MooreMachineAutomata automaton) {
        super(automaton);
    }

    @Override
    protected MooreMachineState instantiateState(int x, int y, String name) {
        return null;
    }

    @Override
    protected MooreMachineState createNewState(int x, int y) {
        String defaultName = automaton.generateUniqueStateName("q");
        JTextField stateNameField = new JTextField(defaultName);
        JTextField outSymbolField = new JTextField();

        Object[] message = {
                "Enter State Name:", stateNameField,
                "Enter output symbol:", outSymbolField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New State",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String stateNameStr = stateNameField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (stateNameStr != null && !stateNameStr.isEmpty() &&
                    outSymbolStr != null && outSymbolStr.length() == 1) {

                return new MooreMachineState(stateNameStr, x, y, outSymbolStr.charAt(0));
            }
            showErrorMessage("Invalid output symbol", "Couldn't Create State");
        }

        return null;
    }

    @Override
    protected MooreMachineState copyState(MooreMachineState original) {
        return new MooreMachineState(automaton.generateUniqueStateName(original.getName()), original.x, original.y, original.getOutput());
    }

    @Override
    protected void editState(MooreMachineState state) {
        JTextField stateNameField = new JTextField(state.getName());
        JTextField outSymbolField = new JTextField(String.valueOf(state.getOutput()));

        Object[] message = {
                "Enter State Name:", stateNameField,
                "Enter output symbol:", outSymbolField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New State",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String newName = stateNameField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (newName == null || newName.isEmpty()) showErrorMessage("State names cannot be empty", "Couldn't Edit State");
            else if (!newName.equals(state.getName()) && !newName.equals(automaton.generateUniqueStateName(newName))) {
                showErrorMessage("State names must be unique", "Couldn't Edit State");
            } else if (outSymbolStr == null) showErrorMessage("Output cannot be empty", "Couldn't Edit State");
            else if (outSymbolStr.length() != 1) showErrorMessage("Output must be a single character", "Couldn't Edit State");
            else {
                saveState();
                state.setName(newName);
                repaint();
            }
        }
    }

    @Override
    protected MooreMachineTransition createNewTransition(MooreMachineState start, MooreMachineState end) {
        JTextField symbolField = new JTextField();

        Object[] message = {
                "Enter transition symbol", symbolField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String nameStr = symbolField.getText();

            if (nameStr == null || nameStr.isEmpty()) showErrorMessage("Transition symbols cannot be empty", "Couldn't Create Transition");
            else if (nameStr.length() != 1) showErrorMessage("Transition symbols must be single characters", "Couldn't Create Transition");
            else {
                saveState();
                return new MooreMachineTransition(start, end, nameStr.charAt(0));
            }
        }
        return null;
    }

    @Override
    protected MooreMachineTransition copyTransition(MooreMachineTransition original, MooreMachineState newStart, MooreMachineState newEnd) {
        return new MooreMachineTransition(newStart, newEnd, original.getSymbol());
    }

    @Override
    protected void editTransition(MooreMachineTransition transition) {
        JTextField symbolField = new JTextField(transition.getSymbol());

        Object[] message = {
                "Enter transition symbol", symbolField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String nameStr = symbolField.getText();

            if (nameStr == null || nameStr.isEmpty()) showErrorMessage("Transition symbols cannot be empty", "Couldn't Edit Transition");
            else if (nameStr.length() != 1) showErrorMessage("Transition symbols must be single characters", "Couldn't Edit Transition");
            else if (!Objects.equals(transition.getSymbol(), nameStr.charAt(0))) {
                saveState();
                transition.setSymbol(nameStr.charAt(0));
            }
        }
    }

    @Override
    public void populateFunctionsMenu(JMenu functionsMenu, JFlatMainWindow mainWindow) {
        JMenuItem outputItem = createGetOutputMenuItem();
        JMenuItem convertToMealyItem = createConvertToMealyMenuItem(mainWindow);

        functionsMenu.add(outputItem);
        functionsMenu.add(convertToMealyItem);
    }

    private JMenuItem createConvertToMealyMenuItem(JFlatMainWindow mainWindow) {
        JMenuItem convertToMooreItem = new JMenuItem("Convert to Mealy Machine");

        convertToMooreItem.addActionListener(e -> {
            if (!isAutomatonValidFor("Mealy Machine Conversion")) {
                return;
            }

            try {
                MealyMachineAutomata result = MooreMachineFunctions.convertToMealy(automaton);
                mainWindow.addAutomatonTab(result.getName(), new MealyMachineCanvas(result));
            } catch (Exception ex) {
                showErrorMessage(
                        "Error converting to Mealy:\n" + ex.getMessage(),
                        "Conversion Error"
                );
            }
        });

        return convertToMooreItem;
    }

    private JMenuItem createGetOutputMenuItem() {
        JMenuItem outputItem = new JMenuItem("Get Output");

        outputItem.addActionListener(e -> {
            if (!isAutomatonValidFor("Get Output")) {
                return;
            }

            String inputString = JOptionPane.showInputDialog(
                    this,
                    "Enter the string to get output off.",
                    "Input",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (inputString == null) {
                return;
            }

            try {
                String output = MooreMachineFunctions.getOutput(automaton, inputString);
                showOutputResult(output);
            } catch (Exception ex) {
                showErrorMessage(
                        "Error getting output:\n" + ex.getMessage(),
                        "Execution Error"
                );
            }
        });

        return outputItem;
    }

    private void showOutputResult(String output) {
        if (output == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No output found for the given input.",
                    "No Output",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Output: " + output,
                "Output",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
