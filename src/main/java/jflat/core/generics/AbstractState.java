package core.generics;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractState<S extends AbstractState<S, T>, T extends AbstractTransition<S, T>> implements State<S, T>{
    private String name;
    public int x;
    public int y;
    protected double dx = 0;
    protected double dy = 0;
    private final Set<T> transitions = new HashSet<>();

    public AbstractState(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public Set<T> getTransitions() {
        return transitions;
    }

    protected void addTransition(T transition) {
        transitions.add(transition);
    }

    protected void removeTransition(T transition) {
        transitions.remove(transition);
    }

    public String getTransitionsTextTo(S destination) {
        return this.transitions.stream()
                .filter(t -> t.getEnd().equals(destination))
                .map(Transition::getTransitionText)
                .sorted((a, b) -> {
                    if ("λ".equals(a) && "λ".equals(b)) return 0;
                    if ("λ".equals(a)) return -1;
                    if ("λ".equals(b)) return 1;
                    return a.compareTo(b);
                })
                .collect(Collectors.joining(", "));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
