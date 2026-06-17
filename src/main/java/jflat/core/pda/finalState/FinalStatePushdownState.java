package core.pda.finalState;

import core.generics.AbstractState;

public class FinalStatePushdownState extends AbstractState<FinalStatePushdownState, FinalStatePushdownTransition> {
    private boolean isFinal;

    public FinalStatePushdownState(String name, int x, int y, boolean isFinal) {
        super(name, x, y);
        this.isFinal = isFinal;
    }

    @Override
    public String getStateText() {
        return getName();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
}
