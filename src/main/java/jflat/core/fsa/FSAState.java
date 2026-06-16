package core.fsa;

import core.generics.AbstractState;

public class FSAState extends AbstractState<FSAState, FSATransition> {
    private boolean isFinal;

    public FSAState(boolean isFinal, String name, int x, int y) {
        super(name, x, y);
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    @Override
    public String getStateText() {
        return getName();
    }
}