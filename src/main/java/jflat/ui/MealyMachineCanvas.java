package ui;

import core.fst.mealy.MealyMachineAutomata;
import core.fst.mealy.MealyMachineFunctions;
import core.fst.mealy.MealyMachineState;
import core.fst.mealy.MealyMachineTransition;
import core.fst.moore.MooreMachineAutomata;

import javax.swing.*;
import java.util.Objects;

public class MealyMachineCanvas extends  AbstractAutomatonCanvas<MealyMachineState, MealyMachineTransition, MealyMachineAutomata> {
    public MealyMachineCanvas(MealyMachineAutomata automaton) {
        super(automaton);
    }

    @Override
    protected MealyMachineState instantiateState(int x, int y, String name) {
        return new MealyMachineState(name, x, y);
    }

    @Override
    protected MealyMachineState copyState(MealyMachineState original) {
        return new MealyMachineState(automaton.generateUniqueStateName(original.getName()), original.x, original.y);
    }

    @Override
    protected MealyMachineTransition createNewTransition(MealyMachineState start, MealyMachineState end) {
        JTextField inSymbolField = new JTextField();
        JTextField outSymbolField = new JTextField();

        Object[] message = {
                "Enter transition symbol", inSymbolField,
                "Enter output symbol:", outSymbolField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = inSymbolField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (inSymbolStr == null || inSymbolStr.isEmpty()) showErrorMessage("Transition symbols cannot be empty", "Couldn't Create Transition");
            else if (outSymbolStr == null || outSymbolStr.isEmpty()) showErrorMessage("Output cannot be empty", "Couldn't Create Transition");
            else if (inSymbolStr.length() != 1) showErrorMessage("Transition symbols must be single characters", "Couldn't Create Transition");
            else if (outSymbolStr.length() != 1) showErrorMessage("Output must be a single character", "Couldn't Create Transition");
            else {
                saveState();
                return new MealyMachineTransition(start, end, inSymbolStr.charAt(0), outSymbolStr.charAt(0));
            }
        }
        return null;
    }

    @Override
    protected MealyMachineTransition copyTransition(MealyMachineTransition original, MealyMachineState newStart, MealyMachineState newEnd) {
        return new MealyMachineTransition(newStart, newEnd, original.getInChar(), original.getOutChar());
    }

    @Override
    protected void editTransition(MealyMachineTransition transition) {
        JTextField inSymbolField = new JTextField(transition.getInChar());
        JTextField outSymbolField = new JTextField(transition.getOutChar());

        Object[] message = {
                "Enter transition symbol", inSymbolField,
                "Enter output symbol:", outSymbolField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Transition",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = inSymbolField.getText();
            String outSymbolStr = outSymbolField.getText();

            if (inSymbolStr == null || inSymbolStr.isEmpty()) showErrorMessage("Transition symbols cannot be empty", "Couldn't Edit Transition");
            else if (outSymbolStr == null || outSymbolStr.isEmpty()) showErrorMessage("Output cannot be empty", "Couldn't Edit Transition");
            else if (inSymbolStr.length() != 1) showErrorMessage("Transition symbols must be single characters", "Couldn't Edit Transition");
            else if (outSymbolStr.length() != 1) showErrorMessage("Output must be a single character", "Couldn't Edit Transition");
            else if (!(Objects.equals(inSymbolStr.charAt(0), transition.getInChar()) && Objects.equals(outSymbolStr.charAt(0), transition.getOutChar()))){
                saveState();
                transition.setInChar(inSymbolStr.charAt(0));
                transition.setOutChar(outSymbolStr.charAt(0));
            }
        }
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
