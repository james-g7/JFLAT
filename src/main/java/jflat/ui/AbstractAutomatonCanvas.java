package ui;

import core.generics.AbstractAutomata;
import core.generics.AbstractState;
import core.generics.AbstractTransition;
import core.tm.TuringMachineState1D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public abstract class AbstractAutomatonCanvas<
        S extends AbstractState<S, T>,
        T extends AbstractTransition<S, T>,
        A extends AbstractAutomata<S, T>> extends JPanel {

    protected final A automaton;
    protected final int STATE_RADIUS = 20;

    protected java.io.File currentFile = null;

    protected double scale = 1.0;
    protected double translateX = 0.0;
    protected double translateY = 0.0;

    protected final Set<S> selectedStates = new HashSet<>();
    protected final Set<T> selectedTransitions = new HashSet<>();

    protected Point selectionStartPoint = null;
    protected Rectangle2D.Double selectionBox = null;

    protected S lastClickedState = null;
    protected final Point currentMouseWorldPos = new Point(0, 0);

    protected final Set<S> clipboardStates = new HashSet<>();
    protected final Set<T> clipboardTransitions = new HashSet<>();
    protected S clipboardInitialState = null;
    protected boolean isPastingMode = false;
    protected final Point clipboardCenter = new Point(0, 0);

    protected final Stack<AutomatonSnapshot<S, T>> undoStack = new Stack<>();
    protected final Stack<AutomatonSnapshot<S, T>> redoStack = new Stack<>();

    public enum Mode { SELECT, ADD_STATE, ADD_TRANSITION }
    protected Mode currentMode = Mode.SELECT;
    protected S transitionStart = null;
    protected Point tempTransitionEnd = null;

    private final AutomatonRenderer<S, T, A> renderer;

    public AbstractAutomatonCanvas(A automaton) {
        this.automaton = automaton;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        this.renderer = new AutomatonRenderer<>(this);
        AutomatonMouseHandler<S, T, A> mouseHandler = new AutomatonMouseHandler<>(this);

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        setupToolbar();
        setupKeyBindings();
    }

    public A getAutomaton() {
        return automaton;
    }

    // ==========================================
    // ABSTRACT HOOKS (Implemented by Subclasses)
    // ==========================================
    protected abstract S instantiateState(int x, int y, String name);
    protected abstract S copyState(S original);

    protected S createNewState(int x, int y) {
        String defaultName = automaton.generateUniqueStateName("q");
        JTextField nameField = new JTextField(defaultName);

        Object[] message = {
                "Enter State Name", nameField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New State",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String nameStr = nameField.getText();

            if (nameStr == null || nameStr.isEmpty()) {
                showErrorMessage("State names cannot be empty", "Couldn't Create State");
            }
            else if (!nameStr.equals(automaton.generateUniqueStateName(nameStr))) {
                showErrorMessage("State names must be unique", "Couldn't Create State");
            } else {
                saveState();
                return instantiateState(x, y, nameStr);
            }
        }
        return null;
    };

    protected void editState(S state) {
        JTextField nameField = new JTextField(state.getName());

        Object[] message = {
                "Enter State Name:", nameField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit State",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText();
            if (newName == null || newName.isEmpty()) showErrorMessage("State names cannot be empty", "Couldn't Edit State");
            else if (!newName.equals(state.getName()) && !newName.equals(automaton.generateUniqueStateName(newName))) {
                showErrorMessage("State names must be unique", "Couldn't Edit State");
            } else {
                saveState();
                state.setName(newName);
                repaint();
            }
        }
    };

    protected abstract T instantiateTransition(S start, S end, Character symbol);
    protected abstract T copyTransition(T original, S newStart, S newEnd);

    protected T createNewTransition(S start, S end) {
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
                return instantiateTransition(start, end, nameStr.charAt(0));
            }
        }
        return null;
    }

    protected abstract void editTransition(T transition);

    protected void addContextMenuExtras(JPopupMenu popup, S state) {}
    protected boolean isStateFinal(S state) {
        return false;
    }

    public void populateFunctionsMenu(JMenu functionsMenu, JFlatMainWindow mainWindow) {}

    // ==========================================
    // TOOLBAR & KEY BINDINGS
    // ==========================================

    private void setupToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(245, 245, 245, 220));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JToggleButton btnSelect = new JToggleButton("Select / Edit", true);
        JToggleButton btnAddState = new JToggleButton("Add State");
        JToggleButton btnAddTransition = new JToggleButton("Add Transition");
        JButton btnDetangle = new JButton("Detangle Layout");

        ButtonGroup group = new ButtonGroup();
        group.add(btnSelect);
        group.add(btnAddState);
        group.add(btnAddTransition);

        btnSelect.addActionListener(e -> currentMode = Mode.SELECT);
        btnAddState.addActionListener(e -> currentMode = Mode.ADD_STATE);
        btnAddTransition.addActionListener(e -> currentMode = Mode.ADD_TRANSITION);

        btnDetangle.addActionListener(e -> {
            if (automaton != null) {
                saveState();
                automaton.forceDirectedLayout();
                repaint();
            }
            currentMode = Mode.SELECT;
            btnSelect.setSelected(true);
        });

        toolbar.add(btnSelect);
        toolbar.add(btnAddState);
        toolbar.add(btnAddTransition);
        toolbar.add(btnDetangle);

        add(toolbar, BorderLayout.NORTH);
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, mask), "selectAll");
        am.put("selectAll", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (automaton != null && !isPastingMode && currentMode == Mode.SELECT) {
                    selectedStates.addAll(automaton.getStates());
                    selectedTransitions.addAll(automaton.getTransitions());
                    repaint();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), "copy");
        am.put("copy", new AbstractAction() { public void actionPerformed(ActionEvent e) { copySelected(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), "cut");
        am.put("cut", new AbstractAction() { public void actionPerformed(ActionEvent e) { cutSelected(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), "paste");
        am.put("paste", new AbstractAction() { public void actionPerformed(ActionEvent e) { triggerPasteMode(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        am.put("delete", new AbstractAction() { public void actionPerformed(ActionEvent e) { deleteSelected(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                isPastingMode = false;
                transitionStart = null;
                repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), "undo");
        am.put("undo", new AbstractAction() { public void actionPerformed(ActionEvent e) { undo(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), "redo");
        am.put("redo", new AbstractAction() { public void actionPerformed(ActionEvent e) { redo(); } });
    }

    // ==========================================
    // UNDO / REDO / SNAPSHOTS
    // ==========================================

    public AutomatonSnapshot<S, T> createSnapshot() {
        Set<S> clonedStates = new HashSet<>();
        Set<T> clonedTransitions = new HashSet<>();
        Map<S, S> oldToNew = new HashMap<>();
        S clonedInitialState = null;

        for (S s : automaton.getStates()) {
            boolean isInit = s.equals(automaton.getInitialState());
            S newS = copyState(s);
            clonedStates.add(newS);
            oldToNew.put(s, newS);
            if (isInit) clonedInitialState = newS;
        }

        for (T t : automaton.getTransitions()) {
            S newStart = oldToNew.get(t.getStart());
            S newEnd = oldToNew.get(t.getEnd());
            if (newStart != null && newEnd != null) {
                T newT = copyTransition(t, newStart, newEnd);
                clonedTransitions.add(newT);
            }
        }
        return new AutomatonSnapshot<>(clonedStates, clonedTransitions, clonedInitialState);
    }

    private void restoreSnapshot(AutomatonSnapshot<S, T> snap) {
        // Clear current automaton strictly using your new proper methods
        List<T> currentTransitions = new ArrayList<>(automaton.getTransitions());
        for (T t : currentTransitions) {
            automaton.removeTransition(t);
        }
        automaton.getStates().clear(); // Note: If you made an automaton.removeState(), use it here!

        Map<S, S> oldToNew = new HashMap<>();
        S clonedInitialState = null;

        for (S s : snap.states()) {
            boolean isInit = s.equals(snap.initialState());
            S newS = copyState(s);
            automaton.getStates().add(newS); // Note: Or use automaton.addState()
            oldToNew.put(s, newS);
            if (isInit) clonedInitialState = newS;
        }

        for (T t : snap.transitions()) {
            S newStart = oldToNew.get(t.getStart());
            S newEnd = oldToNew.get(t.getEnd());
            if (newStart != null && newEnd != null) {
                T newT = copyTransition(t, newStart, newEnd);
                // --- UPDATED LOGIC HERE ---
                automaton.addTransition(newT);
                // --------------------------
            }
        }

        automaton.setInitialState(clonedInitialState);

        selectedStates.clear();
        selectedTransitions.clear();
        repaint();
    }

    protected void saveState() {
        if (automaton == null) return;
        undoStack.push(createSnapshot());
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty() && automaton != null) {
            redoStack.push(createSnapshot());
            restoreSnapshot(undoStack.pop());
        }
    }

    private void redo() {
        if (!redoStack.isEmpty() && automaton != null) {
            undoStack.push(createSnapshot());
            restoreSnapshot(redoStack.pop());
        }
    }

    // ==========================================
    // CLIPBOARD (COPY/PASTE/DELETE)
    // ==========================================

    private void copySelected() {
        if (isPastingMode || currentMode != Mode.SELECT) return;
        clipboardStates.clear();
        clipboardTransitions.clear();
        clipboardInitialState = null;

        clipboardStates.addAll(selectedStates);

        for (T t : automaton.getTransitions()) {
            if (selectedStates.contains(t.getStart()) && selectedStates.contains(t.getEnd())) {
                clipboardTransitions.add(t);
            }
        }

        for (S s : selectedStates) {
            if (s.equals(automaton.getInitialState())) {
                clipboardInitialState = s;
            }
        }
    }

    private void cutSelected() {
        saveState();
        copySelected();
        deleteSelected();
    }

    private void triggerPasteMode() {
        if (automaton == null || clipboardStates.isEmpty() || currentMode != Mode.SELECT) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (S s : clipboardStates) {
            minX = Math.min(minX, s.x);
            minY = Math.min(minY, s.y);
            maxX = Math.max(maxX, s.x);
            maxY = Math.max(maxY, s.y);
        }

        clipboardCenter.x = (minX + maxX) / 2;
        clipboardCenter.y = (minY + maxY) / 2;
        isPastingMode = true;
        repaint();
    }

    public void commitPaste() {
        saveState();
        selectedStates.clear();
        selectedTransitions.clear();
        Map<S, S> oldToNew = new HashMap<>();

        int targetX = currentMouseWorldPos.x;
        int targetY = currentMouseWorldPos.y;
        if (targetX == 0 && targetY == 0) {
            targetX = (int) (((getWidth() / 2.0) - translateX) / scale);
            targetY = (int) (((getHeight() / 2.0) - translateY) / scale);
        }

        for (S old : clipboardStates) {
            int newX = targetX + (old.x - clipboardCenter.x);
            int newY = targetY + (old.y - clipboardCenter.y);

            S newS = copyState(old);
            newS.x = newX;
            newS.y = newY;

            automaton.addState(newS);
            selectedStates.add(newS);
            oldToNew.put(old, newS);

            if (old.equals(clipboardInitialState) && automaton.getInitialState() == null) {
                automaton.setInitialState(newS);
            }
        }

        for (T oldT : clipboardTransitions) {
            S newStart = oldToNew.get(oldT.getStart());
            S newEnd = oldToNew.get(oldT.getEnd());

            if (newStart != null && newEnd != null) {
                T newT = copyTransition(oldT, newStart, newEnd);
                // --- UPDATED LOGIC HERE ---
                automaton.addTransition(newT);
                // --------------------------
                selectedTransitions.add(newT);
            }
        }
        isPastingMode = false;
        repaint();
    }

    private void deleteSelected() {
        if (automaton == null || isPastingMode || (selectedStates.isEmpty() && selectedTransitions.isEmpty())) return;
        saveState();

        for (T t : selectedTransitions) {
            // --- UPDATED LOGIC HERE ---
            automaton.removeTransition(t);
            // --------------------------
        }

        for (S s : selectedStates) {
            List<T> connected = automaton.getTransitions().stream()
                    .filter(t -> t.getStart().equals(s) || t.getEnd().equals(s))
                    .toList();

            for (T t : connected) {
                // --- UPDATED LOGIC HERE ---
                automaton.removeTransition(t);
                // --------------------------
            }
            automaton.getStates().remove(s);

            if (s.equals(automaton.getInitialState())) {
                automaton.setInitialState(null);
            }
        }
        selectedStates.clear();
        selectedTransitions.clear();
        repaint();
    }

    // ==========================================
    // UTILITIES
    // ==========================================

    protected S getStateAt(double worldX, double worldY) {
        if (automaton == null) return null;
        for (S s : automaton.getStates()) {
            double dx = worldX - s.x;
            double dy = worldY - s.y;
            if (dx * dx + dy * dy <= STATE_RADIUS * STATE_RADIUS) return s;
        }
        return null;
    }

    protected void showStateContextMenu(MouseEvent e, S state) {
        JPopupMenu popup = new JPopupMenu();

        JCheckBoxMenuItem startItem = new JCheckBoxMenuItem("Initial State");
        startItem.setSelected(state.equals(automaton.getInitialState()));
        startItem.addActionListener(a -> {
            saveState();
            if(startItem.isSelected()) automaton.setInitialState(state);
            else automaton.setInitialState(null);
            repaint();
        });
        popup.add(startItem);
        addContextMenuExtras(popup, state);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D) g);
    }

    public void detangleLayout() {
        if (automaton != null) {
            saveState();
            automaton.forceDirectedLayout();
            repaint();
        }
    }

    protected void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    protected boolean isAutomatonValidFor(String operationName) {
        if (automaton.isValid()) return true;

        showErrorMessage("This automaton is not valid. Cannot perform " + operationName + ".", "Invalid Automata");
        return false;
    }

    public java.io.File getCurrentFile() { return currentFile; }
    public void setCurrentFile(java.io.File file) { this.currentFile = file; }
    public String getLatexRepresentation() { return automaton.getLatex(); }

    public record AutomatonSnapshot<S, T>(Set<S> states, Set<T> transitions, S initialState) {}
}