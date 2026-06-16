package core.fst.moore;

import core.generics.AbstractState;

public class MooreMachineState extends AbstractState<MooreMachineState, MooreMachineTransition> {
    private Character output;

    public MooreMachineState(String name, int x, int y, Character output) {
        super(name, x, y);
        this.output = output;
    }

    public Character getOutput() {
        return output;
    }

    public void setOutput(Character output) {
        this.output = output;
    }

    @Override
    public String getStateText() {
        if (output == null) {
            return String.format("%s/", getName());
        }
        return String.format("%s/%s", getName(), output);
    }
}
