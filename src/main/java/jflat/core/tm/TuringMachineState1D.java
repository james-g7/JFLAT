package core.tm;

import core.generics.AbstractState;

public class TuringMachineState1D extends AbstractState<TuringMachineState1D, TuringMachineTransition1D> {
    private boolean isFinal;

    public TuringMachineState1D(String name, int x, int y, boolean isFinal) {
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

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
}
