package core.generics;

import java.util.*;

public interface Transition<S extends State<S, T>, T extends Transition<S, T>> {
    S getStart();
    S getEnd();
    String getTransitionText();

    static <S extends State<S, T>, T extends Transition<S, T>>
    Map<AbstractAutomata.StatePair<S>, List<T>> groupTransitions(Set<T> transitions) {
        Map<AbstractAutomata.StatePair<S>, List<T>> grouped = new HashMap<>();
        if (transitions != null) {
            for (T t : transitions) {
                AbstractAutomata.StatePair<S> pair =
                        new AbstractAutomata.StatePair<>(t.getStart(), t.getEnd());
                grouped.computeIfAbsent(pair, k -> new ArrayList<>()).add(t);
            }
        }
        return grouped;
    }
}
