package ui;

import core.pda.emptyStack.EmptyStackPushdownAutomata;
import core.pda.emptyStack.EmptyStackPushdownState;
import core.pda.emptyStack.EmptyStackPushdownTransition;

import javax.swing.*;
import java.util.Objects;

public class EmptyStackPushdownAutomataCanvas extends AbstractAutomatonCanvas<EmptyStackPushdownState, EmptyStackPushdownTransition, EmptyStackPushdownAutomata> {
    EmptyStackPushdownAutomataCanvas(EmptyStackPushdownAutomata automaton) {
        super(automaton);
    }

    @Override
    protected EmptyStackPushdownState instantiateState(int x, int y, String name) {
        return new EmptyStackPushdownState(name, x, y);
    }

    @Override
    protected EmptyStackPushdownState copyState(EmptyStackPushdownState original) {
        return new EmptyStackPushdownState(original.getName(), original.x, original.y);
    }

    @Override
    protected EmptyStackPushdownTransition createNewTransition(EmptyStackPushdownState start, EmptyStackPushdownState end) {
        JTextField inSymbolField = new JTextField();
        JTextField popSymbolField = new JTextField();
        JTextField pushSymbolsField = new JTextField();

        Object[] message = {
                "Enter transition symbol\n(Leave blank for λ)", inSymbolField,
                "Enter pop symbol\n(Leave blank for λ, Type 'empty' for Z₀)", popSymbolField,
                "Enter push symbol(s) - max 2\n(Leave blank for λ, Type 'empty' for Z₀)", pushSymbolsField,
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "New Transition",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String inStr = inSymbolField.getText().trim();
            String popStr = popSymbolField.getText().trim();
            String pushStr = pushSymbolsField.getText().trim();

            if (inStr.isEmpty()) inStr = null;
            if (popStr.isEmpty()) popStr = null;
            if (pushStr.isEmpty()) pushStr = null;

            if (inStr != null && inStr.length() > 1) {
                showErrorMessage("Transition symbol must be a single character.", "Couldn't Create Transition");
                return null;
            }

            if (popStr != null && !popStr.equalsIgnoreCase("empty") && popStr.length() > 1) {
                showErrorMessage("Pop symbol must be a single character.", "Couldn't Create Transition");
                return null;
            }

            if (pushStr != null && !pushStr.equalsIgnoreCase("empty") && pushStr.length() > 2) {
                showErrorMessage("Push symbols must be max two characters.", "Couldn't Create Transition");
                return null;
            }

            Character inChar = (inStr == null) ? null : inStr.charAt(0);
            String finalPop = (popStr != null && popStr.equalsIgnoreCase("empty")) ? "empty" : popStr;
            String finalPush = (pushStr != null && pushStr.equalsIgnoreCase("empty")) ? "empty" : pushStr;

            saveState();

            return new EmptyStackPushdownTransition(
                    start,
                    end,
                    inChar,
                    finalPop,
                    finalPush
            );
        }

        return null;
    }

    @Override
    protected EmptyStackPushdownTransition copyTransition(EmptyStackPushdownTransition original, EmptyStackPushdownState newStart, EmptyStackPushdownState newEnd) {
        return new EmptyStackPushdownTransition(newStart, newEnd, original.getInSymbol(), original.getPopSymbol(), original.getPushSymbols());
    }

    @Override
    protected void editTransition(EmptyStackPushdownTransition transition) {
        String currentIn = transition.getInSymbol() == null ? "" : String.valueOf(transition.getInSymbol());
        String currentPop = transition.getPopSymbol() == null ? "" : transition.getPopSymbol();
        String currentPush = transition.getPushSymbols() == null ? "" : transition.getPushSymbols();

        JTextField inSymbolField = new JTextField(currentIn);
        JTextField popSymbolField = new JTextField(currentPop);
        JTextField pushSymbolsField = new JTextField(currentPush);

        Object[] message = {
                "Enter transition symbol\n(Leave blank for λ)", inSymbolField,
                "Enter pop symbol\n(Leave blank for λ, Type 'empty' for Z₀)", popSymbolField,
                "Enter push symbol(s) - max 2\n(Leave blank for λ, Type 'empty' for Z₀)", pushSymbolsField,
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Edit Transition",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String inStr = inSymbolField.getText().trim();
            String popStr = popSymbolField.getText().trim();
            String pushStr = pushSymbolsField.getText().trim();

            if (inStr.isEmpty()) inStr = null;
            if (popStr.isEmpty()) popStr = null;
            if (pushStr.isEmpty()) pushStr = null;

            if (inStr != null && inStr.length() > 1) {
                showErrorMessage("Transition symbol must be a single character.", "Couldn't Edit Transition");
                return;
            }

            if (popStr != null && !popStr.equalsIgnoreCase("empty") && popStr.length() > 1) {
                showErrorMessage("Pop symbol must be a single character.", "Couldn't Edit Transition");
                return;
            }

            if (pushStr != null && !pushStr.equalsIgnoreCase("empty") && pushStr.length() > 2) {
                showErrorMessage("Push symbols must be max two characters.", "Couldn't Edit Transition");
                return;
            }

            Character inChar = (inStr == null) ? null : inStr.charAt(0);
            String finalPop = (popStr != null && popStr.equalsIgnoreCase("empty")) ? "empty" : popStr;
            String finalPush = (pushStr != null && pushStr.equalsIgnoreCase("empty")) ? "empty" : pushStr;

            if (!Objects.equals(transition.getInSymbol(), inChar) ||
                    !Objects.equals(transition.getPopSymbol(), finalPop) ||
                    !Objects.equals(transition.getPushSymbols(), finalPush)) {

                saveState();
                transition.setInSymbol(inChar);
                transition.setPopSymbol(finalPop);
                transition.setPushSymbols(finalPush);
            }
        }
    }
}
