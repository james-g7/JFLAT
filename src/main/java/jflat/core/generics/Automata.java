package jflat.core.generics;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Automata<S extends State<S, T>, T extends Transition<S, T>> {
    Set<S> getStates();
    void addState(S state);
    void removeState(S state);

    Set<T> getTransitions();
    void addTransition(T transition);
    void removeTransition(T transition);

    S getInitialState();
    void setInitialState(S state);

    String getName();
    void setName(String name);
    String getLatex();

    boolean isValid();

    String generateUniqueStateName(String baseName);
    Set<S> getReachableStates();
    void removeUselessStates();

    int getGraphWidth();
    int getGraphHeight();

    void forceDirectedLayout();

    record StatePair<S>(S start, S end) {}
    Map<AbstractAutomata.StatePair<S>, List<T>> getGroupedTransitions();
}

