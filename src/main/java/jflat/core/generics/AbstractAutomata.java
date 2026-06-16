package jflat.core.generics;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractAutomata<S extends AbstractState<S, T>, T extends AbstractTransition<S, T>> implements Automata<S, T>{
    protected String name;
    protected Set<S> states = new HashSet<>();
    protected Set<T> transitions = new HashSet<>();
    protected S initialState;

    public AbstractAutomata(String name) {
        this.name = name;
    }

    public void addState(S state) {
        this.states.add(state);
    }

    public void removeState(S state) {
        if (states.remove(state)) {
            transitions.removeIf(t -> t.getStart().equals(state) || t.getEnd().equals(state));
        }
    }

    public Set<S> getStates() {
        return states;
    }

    public Set<T> getTransitions() {
        return transitions;
    }

    public void removeTransition(T transition) {
        transitions.remove(transition);
        transition.getStart().removeTransition(transition);
    }

    public void addTransition(T transition) {
        transitions.add(transition);
        transition.getStart().addTransition(transition);
    }

    public boolean isValid() {
        if (initialState == null) {
            return false;
        }

        return getReachableStates().equals(getStates());
    }

    public int getGraphWidth() {
        int min = 0;
        int max = 0;
        for(S state : states) {
            if (state.x < min) {
                min = state.x;
            }
            if (state.x > max) {
                max = state.x;
            }
        }
        return max - min;
    }

    public int getGraphHeight() {
        int min = 0;
        int max = 0;
        for(S state : states) {
            if (state.y < min) {
                min = state.y;
            }
            if (state.y > max) {
                max = state.y;
            }
        }
        return max - min;
    }

    public void forceDirectedLayout() {
        int width = getGraphWidth();
        int height = getGraphHeight();
        double idealEdgeLength = Math.sqrt((double) (width * height) / states.size());
        double temperature = width / 10.0;
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            for (S v : states) {
                v.dx = 0;
                v.dy = 0;
                for (S u : states) {
                    if (v != u) {
                        double deltaX = v.x - u.x;
                        double deltaY = v.y - u.y;
                        double distance = Math.max(Math.hypot(deltaX, deltaY), 0.0001);

                        double repulsionForce = (idealEdgeLength * idealEdgeLength) / distance;
                        v.dx += (deltaX / distance) * repulsionForce;
                        v.dy += (deltaY / distance) * repulsionForce;
                    }
                }
            }

            for(T t : transitions) {
                double deltaX = t.getStart().x - t.getEnd().x;
                double deltaY = t.getStart().y - t.getEnd().y;
                double distance = Math.max(Math.hypot(deltaX, deltaY), 0.0001);

                double attractionForce = (distance * distance) / idealEdgeLength;
                double displacementX = (deltaX / distance) * attractionForce;
                double displacementY = (deltaY / distance) * attractionForce;

                t.getStart().dx -= displacementX;
                t.getStart().dy -= displacementY;
                t.getEnd().dx += displacementX;
                t.getEnd().dy += displacementY;
            }

            for(S v : states) {
                double gravDeltaX = centerX - v.x;
                double gravDeltaY = centerY - v.y;
                double gravDist = Math.max(Math.hypot(gravDeltaX, gravDeltaY), 0.0001);
                v.dx += (int) ((gravDeltaX / gravDist) * (gravDist * 0.05)); // Gravity strength constant

                // Limit movement by current temperature
                double distance = Math.max(Math.hypot(v.dx, v.dy), 0.0001);
                double limitedDist = Math.min(distance, temperature);

                v.x += (int) ((v.dx / distance) * limitedDist);
                v.y += (int) ((v.dy / distance) * limitedDist);

                // Keep nodes bounded within screen limits
                v.x = Math.clamp(v.x, 0, width);
                v.y = Math.clamp(v.y, 0, height);
            }

            temperature *= 0.95;
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public S getInitialState() {
        return initialState;
    }

    public void setInitialState(S state) {
        initialState = state;
    }

    public String generateUniqueStateName(String baseName) {
        if (baseName == null || baseName.isEmpty()) baseName = "q";
        Set<String> existingNames = getStates().stream()
                .map(AbstractState::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!existingNames.contains(baseName)) return baseName;

        int counter = 1;
        while (existingNames.contains(baseName + "_" + counter)) counter++;
        return baseName + "_" + counter;
    }

    public Map<StatePair<S>, List<T>> getGroupedTransitions() {
        return Transition.groupTransitions(transitions);
    }

    public Set<S> getReachableStates() {
        Set<S> reachable = new HashSet<>();

        if (initialState == null) {
            return reachable;
        }

        Queue<S> toProcess = new LinkedList<>();
        toProcess.add(initialState);
        reachable.add(initialState);

        while (!toProcess.isEmpty()) {
            S cur = toProcess.poll();
            for (T t : cur.getTransitions()) {
                S end = t.getEnd();

                if (end != null && reachable.add(end)) {
                    toProcess.add(end);
                }
            }
        }

        return reachable;
    }

    public void removeUselessStates() {
        Set<S> usefulStates = getReachableStates();
        for(S state : states) {
            if(!usefulStates.contains(state)) {
                removeState(state);
            }
        }
    }
}