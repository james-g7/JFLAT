package core.fst.mealy;

import core.generics.AbstractState;

public class MealyMachineState extends AbstractState<MealyMachineState, MealyMachineTransition> {

    public MealyMachineState(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public String getStateText() {
        return getName();
    }
}
