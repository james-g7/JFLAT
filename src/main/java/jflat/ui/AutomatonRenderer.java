package jflat.ui;

import jflat.core.generics.AbstractAutomata;
import jflat.core.generics.AbstractState;
import jflat.core.generics.AbstractTransition;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.util.Objects;

public class AutomatonRenderer<
        S extends AbstractState<S, T>,
        T extends AbstractTransition<S, T>,
        A extends AbstractAutomata<S, T>> {

    private final AbstractAutomatonCanvas<S, T, A> canvas;

    public AutomatonRenderer(AbstractAutomatonCanvas<S, T, A> canvas) {
        this.canvas = canvas;
    }

    public void render(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        A automaton = canvas.getAutomaton();
        if (automaton != null) {
            int autoSpaceIndex = 1;
            for (S s : automaton.getStates()) {
                if (s.x == 0 && s.y == 0) {
                    s.x = 80 * autoSpaceIndex;
                    s.y = 80 * autoSpaceIndex;
                    autoSpaceIndex++;
                }
            }
        }

        if (canvas.selectionBox != null && canvas.selectionStartPoint != null) {
            g2d.setColor(new Color(0, 120, 215, 50));
            Point mousePos = canvas.getMousePosition();
            if (mousePos != null) {
                int x = Math.min(canvas.selectionStartPoint.x, mousePos.x);
                int y = Math.min(canvas.selectionStartPoint.y, mousePos.y);
                int w = Math.abs(canvas.selectionStartPoint.x - mousePos.x);
                int h = Math.abs(canvas.selectionStartPoint.y - mousePos.y);
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(0, 120, 215));
                g2d.drawRect(x, y, w, h);
            }
        }

        AffineTransform originalTransform = g2d.getTransform();
        g2d.translate(canvas.translateX, canvas.translateY);
        g2d.scale(canvas.scale, canvas.scale);

        if (canvas.currentMode == AbstractAutomatonCanvas.Mode.ADD_TRANSITION && canvas.transitionStart != null && canvas.tempTransitionEnd != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke((float) (2.0 / canvas.scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10, 10}, 0));
            g2d.drawLine(canvas.transitionStart.x, canvas.transitionStart.y, canvas.tempTransitionEnd.x, canvas.tempTransitionEnd.y);
            g2d.setStroke(new BasicStroke((float) (2.0 / canvas.scale)));
        }

        if (automaton != null) {
            var groupedTransitions = automaton.getGroupedTransitions();
            g2d.setStroke(new BasicStroke((float) (2.0 / canvas.scale)));

            for (var entry : groupedTransitions.entrySet()) {
                boolean isSelected = entry.getValue().stream().anyMatch(canvas.selectedTransitions::contains);
                g2d.setColor(isSelected ? Color.BLUE : Color.BLACK);
                String text = entry.getKey().start().getTransitionsTextTo(entry.getKey().end());

                // Check if a reverse transition exists to curve the line
                boolean hasReverse = groupedTransitions.keySet().stream()
                        .anyMatch(k -> k.start().equals(entry.getKey().end()) && k.end().equals(entry.getKey().start()));

                drawTransitionGroup(g2d, entry.getKey().start(), entry.getKey().end(), text, hasReverse);
            }

            for (S s : automaton.getStates()) {
                Color bgColor = canvas.selectedStates.contains(s) ? new Color(173, 216, 230) : Color.LIGHT_GRAY;
                Color borderColor = canvas.selectedStates.contains(s) ? Color.BLUE : Color.BLACK;

                boolean isStarting = s.equals(automaton.getInitialState());
                boolean isFinal = canvas.isStateFinal(s);
                drawStateVisuals(g2d, s.x, s.y, isFinal, isStarting, s.getStateText(), bgColor, borderColor);
            }
        }

        if (canvas.isPastingMode && !canvas.clipboardStates.isEmpty()) {
            drawClipboardGhosts(g2d);
        }

        g2d.setTransform(originalTransform);
        drawOverlays(g2d);
    }

    private void drawClipboardGhosts(Graphics2D g2d) {
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g2d.setStroke(new BasicStroke((float) (2.0 / canvas.scale)));
        g2d.setColor(Color.BLACK);

        int targetX = canvas.currentMouseWorldPos.x;
        int targetY = canvas.currentMouseWorldPos.y;
        if (targetX == 0 && targetY == 0) {
            targetX = (int) (((canvas.getWidth() / 2.0) - canvas.translateX) / canvas.scale);
            targetY = (int) (((canvas.getHeight() / 2.0) - canvas.translateY) / canvas.scale);
        }

        for (T ghostT : canvas.clipboardTransitions) {
            int startX = targetX + (ghostT.getStart().x - canvas.clipboardCenter.x);
            int startY = targetY + (ghostT.getStart().y - canvas.clipboardCenter.y);
            int endX = targetX + (ghostT.getEnd().x - canvas.clipboardCenter.x);
            int endY = targetY + (ghostT.getEnd().y - canvas.clipboardCenter.y);
            g2d.drawLine(startX, startY, endX, endY);
        }

        for (S s : canvas.clipboardStates) {
            int ghostX = targetX + (s.x - canvas.clipboardCenter.x);
            int ghostY = targetY + (s.y - canvas.clipboardCenter.y);

            boolean isStarting = s.equals(canvas.clipboardInitialState) && Objects.requireNonNull(canvas.getAutomaton()).getInitialState() == null;
            boolean isFinal = canvas.isStateFinal(s);
            drawStateVisuals(g2d, ghostX, ghostY, isFinal, isStarting, s.getStateText(), Color.LIGHT_GRAY, Color.BLACK);
        }
        g2d.setComposite(oldComposite);
    }

    private void drawOverlays(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String cordText = String.format("X: %d, Y: %d", canvas.currentMouseWorldPos.x, canvas.currentMouseWorldPos.y);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(cordText, canvas.getWidth() - fm.stringWidth(cordText) - 10, canvas.getHeight() - 10);

        if (canvas.isPastingMode) {
            g2d.drawString("Pasting... Left-click to place, Right-click to cancel.", 10, canvas.getHeight() - 10);
        }
    }

    private void drawStateVisuals(Graphics2D g2d, int x, int y, boolean isFinal, boolean isStarting, String name, Color bg, Color border) {
        int radius = canvas.STATE_RADIUS;
        int drawX = x - radius;
        int drawY = y - radius;
        int diameter = radius * 2;

        g2d.setColor(bg);
        g2d.fillOval(drawX, drawY, diameter, diameter);
        g2d.setColor(border);
        g2d.drawOval(drawX, drawY, diameter, diameter);

        if (isFinal) {
            int padding = 4;
            g2d.drawOval(drawX + padding, drawY + padding, diameter - (padding * 2), diameter - (padding * 2));
        }

        if (isStarting) {
            g2d.drawLine(drawX - 25, y, drawX, y);
            g2d.drawLine(drawX, y, drawX - 8, y - 5);
            g2d.drawLine(drawX, y, drawX - 8, y + 5);
        }

        if (name != null) {
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x - (fm.stringWidth(name) / 2);
            int textY = y + (fm.getAscent() / 2) - 1;
            g2d.drawString(name, textX, textY);
        }
    }

    private void drawTransitionGroup(Graphics2D g2d, S start, S end, String text, boolean hasReverse) {
        int radius = canvas.STATE_RADIUS;

        if (start.x == end.x && start.y == end.y) {
            int loopRadius = 15;
            g2d.drawArc(start.x - loopRadius, start.y - radius - loopRadius * 2, loopRadius * 2, loopRadius * 2, 0, 360);
            g2d.drawLine(start.x, start.y - radius, start.x - 5, start.y - radius - 8);
            g2d.drawLine(start.x, start.y - radius, start.x + 5, start.y - radius - 8);

            if (text != null && !text.isEmpty()) {
                int textX = start.x - (g2d.getFontMetrics().stringWidth(text) / 2);
                int textY = start.y - radius - (loopRadius * 2) - 5;
                g2d.drawString(text, textX, textY);
            }
        } else if (hasReverse) {
            // Draw curved line to avoid bidirectional overlap
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist == 0) return;

            double mx = (start.x + end.x) / 2.0;
            double my = (start.y + end.y) / 2.0;

            // Push the control point outwards perpendicularly
            double curveOffset = 30.0;
            double cx = mx + (-dy / dist) * curveOffset;
            double cy = my + (dx / dist) * curveOffset;

            // Calculate angles to properly attach to the circle edge
            double angleStart = Math.atan2(cy - start.y, cx - start.x);
            double arrowAngle = Math.atan2(end.y - cy, end.x - cx);

            int startX = (int) (start.x + radius * Math.cos(angleStart));
            int startY = (int) (start.y + radius * Math.sin(angleStart));
            int endX = (int) (end.x - radius * Math.cos(arrowAngle));
            int endY = (int) (end.y - radius * Math.sin(arrowAngle));

            QuadCurve2D curve = new QuadCurve2D.Double(startX, startY, cx, cy, endX, endY);
            g2d.draw(curve);

            // Draw arrowhead along tangent
            int arrowSize = 10;
            double arrowSpread = Math.PI / 6;
            g2d.drawLine(endX, endY, (int) (endX - arrowSize * Math.cos(arrowAngle - arrowSpread)), (int) (endY - arrowSize * Math.sin(arrowAngle - arrowSpread)));
            g2d.drawLine(endX, endY, (int) (endX - arrowSize * Math.cos(arrowAngle + arrowSpread)), (int) (endY - arrowSize * Math.sin(arrowAngle + arrowSpread)));

            if (text != null && !text.isEmpty()) {
                int textX = (int) (cx - (g2d.getFontMetrics().stringWidth(text) / 2.0));
                int textY = (int) cy - 5;
                g2d.drawString(text, textX, textY);
            }
        } else {
            // Standard straight line
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            double angle = Math.atan2(dy, dx);
            double length = Math.sqrt(dx * dx + dy * dy);

            int startX = (int) (start.x + radius * Math.cos(angle));
            int startY = (int) (start.y + radius * Math.sin(angle));
            int endX = (int) (end.x - radius * Math.cos(angle));
            int endY = (int) (end.y - radius * Math.sin(angle));

            g2d.drawLine(startX, startY, endX, endY);

            int arrowSize = 10;
            double arrowAngle = Math.PI / 6;
            g2d.drawLine(endX, endY, (int) (endX - arrowSize * Math.cos(angle - arrowAngle)), (int) (endY - arrowSize * Math.sin(angle - arrowAngle)));
            g2d.drawLine(endX, endY, (int) (endX - arrowSize * Math.cos(angle + arrowAngle)), (int) (endY - arrowSize * Math.sin(angle + arrowAngle)));

            if (text != null && !text.isEmpty()) {
                int textOffsetX = (int) ((dy / length) * 15);
                int textOffsetY = (int) ((-dx / length) * 15);
                int textX = (int) (((start.x + end.x) / 2.0) + textOffsetX - (g2d.getFontMetrics().stringWidth(text) / 2.0));
                int textY = (int) (((start.y + end.y) / 2.0) + textOffsetY);
                g2d.drawString(text, textX, textY);
            }
        }
    }
}
