package ui;

import core.fst.mealy.MealyMachineAutomata;
import core.fst.mealy.MealyMachineFunctions;
import core.fst.mealy.MealyMachineState;
import core.fst.mealy.MealyMachineTransition;
import core.fst.moore.MooreMachineAutomata;

import javax.swing.*;

public class MealyMachineCanvas extends  AbstractAutomatonCanvas<MealyMachineState, MealyMachineTransition, MealyMachineAutomata> {
    public MealyMachineCanvas(MealyMachineAutomata automaton) {
        super(automaton);
    }

    @Override
    protected MealyMachineState createNewState(int x, int y) {
        String defaultName = automaton.generateUniqueStateName("q");
        String nameStr = JOptionPane.showInputDialog(this, "Enter State Name:", defaultName);
        if (nameStr == null) {
            return null;
        }
        saveState();
        nameStr = nameStr.trim().isEmpty() ? defaultName : automaton.generateUniqueStateName(nameStr.trim());
        return new MealyMachineState(nameStr, x, y);
    }

    @Override
    protected MealyMachineState copyState(MealyMachineState original) {
        return new MealyMachineState(automaton.generateUniqueStateName(original.getName()), original.x, original.y);
    }

    @Override
    protected void editState(MealyMachineState state) {
        String newName = JOptionPane.showInputDialog(this, "Edit State:", state.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            if (!newName.equals(state.getName())) {
                state.setName(automaton.generateUniqueStateName(newName.trim()));
            }
        }
    }

    @Override
    protected MealyMachineTransition createNewTransition(MealyMachineState start, MealyMachineState end) {
        JTextField inSymbolField = new JTextField();
        JTextField outSymbolField = new JTextField();

        Object[] message = {
                "Enter transition symbol:", inSymbolField,
                "Enter output symbol:", outSymbolField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = inSymbolField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (inSymbolStr != null && !inSymbolStr.isEmpty() &&
                    outSymbolStr != null && !outSymbolStr.isEmpty()) {

                return new MealyMachineTransition(start, end, inSymbolStr.charAt(0), outSymbolStr.charAt(0));
            } else {
                showErrorMessage("Empty transitions are not allowed for this automaton", "Couldn't Create Transition");
                return null;
            }
        }

        return null;
    }

    @Override
    protected MealyMachineTransition copyTransition(MealyMachineTransition original, MealyMachineState newStart, MealyMachineState newEnd) {
        return new MealyMachineTransition(newStart, newEnd, original.getInChar(), original.getOutChar());
    }

    @Override
    protected MealyMachineTransition editTransition(MealyMachineTransition transition) {
        JTextField inSymbolField = new JTextField(String.valueOf(transition.getInChar()));
        JTextField outSymbolField = new JTextField(String.valueOf(transition.getOutChar()));

        Object[] message = {
                "Enter transition symbol:", inSymbolField,
                "Enter output symbol:", outSymbolField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = inSymbolField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (inSymbolStr != null && !inSymbolStr.isEmpty() &&
                    outSymbolStr != null && !outSymbolStr.isEmpty()) {
                return new MealyMachineTransition(transition.getStart(), transition.getEnd(), inSymbolStr.charAt(0), outSymbolStr.charAt(0));
            }
            else {
                showErrorMessage("Empty transitions are not allowed for this automaton", "Couldn't Create Transition");
                return null;
            }
        }

        return null;
    }

    @Override
    protected void addContextMenuExtras(JPopupMenu popup, MealyMachineState state) {
        return;
    }

    @Override
    protected boolean isStateFinal(MealyMachineState state) {
        return false;
    }


    @Override
    public void populateFunctionsMenu(JMenu functionsMenu, JFlatMainWindow mainWindow) {
        JMenuItem outputItem = createGetOutputMenuItem();
        JMenuItem convertToMooreItem = createConvertToMooreMenuItem(mainWindow);

        functionsMenu.add(outputItem);
        functionsMenu.add(convertToMooreItem);
    }

    private JMenuItem createConvertToMooreMenuItem(JFlatMainWindow mainWindow) {
        JMenuItem convertToMooreItem = new JMenuItem("Convert to Moore Machine");

        convertToMooreItem.addActionListener(e -> {
            if (!isAutomatonValidFor("Moore Machine Conversion")) {
                return;
            }

            try {
                MooreMachineAutomata result = MealyMachineFunctions.convertToMoore(automaton);
                mainWindow.addAutomatonTab(result.getName(), new MooreMachineCanvas(result));
            } catch (Exception ex) {
                showErrorMessage(
                        "Error converting to Moore:\n" + ex.getMessage(),
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
                String output = MealyMachineFunctions.getOutput(automaton, inputString);
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
