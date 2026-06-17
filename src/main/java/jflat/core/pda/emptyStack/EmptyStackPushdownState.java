package core.pda.emptyStack;

import core.generics.AbstractState;

public class EmptyStackPushdownState extends AbstractState<EmptyStackPushdownState, EmptyStackPushdownTransition> {
    public EmptyStackPushdownState(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public String getStateText() {
        return getName();
    }
}
