package jflat.core.generics;

import java.util.Set;

public interface State<S extends State<S, T>, T extends Transition<S, T>> {
    String getName();
    void setName(String name);

    Set<T> getTransitions();

    String getTransitionsTextTo(S destination);
    String getStateText();

    int hashCode();
}
