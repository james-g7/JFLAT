package ui;

import core.fsa.FSAAutomata;
import core.fsa.FSAFunctions;
import core.fsa.FSAState;
import core.fsa.FSATransition;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FSAAutomatonCanvas extends AbstractAutomatonCanvas<FSAState, FSATransition, FSAAutomata> {

    public FSAAutomatonCanvas(FSAAutomata automaton) {
        super(automaton);
    }

    @Override
    protected FSAState createNewState(int x, int y) {
        String defaultName = automaton.generateUniqueStateName("q");
        String nameStr = JOptionPane.showInputDialog(this, "Enter State Name:", defaultName);
        if (nameStr == null) {
            return null;
        }
        saveState();
        nameStr = nameStr.trim().isEmpty() ? defaultName : automaton.generateUniqueStateName(nameStr.trim());
        return new FSAState(false, nameStr, x, y);
    }

    @Override
    protected FSAState copyState(FSAState original) {
        return new FSAState(original.isFinal(), automaton.generateUniqueStateName(original.getName()), original.x, original.y);
    }

    @Override
    protected void editState(FSAState state) {
        String newName = JOptionPane.showInputDialog(this, "Edit State:", state.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            String trimmedName = newName.trim();
            if (!trimmedName.equals(state.getName())) {
                saveState();
                state.setName(automaton.generateUniqueStateName(trimmedName));
                repaint();
            }
        }
    }

    @Override
    protected FSATransition createNewTransition(FSAState start, FSAState end) {
        String symbolStr = JOptionPane.showInputDialog(this,
                "Enter transition symbol\n(Leave blank for Lambda λ):",
                "New Transition",
                JOptionPane.PLAIN_MESSAGE);

        if (symbolStr != null) {
            Character symbol = symbolStr.isEmpty() ? null : symbolStr.charAt(0);
            return new FSATransition(start, end, symbol);
        }

        return null;
    }

    @Override
    protected FSATransition copyTransition(FSATransition original, FSAState newStart, FSAState newEnd) {
        return new FSATransition(newStart, newEnd, original.getSymbol());
    }

    @Override
    protected FSATransition editTransition(FSATransition transition) {
        String currentSym = transition.getSymbol() == null ? "" : transition.getSymbol().toString();
        String newSym = JOptionPane.showInputDialog(this,
                "Edit Transition Symbol\n(Leave blank for Lambda λ):", currentSym);

        if (newSym != null) {
            Character symbol = newSym.isEmpty() ? null : newSym.charAt(0);
            return new FSATransition(transition.getStart(), transition.getEnd(), symbol);
        }

        return null;
    }

    @Override
    protected void addContextMenuExtras(JPopupMenu popup, FSAState state) {
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
    protected boolean isStateFinal(FSAState state) {
        return state.isFinal();
    }

    @Override
    public void populateFunctionsMenu(JMenu functionsMenu, JFlatMainWindow mainWindow) {
        JMenuItem dfaItem = new JMenuItem("Convert to DFA");
        dfaItem.addActionListener(e -> applyUnaryFunction(mainWindow, "DFA Conversion", FSAFunctions::convertToDFA, false));

        JMenuItem complementItem = new JMenuItem("Complement");
        complementItem.addActionListener(e -> applyUnaryFunction(mainWindow, "Complement", FSAFunctions::getComplement, true));

        JMenuItem kleeneItem = new JMenuItem("Kleene Star");
        kleeneItem.addActionListener(e -> applyUnaryFunction(mainWindow, "Kleene Star", FSAFunctions::getKleeneStar, false));

        JMenuItem reverseItem = new JMenuItem("Reversal");
        reverseItem.addActionListener(e -> applyUnaryFunction(mainWindow, "Reversal", FSAFunctions::getReversal, false));

        JMenuItem minimiseItem = new JMenuItem("Minimise");
        minimiseItem.addActionListener(e -> applyUnaryFunction(mainWindow, "Minimised", FSAFunctions::minimise, true));

        JMenuItem unionItem = new JMenuItem("Union");
        unionItem.addActionListener(e -> applyMultipleFunction(mainWindow, "Union", FSAFunctions::getUnion));

        JMenuItem intersectItem = new JMenuItem("Intersection");
        intersectItem.addActionListener(e -> applyMultipleFunction(mainWindow, "Intersection", FSAFunctions::getIntersection));

        JMenuItem concatItem = new JMenuItem("Concatenation");
        concatItem.addActionListener(e -> applyMultipleFunction(mainWindow, "Concatenation", FSAFunctions::getConcatenation));

        JMenuItem equivalentItem = new JMenuItem("Check Equivalence");
        equivalentItem.addActionListener(e -> handleEquivalence(mainWindow));

        JMenuItem testInputItem = new JMenuItem("Test Input String");
        testInputItem.addActionListener(e -> handleTestInput());

        functionsMenu.add(dfaItem);
        functionsMenu.add(minimiseItem);
        functionsMenu.addSeparator();
        functionsMenu.add(complementItem);
        functionsMenu.add(kleeneItem);
        functionsMenu.add(reverseItem);
        functionsMenu.addSeparator();
        functionsMenu.add(unionItem);
        functionsMenu.add(intersectItem);
        functionsMenu.add(concatItem);
        functionsMenu.addSeparator();
        functionsMenu.add(equivalentItem);
        functionsMenu.add(testInputItem);
    }

    private FSAAutomata ensureDeterministic(JFlatMainWindow mainWindow, FSAAutomata fsa, String actionName) {
        if (fsa.isDeterministic()) return fsa;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "The '" + actionName + "' operation requires a Deterministic Finite Automaton (DFA).\n" +
                        "The selected automaton is non-deterministic (NFA).\n\n" +
                        "Would you like to automatically convert it to a DFA, open the intermediate result in a new tab, and proceed?",
                "DFA Conversion Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                FSAAutomata dfa = FSAFunctions.convertToDFA(fsa);
                mainWindow.addAutomatonTab(dfa.getName(), new FSAAutomatonCanvas(dfa));
                return dfa;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error converting to DFA:\n" + ex.getMessage(), "Conversion Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    private void applyUnaryFunction(JFlatMainWindow mainWindow, String actionName, Function<FSAAutomata, FSAAutomata> function, boolean requiresDFA) {
        if (!automaton.isValid()) {
            JOptionPane.showMessageDialog(this, "This automaton is not valid. Cannot perform " + actionName + ".", "Invalid Automata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FSAAutomata currentFsa = automaton;
        if (requiresDFA) {
            currentFsa = ensureDeterministic(mainWindow, currentFsa, actionName);
            if (currentFsa == null) return;
        }

        try {
            FSAAutomata result = function.apply(currentFsa);
            mainWindow.addAutomatonTab(result.getName(), new FSAAutomatonCanvas(result));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error executing " + actionName + ":\n" + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyMultipleFunction(JFlatMainWindow mainWindow, String actionName, Function<List<FSAAutomata>, FSAAutomata> function) {
        if (!automaton.isValid()) {
            JOptionPane.showMessageDialog(this, "The base automaton is not valid.", "Invalid Automata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<FSAAutomata> additionalFsas = promptForMultipleFSAs(mainWindow, actionName);

        if (additionalFsas == null || additionalFsas.isEmpty()) {
            return;
        }

        try {
            List<FSAAutomata> allAutomata = new ArrayList<>();

            allAutomata.add(automaton);
            allAutomata.addAll(additionalFsas);
            FSAAutomata result = function.apply(allAutomata);

            mainWindow.addAutomatonTab(result.getName(), new FSAAutomatonCanvas(result));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error executing " + actionName + ":\n" + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEquivalence(JFlatMainWindow mainWindow) {
        if (!automaton.isValid()) {
            JOptionPane.showMessageDialog(this, "This automaton is not valid. Cannot check equivalence.", "Invalid Automata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FSAAutomata fsa2 = promptForSecondFSA(mainWindow, "Equivalence Check");
        if (fsa2 == null) return;

        FSAAutomata fsa1 = ensureDeterministic(mainWindow, automaton, "Equivalence Check");
        if (fsa1 == null) return;

        fsa2 = ensureDeterministic(mainWindow, fsa2, "Equivalence Check");
        if (fsa2 == null) return;

        try {
            boolean isEquivalent = FSAFunctions.equivalent(fsa1, fsa2);
            String message = isEquivalent ? "The automata ARE equivalent." : "The automata are NOT equivalent.";
            int icon = isEquivalent ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            JOptionPane.showMessageDialog(this, message, "Equivalence Result", icon);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error executing Equivalence Check:\n" + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTestInput() {
        if (!automaton.isValid()) {
            JOptionPane.showMessageDialog(this, "This automaton is not valid. Cannot test input.", "Invalid Automata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String inputString = JOptionPane.showInputDialog(this, "Enter the string to test against the automaton:", "Test Input", JOptionPane.QUESTION_MESSAGE);
        if (inputString == null) return;

        try {
            List<List<FSATransition>> successfulPaths = FSAFunctions.testInput(inputString, automaton);
            if (successfulPaths.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The input string was REJECTED.\nNo valid paths found.", "Result", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "The input string was ACCEPTED!\nFound " + successfulPaths.size() + " distinct successful path(s).", "Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error testing input:\n" + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private FSAAutomata promptForSecondFSA(JFlatMainWindow mainWindow, String actionName) {
        List<FSAAutomata> availableAutomata = new ArrayList<>();
        List<String> availableNames = new ArrayList<>();
        JTabbedPane tabbedPane = mainWindow.getTabbedPane();

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tab = tabbedPane.getComponentAt(i);
            if (tab instanceof FSAAutomatonCanvas canvas) {
                if (canvas.getAutomaton().isValid() && canvas != this) {
                    availableAutomata.add(canvas.getAutomaton());
                    availableNames.add(tabbedPane.getTitleAt(i));
                }
            }
        }

        if (availableAutomata.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other valid FSA open to merge with.", "Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        record NamedAutomaton(String name, FSAAutomata fsa) {
            @Override public String toString() { return name; }
        }

        NamedAutomaton[] choices = new NamedAutomaton[availableAutomata.size()];
        for (int i = 0; i < availableAutomata.size(); i++) {
            choices[i] = new NamedAutomaton(availableNames.get(i), availableAutomata.get(i));
        }

        NamedAutomaton selected = (NamedAutomaton) JOptionPane.showInputDialog(
                this,
                "Select the second automaton to use for " + actionName + ":",
                "Select Automaton",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                choices[0]
        );

        return selected != null ? selected.fsa() : null;
    }

    private List<FSAAutomata> promptForMultipleFSAs(JFlatMainWindow mainWindow, String actionName) {
        List<FSAAutomata> availableAutomata = new ArrayList<>();
        List<String> availableNames = new ArrayList<>();
        JTabbedPane tabbedPane = mainWindow.getTabbedPane();

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tab = tabbedPane.getComponentAt(i);
            if (tab instanceof FSAAutomatonCanvas canvas) {
                if (canvas.getAutomaton().isValid() && canvas.getAutomaton() != this.automaton) {
                    availableAutomata.add(canvas.getAutomaton());
                    availableNames.add(tabbedPane.getTitleAt(i));
                }
            }
        }

        if (availableAutomata.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow,
                    "No other valid automata available to select.",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        record NamedAutomaton(String name, FSAAutomata fsa) {
            @Override
            public String toString() {
                return name;
            }
        }

        DefaultListModel<NamedAutomaton> listModel = new DefaultListModel<>();
        for (int i = 0; i < availableAutomata.size(); i++) {
            listModel.addElement(new NamedAutomaton(availableNames.get(i), availableAutomata.get(i)));
        }

        JList<NamedAutomaton> fsaList = new JList<>(listModel);
        fsaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fsaList.setVisibleRowCount(5);

        JScrollPane scrollPane = new JScrollPane(fsaList);

        int result = JOptionPane.showConfirmDialog(
                mainWindow,
                scrollPane,
                "Select Automata for " + actionName + " (Ctrl/Cmd-click for multiple)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<NamedAutomaton> selected = fsaList.getSelectedValuesList();
            if (selected.isEmpty()) {
                return null;
            }

            List<FSAAutomata> selectedFSAs = new ArrayList<>();
            for (NamedAutomaton item : selected) {
                selectedFSAs.add(item.fsa());
            }
            return selectedFSAs;
        }

        return null;
    }
}