package ui;

import core.tm.TuringMachine1D;
import core.tm.TuringMachineState1D;
import core.tm.TuringMachineTransition1D;

import javax.swing.*;
import java.util.Objects;

public class TuringMachineCanvas extends AbstractAutomatonCanvas <TuringMachineState1D, TuringMachineTransition1D, TuringMachine1D>{
    public TuringMachineCanvas(TuringMachine1D automaton) {
        super(automaton);
    }

    @Override
    protected TuringMachineState1D instantiateState(int x, int y, String name) {
        return new TuringMachineState1D(name, x, y, false);
    }

    @Override
    protected TuringMachineState1D copyState(TuringMachineState1D original) {
        return new TuringMachineState1D(
                automaton.generateUniqueStateName(original.getName()),
                original.x,
                original.y,
                original.isFinal()
        );
    }

    @Override
    protected TuringMachineTransition1D instantiateTransition(TuringMachineState1D start, TuringMachineState1D end, Character symbol) {
        return null;
    }

    @Override
    protected TuringMachineTransition1D createNewTransition(TuringMachineState1D start, TuringMachineState1D end) {
        JTextField readSymbolField = new JTextField();
        JTextField writeSymbolField = new JTextField();
        JComboBox<TuringMachineTransition1D.Direction> directionJList = new JComboBox<>(TuringMachineTransition1D.Direction.values());

        Object[] message = {
                "Enter transition symbol\n(Leave blank for blank □)", readSymbolField,
                "Enter output symbol\n(Leave blank for blank □)", writeSymbolField,
                "Enter direction", directionJList,
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "New Transition",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = readSymbolField.getText();
            String outSymbolStr = writeSymbolField.getText();
            TuringMachineTransition1D.Direction direction = (TuringMachineTransition1D.Direction) directionJList.getSelectedItem();

            if (inSymbolStr != null && inSymbolStr.length() > 1 ) showErrorMessage("Transition symbols must be single characters", "Couldn't Create Transition");
            else if (outSymbolStr != null && outSymbolStr.length() > 1 ) showErrorMessage("Output symbols must be single characters", "Couldn't Create Transition");
            else if (direction == null) showErrorMessage("Invalid direction", "Couldn't Create Transition");
            else {
                saveState();
                return new TuringMachineTransition1D(
                        start,
                        end,
                        inSymbolStr == null || inSymbolStr.isEmpty() ? null : inSymbolStr.charAt(0),
                         outSymbolStr == null || outSymbolStr.isEmpty() ? null : outSymbolStr.charAt(0),
                        direction
                );
            }
        }
        return null;
    }

    @Override
    protected TuringMachineTransition1D copyTransition(TuringMachineTransition1D original, TuringMachineState1D newStart, TuringMachineState1D newEnd) {
        return new TuringMachineTransition1D(
                newStart,
                newEnd,
                original.getRead(),
                original.getWrite(),
                original.getDestination()
        );
    }

    @Override
    protected void editTransition(TuringMachineTransition1D transition) {
        JTextField readSymbolField = new JTextField(transition.getRead() == null ? "" : String.valueOf(transition.getRead()));
        JTextField writeSymbolField = new JTextField(transition.getWrite() == null ? "" : String.valueOf(transition.getWrite()));
        JComboBox<TuringMachineTransition1D.Direction> directionJList = new JComboBox<>(TuringMachineTransition1D.Direction.values());
        directionJList.setSelectedItem(transition.getDestination());

        Object[] message = {
                "Enter transition symbol\n(Leave blank for blank □)", readSymbolField,
                "Enter output symbol\n(Leave blank for blank □)", writeSymbolField,
                "Enter direction", directionJList,
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Edit Transition",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String inSymbolStr = readSymbolField.getText();
            String outSymbolStr = writeSymbolField.getText();
            TuringMachineTransition1D.Direction direction = (TuringMachineTransition1D.Direction) directionJList.getSelectedItem();

            if (inSymbolStr != null && inSymbolStr.length() > 1 ) showErrorMessage("Transition symbols must be single characters", "Couldn't Edit Transition");
            else if (outSymbolStr != null && outSymbolStr.length() > 1 ) showErrorMessage("Output symbols must be single characters", "Couldn't Edit Transition");
            else if (direction == null) showErrorMessage("Invalid direction", "Couldn't Edit Transition");
            else if (!(Objects.equals(inSymbolStr == null || inSymbolStr.isEmpty() ? null : inSymbolStr.charAt(0), transition.getRead()) &&
                    Objects.equals(outSymbolStr == null || outSymbolStr.isEmpty() ? null : outSymbolStr.charAt(0), transition.getWrite()) &&
                    Objects.equals(direction, transition.getDestination()))
            ){
                saveState();
                transition.setRead(inSymbolStr == null || inSymbolStr.isEmpty() ? null : inSymbolStr.charAt(0));
                transition.setWrite(outSymbolStr == null || outSymbolStr.isEmpty() ? null : outSymbolStr.charAt(0));
                transition.setDestination(direction);
            }
        }
    }

    @Override
    protected void addContextMenuExtras(JPopupMenu popup, TuringMachineState1D state) {
        JCheckBoxMenuItem finalItem = new JCheckBoxMenuItem("Final State");
        finalItem.setSelected(state.isFinal());
        finalItem.addActionListener(a -> {
            saveState();
            state.setFinal(finalItem.isSelected());
            repaint();
        });
        popup.add(finalItem);
    }

    @Override
    protected boolean isStateFinal(TuringMachineState1D state) {
        return state.isFinal();
    }
}
