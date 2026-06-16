package jflat.core.generics;

public abstract class AbstractTransition<S extends AbstractState<S, T>, T extends AbstractTransition<S, T>> implements Transition<S, T> {
    private final S start;
    private final S end;

    public AbstractTransition(S start, S end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public S getStart() {
        return start;
    }

    @Override
    public S getEnd() {
        return end;
    }
}