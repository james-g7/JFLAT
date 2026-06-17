package ui;

import core.generics.AbstractAutomata;
import core.generics.AbstractState;
import core.generics.AbstractTransition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class AutomatonMouseHandler<
        S extends AbstractState<S, T>,
        T extends AbstractTransition<S, T>,
        A extends AbstractAutomata<S, T>> extends MouseAdapter {

    private final AbstractAutomatonCanvas<S, T, A> canvas;
    private Point lastMousePoint = null;
    private boolean isDraggingStates = false;
    private boolean hasDragged = false;
    private AbstractAutomatonCanvas.AutomatonSnapshot<S, T> preDragSnapshot = null;

    public AutomatonMouseHandler(AbstractAutomatonCanvas<S, T, A> canvas) {
        this.canvas = canvas;
    }

    private boolean isPanEvent(MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e) ||
                SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) && e.isAltDown());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        canvas.currentMouseWorldPos.x = (int) ((e.getX() - canvas.translateX) / canvas.scale);
        canvas.currentMouseWorldPos.y = (int) ((e.getY() - canvas.translateY) / canvas.scale);
        canvas.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePoint = e.getPoint();
        double worldX = (e.getX() - canvas.translateX) / canvas.scale;
        double worldY = (e.getY() - canvas.translateY) / canvas.scale;

        canvas.currentMouseWorldPos.x = (int) worldX;
        canvas.currentMouseWorldPos.y = (int) worldY;
        hasDragged = false;

        if (isPanEvent(e)) {
            boolean actionCancelled = false;
            if (canvas.isPastingMode) { canvas.isPastingMode = false; actionCancelled = true; }
            if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_TRANSITION && canvas.transitionStart != null) { canvas.transitionStart = null; actionCancelled = true; }

            if (actionCancelled) { canvas.repaint(); return; }

            S clickedState = canvas.getStateAt(worldX, worldY);
            if (SwingUtilities.isRightMouseButton(e) && clickedState != null) {
                canvas.showStateContextMenu(e, clickedState);
                lastMousePoint = null;
            }
            return;
        }

        if (canvas.isPastingMode && SwingUtilities.isLeftMouseButton(e)) {
            canvas.commitPaste();
            return;
        }

        if (canvas.getAutomaton() == null) return;
        S clickedState = canvas.getStateAt(worldX, worldY);

        if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_STATE) {
            if (SwingUtilities.isLeftMouseButton(e) && clickedState == null) {
                S newState = canvas.createNewState((int) worldX, (int) worldY);
                if (newState != null) {
                    canvas.getAutomaton().addState(newState);
                    canvas.repaint();
                }
            }
            return;
        }

        if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_TRANSITION) {
            if (SwingUtilities.isLeftMouseButton(e) && clickedState != null) {
                canvas.transitionStart = clickedState;
                canvas.tempTransitionEnd = new Point((int) worldX, (int) worldY);
                canvas.repaint();
            }
            return;
        }

        List<T> clickedTransitions = getTransitionsAt(worldX, worldY);

        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && canvas.currentMode == AbstractAutomatonCanvas.Mode.SELECT) {
            if (clickedState != null) {
                canvas.editState(clickedState);
                canvas.saveState();
                canvas.repaint();
                isDraggingStates = false;
                hasDragged = false;
                return;
            } else if (!clickedTransitions.isEmpty()) {
                T t = clickedTransitions.getFirst();
                canvas.editTransition(t);
                canvas.saveState();
                canvas.repaint();
                return;
            }
        }

        boolean ctrl = e.isControlDown();
        boolean shift = e.isShiftDown();

        if (clickedState != null) {
            preDragSnapshot = canvas.createSnapshot();
            isDraggingStates = true;

            if (shift && canvas.lastClickedState != null && canvas.lastClickedState != clickedState) {
                canvas.selectedStates.add(clickedState);
                canvas.selectedStates.add(canvas.lastClickedState);
                for (T t : canvas.getAutomaton().getTransitions()) {
                    if ((t.getStart().equals(canvas.lastClickedState) && t.getEnd().equals(clickedState)) ||
                            (t.getStart().equals(clickedState) && t.getEnd().equals(canvas.lastClickedState))) {
                        canvas.selectedTransitions.add(t);
                    }
                }
            } else if (ctrl) {
                if (canvas.selectedStates.contains(clickedState)) {
                    canvas.selectedStates.remove(clickedState);
                    isDraggingStates = false;
                } else canvas.selectedStates.add(clickedState);
            } else {
                if (!canvas.selectedStates.contains(clickedState)) {
                    canvas.selectedStates.clear();
                    canvas.selectedTransitions.clear();
                    canvas.selectedStates.add(clickedState);
                }
            }
            canvas.lastClickedState = clickedState;
        } else if (!clickedTransitions.isEmpty()) {
            if (ctrl) {
                if (canvas.selectedTransitions.containsAll(clickedTransitions)) clickedTransitions.forEach(canvas.selectedTransitions::remove);
                else canvas.selectedTransitions.addAll(clickedTransitions);
            } else {
                canvas.selectedStates.clear();
                canvas.selectedTransitions.clear();
                canvas.selectedTransitions.addAll(clickedTransitions);
            }
        } else {
            if (!ctrl && !shift) {
                canvas.selectedStates.clear();
                canvas.selectedTransitions.clear();
                canvas.lastClickedState = null;
            }
            canvas.selectionStartPoint = e.getPoint();
        }
        canvas.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        canvas.currentMouseWorldPos.x = (int) ((e.getX() - canvas.translateX) / canvas.scale);
        canvas.currentMouseWorldPos.y = (int) ((e.getY() - canvas.translateY) / canvas.scale);

        if (lastMousePoint == null) return;

        if (isPanEvent(e)) {
            canvas.translateX += e.getX() - lastMousePoint.x;
            canvas.translateY += e.getY() - lastMousePoint.y;
            lastMousePoint = e.getPoint();
        }
        else if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_TRANSITION && canvas.transitionStart != null) {
            canvas.tempTransitionEnd = new Point(canvas.currentMouseWorldPos.x, canvas.currentMouseWorldPos.y);
            canvas.repaint();
            return;
        }
        else if (isDraggingStates && !canvas.selectedStates.isEmpty() && canvas.currentMode == AbstractAutomatonCanvas.Mode.SELECT) {
            hasDragged = true;
            double worldDx = (e.getX() - lastMousePoint.x) / canvas.scale;
            double worldDy = (e.getY() - lastMousePoint.y) / canvas.scale;

            FontMetrics fm = canvas.getFontMetrics(canvas.getFont());
            var grouped = canvas.getAutomaton().getGroupedTransitions();

            for (S s : canvas.selectedStates) {
                double proposedX = s.x + worldDx;
                double proposedY = s.y + worldDy;

                for (var entry : grouped.entrySet()) {
                    S start = entry.getKey().start();
                    S end = entry.getKey().end();
                    if (start.equals(end)) continue;

                    S other = null;
                    if (start.equals(s)) other = end;
                    if (end.equals(s)) other = start;

                    if (other != null && !canvas.selectedStates.contains(other)) {
                        String text = start.getTransitionsTextTo(end);
                        int textWidth = fm.stringWidth(text);
                        double minLength = textWidth + (canvas.STATE_RADIUS * 2) + 20;

                        double dx = proposedX - other.x;
                        double dy = proposedY - other.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        if (dist < minLength && dist > 0.01) {
                            proposedX = other.x + (dx / dist) * minLength;
                            proposedY = other.y + (dy / dist) * minLength;
                        }
                    }
                }
                s.x = (int) proposedX;
                s.y = (int) proposedY;
            }
            lastMousePoint = e.getPoint();
        } else if (canvas.selectionStartPoint != null && canvas.currentMode == AbstractAutomatonCanvas.Mode.SELECT) {
            int x = Math.min(canvas.selectionStartPoint.x, e.getX());
            int y = Math.min(canvas.selectionStartPoint.y, e.getY());
            int width = Math.abs(canvas.selectionStartPoint.x - e.getX());
            int height = Math.abs(canvas.selectionStartPoint.y - e.getY());

            double worldX = (x - canvas.translateX) / canvas.scale;
            double worldY = (y - canvas.translateY) / canvas.scale;
            double worldW = width / canvas.scale;
            double worldH = height / canvas.scale;

            canvas.selectionBox = new Rectangle2D.Double(worldX, worldY, worldW, worldH);
        }
        canvas.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_TRANSITION && canvas.transitionStart != null) {
            S releasedState = canvas.getStateAt(canvas.currentMouseWorldPos.x, canvas.currentMouseWorldPos.y);
            if (releasedState != null) {
                T newT = canvas.createNewTransition(canvas.transitionStart, releasedState);
                if (newT != null) {
                    canvas.saveState();
                    // --- UPDATED LOGIC HERE ---
                    canvas.getAutomaton().addTransition(newT);
                    // --------------------------
                }
            }
            canvas.transitionStart = null;
            canvas.tempTransitionEnd = null;
            canvas.repaint();
            return;
        }

        if (isDraggingStates && hasDragged && preDragSnapshot != null) {
            canvas.undoStack.push(preDragSnapshot);
            canvas.redoStack.clear();
        }

        if (canvas.selectionBox != null && canvas.getAutomaton() != null && canvas.currentMode == AbstractAutomatonCanvas.Mode.SELECT) {
            for (S s : canvas.getAutomaton().getStates()) {
                if (canvas.selectionBox.contains(s.x, s.y)) canvas.selectedStates.add(s);
            }
            var grouped = canvas.getAutomaton().getGroupedTransitions();
            for (var entry : grouped.entrySet()) {
                if (canvas.selectionBox.contains(entry.getKey().start().x, entry.getKey().start().y) ||
                        canvas.selectionBox.contains(entry.getKey().end().x, entry.getKey().end().y)) {
                    canvas.selectedTransitions.addAll(entry.getValue());
                }
            }
        }
        isDraggingStates = false;
        hasDragged = false;
        preDragSnapshot = null;
        canvas.selectionStartPoint = null;
        canvas.selectionBox = null;
        canvas.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double oldScale = canvas.scale;
        if (e.getWheelRotation() < 0) canvas.scale *= 1.1;
        else canvas.scale /= 1.1;

        canvas.translateX = e.getX() - (e.getX() - canvas.translateX) * (canvas.scale / oldScale);
        canvas.translateY = e.getY() - (e.getY() - canvas.translateY) * (canvas.scale / oldScale);

        canvas.currentMouseWorldPos.x = (int) ((e.getX() - canvas.translateX) / canvas.scale);
        canvas.currentMouseWorldPos.y = (int) ((e.getY() - canvas.translateY) / canvas.scale);
        canvas.repaint();
    }

    private List<T> getTransitionsAt(double worldX, double worldY) {
        List<T> clickedTransitions = new ArrayList<>();
        var grouped = canvas.getAutomaton().getGroupedTransitions();

        for (var entry : grouped.entrySet()) {
            S start = entry.getKey().start();
            S end = entry.getKey().end();

            double dist;
            if (start.equals(end)) {
                int loopRadius = 15;
                double cx = start.x;
                double cy = start.y - canvas.STATE_RADIUS - loopRadius;
                double dx = worldX - cx;
                double dy = worldY - cy;
                dist = Math.abs(Math.sqrt(dx * dx + dy * dy) - loopRadius);
            } else {
                boolean hasReverse = grouped.keySet().stream().anyMatch(k -> k.start().equals(end) && k.end().equals(start));
                if (hasReverse) {
                    double mx = (start.x + end.x) / 2.0;
                    double my = (start.y + end.y) / 2.0;
                    double lineDx = end.x - start.x;
                    double lineDy = end.y - start.y;
                    double lineLen = Math.sqrt(lineDx * lineDx + lineDy * lineDy);
                    double cx = mx + (-lineDy / lineLen) * 30.0;
                    double cy = my + (lineDx / lineLen) * 30.0;

                    double d1 = Line2D.ptSegDist(start.x, start.y, cx, cy, worldX, worldY);
                    double d2 = Line2D.ptSegDist(cx, cy, end.x, end.y, worldX, worldY);
                    dist = Math.min(d1, d2);
                } else {
                    dist = Line2D.ptSegDist(start.x, start.y, end.x, end.y, worldX, worldY);
                }
            }

            if (dist <= 5.0 / canvas.scale) {
                clickedTransitions.addAll(entry.getValue());
                break;
            }
        }
        return clickedTransitions;
    }
}